package com.kaifengcrt.exnihilosatellitium.common.blockentity;

import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import com.kaifengcrt.exnihilosatellitium.common.tag.ModTags;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Barrel state: a 1000 mB fluid tank, a compost progress bar, and a solid
 * block (cobblestone / obsidian / netherrack / end stone) produced by fluid
 * reactions.
 *
 * <p>The barrel is never replaced. Composting only works while the tank is
 * empty. When the progress reaches {@link #COMPOST_MAX}, a timer starts; while
 * it runs the renderer blends the compost colour towards dirt. Once it ends the
 * barrel enters the DIRT state; right-clicking extracts one dirt item and
 * resets the barrel.</p>
 *
 * <p>Fluid rules (after Ex Nihilo: Sequentia): wooden barrels cannot store
 * fluids hotter than {@link #WOOD_MAX_TEMPERATURE} K (lava is 1300 K), stone
 * barrels can. The reactions themselves come from
 * {@link BarrelRecipes} ({@code data/<namespace>/barrel/*.json}): pouring the
 * second fluid into a full barrel, or right-clicking an item, replaces the
 * tank with a solid block. That block is held in {@link #solid} and taken out
 * with a right-click.</p>
 */
public class BarrelBlockEntity extends BlockEntity {

    /** One compost ingredient: the item and the points it contributed. */
    public record CompostIngredient(Item item, int points) {
    }

    public static final int CAPACITY = 1000;
    /**
     * Compost capacity. Ex Nihilo Omnia uses a 1000 mB table, so 0.125
     * saplings become 125 and the 1/6 melon value becomes 150.
     */
    public static final int COMPOST_MAX = 1000;
    /** 30 seconds to turn a full compost pile into dirt (1.1.1 tuning). */
    public static final int COMPOST_TICKS = 600;
    /** Wood barrels reject fluids hotter than this (water 300 K, lava 1300 K). */
    public static final int WOOD_MAX_TEMPERATURE = 433;
    /** Rain adds water in batches, so the block update is not sent every tick. */
    private static final int RAIN_BATCH = 20;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public int fill(final FluidStack resource, final FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            // A ready dirt block, a solid product, or an active compost pile owns
            // the barrel; pipes must not overwrite that state behind the player's
            // back. The right-click path checks these first.
            if (BarrelBlockEntity.this.dirtReady || BarrelBlockEntity.this.hasSolid()
                    || BarrelBlockEntity.this.compostProgress > 0) {
                return 0;
            }
            final FluidStack existing = this.getFluid();
            // Full tank + the opposite fluid = solidification, even in a wooden
            // barrel: the lava is consumed immediately and never stored.
            if (!existing.isEmpty() && this.getFluidAmount() >= this.getCapacity()
                    && existing.getFluid() != resource.getFluid()) {
                final ItemStack result = BarrelRecipes.resultForFluid(
                        existing.getFluid(), resource.getFluid());
                if (!result.isEmpty()) {
                    if (action.execute()) {
                        BarrelBlockEntity.this.setSolid(result);
                    }
                    return resource.getAmount();
                }
            }
            if (!BarrelBlockEntity.this.canStore(resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Override
        protected void onContentsChanged() {
            BarrelBlockEntity.this.setChanged();
            BarrelBlockEntity.this.sync();
        }
    };


    private int compostProgress;
    private int compostTicks;
    private boolean dirtReady;
    /**
     * What was put in, with the points each item contributed. The renderer
     * averages the item colours by these weights so a pile of rotten flesh
     * looks reddish-brown and leaves push the tint towards green.
     */
    private final List<CompostIngredient> compostIngredients = new ArrayList<>();
    /** A finished barrel block waiting to be taken out; empty when none. */
    private ItemStack solid = ItemStack.EMPTY;
    /** Fractional rain accumulation, reset when it cannot be added. */
    private float rainBuffer;
    /**
     * Witch-water spore progress: 0 = not sporing. Reaching {@link #COMPOST_TICKS}
     * turns the full water tank into witch water (M6).
     */
    private int sporeProgress;
    /** Doll rules: entity to spawn when cooking finishes, or null. */
    private ResourceLocation pendingEntity;
    private final java.util.List<Item> peacefulDrops = new java.util.ArrayList<>();
    private int cookTicks;
    /**
     * Delayed fluid conversion (sugar water + dye -> concentrated honey). The
     * source fluid is kept in the tank while the timer runs; the conversion is
     * skipped if the tank changed in the meantime.
     */
    @Nullable
    private Fluid pendingFluidFrom;
    @Nullable
    private Fluid pendingFluidTo;
    private int pendingFluidTicks;

    public BarrelBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.BARREL.get(), pos, state);
    }

    public FluidTank getTank() {
        return this.tank;
    }

    public int getCompostProgress() {
        return this.compostProgress;
    }

    public int getCompostTicks() {
        return this.compostTicks;
    }

    /** Snapshot of the compost pile, used by the client renderer for the tint. */
    public List<CompostIngredient> getCompostIngredients() {
        return List.copyOf(this.compostIngredients);
    }

    public boolean isDirtReady() {
        return this.dirtReady;
    }

    /** The block waiting inside the barrel (cobblestone, obsidian, ...), or empty. */
    public ItemStack getSolid() {
        return this.solid;
    }

    public boolean hasSolid() {
        return !this.solid.isEmpty();
    }

    /** 0 while filling, then 0..1 across the composting timer, 1 in the DIRT state. */
    public float getCompostTransition() {
        if (this.dirtReady) {
            return 1.0F;
        }
        if (this.compostProgress < COMPOST_MAX || this.compostTicks <= 0) {
            return 0.0F;
        }
        return 1.0F - (this.compostTicks / (float) COMPOST_TICKS);
    }

    /** How full the compost heap is, 0..1. */
    public float getCompostFill() {
        return this.dirtReady ? 1.0F : this.compostProgress / (float) COMPOST_MAX;
    }

    /** Stone barrels hold lava; wooden barrels are limited to {@link #WOOD_MAX_TEMPERATURE}. */
    public boolean allowsHotFluids() {
        return this.getBlockState().is(ModBlocks.STONE_BARREL.get());
    }

    private boolean canStore(final FluidStack stack) {
        return this.allowsHotFluids()
                || stack.getFluidType().getTemperature() <= WOOD_MAX_TEMPERATURE;
    }

    /** A full barrel accepts the add-on from {@link BarrelRecipes} (item or second fluid). */
    private BarrelRecipes.Recipe itemMixRule(final ItemStack held) {
        if (this.tank.getFluidAmount() < CAPACITY || this.tank.getFluid().isEmpty()) {
            return null;
        }
        return BarrelRecipes.ruleForItem(this.tank.getFluid().getFluid(), held);
    }

    /**
     * A drain rule works on a partial barrel (at least {@code drain} mB), unlike
     * the normal "tank must be full" item rules.
     */
    @Nullable
    private BarrelRecipes.Recipe drainRule(final ItemStack held) {
        if (this.tank.getFluidAmount() <= 0 || this.tank.getFluid().isEmpty()) {
            return null;
        }
        final BarrelRecipes.Recipe rule =
                BarrelRecipes.ruleForItem(this.tank.getFluid().getFluid(), held);
        return rule != null && rule.drainsFluid() && this.tank.getFluidAmount() >= rule.drain()
                ? rule : null;
    }

    private boolean wouldSolidify(final FluidStack incoming) {
        return this.tank.getFluidAmount() >= CAPACITY
                && !this.tank.getFluid().isEmpty()
                && !BarrelRecipes.resultForFluid(
                        this.tank.getFluid().getFluid(), incoming.getFluid()).isEmpty();
    }

    private static boolean isFluidContainer(final ItemStack stack) {
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    /** Whether the held stack can do anything at all (used for client prediction). */
    public boolean canUse(final ItemStack stack) {
        if (this.dirtReady || this.hasSolid()) {
            return true;
        }
        if (isFluidContainer(stack)) {
            return this.compostProgress == 0;
        }
        if (this.drainRule(stack) != null) {
            return true;
        }
        if (this.itemMixRule(stack) != null) {
            return true;
        }
        if (stack.is(Items.MUSHROOM_STEW) && this.isFullWater()) {
            return true;
        }
        // A compostable item is always "handled": when the barrel is full the
        // use() path returns CONSUME, which must block the block placement that
        // would otherwise put the leaf on top of the barrel.
        return stack.is(ModTags.Items.COMPOSTABLE);
    }

    /** True while a pipe may add compost: empty tank, no solid/dirt, room left. */
    public boolean canAcceptCompost() {
        return !this.dirtReady && !this.hasSolid()
                && this.tank.getFluidAmount() == 0
                && this.compostProgress < COMPOST_MAX;
    }

    public boolean canInsertCompost(final ItemStack stack) {
        return this.canAcceptCompost() && stack.is(ModTags.Items.COMPOSTABLE);
    }

    /**
     * Pipe/hopper insertion: credits up to {@code maxCount} compostable items.
     * Returns how many items were accepted; the caller shrinks its own stack.
     * The last accepted item may be credited partially, exactly like a hand use.
     */
    public int insertCompost(final ItemStack stack, final int maxCount, final boolean simulate) {
        if (maxCount <= 0 || !this.canInsertCompost(stack)) {
            return 0;
        }
        final int value = Math.max(1, CompostRegistry.valueOf(stack));
        final int room = COMPOST_MAX - this.compostProgress;
        final int accepted = Math.min(maxCount, (room + value - 1) / value);
        if (accepted <= 0) {
            return 0;
        }
        if (simulate) {
            return accepted;
        }
        final int credited = Math.min(room, accepted * value);
        this.compostProgress += credited;
        this.addCompostIngredient(stack.getItem(), credited);
        if (this.compostProgress >= COMPOST_MAX) {
            this.compostTicks = COMPOST_TICKS;
        }
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_FILL,
                    SoundSource.BLOCKS, 1.0F, 0.8F + this.level.random.nextFloat() * 0.4F);
        }
        this.setChanged();
        this.sync();
        return accepted;
    }

    /** The ready dirt or solid product, without removing it (pipes peek here). */
    public ItemStack getOutputStack() {
        if (this.dirtReady) {
            return new ItemStack(Items.DIRT);
        }
        return this.solid.isEmpty() ? ItemStack.EMPTY : this.solid.copy();
    }

    /** Pipe/hopper extraction: takes the dirt or solid product, or empty. */
    public ItemStack extractOutput(final boolean simulate) {
        final ItemStack out = this.getOutputStack();
        if (out.isEmpty() || simulate) {
            return out;
        }
        if (this.dirtReady) {
            this.dirtReady = false;
            this.clearCompost();
        } else {
            this.solid = ItemStack.EMPTY;
        }
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        this.setChanged();
        this.sync();
        return out;
    }

    /** Server-side interaction. */
    public InteractionResult use(final Player player, final InteractionHand hand) {
        if (this.dirtReady) {
            this.extractDirt(player);
            return InteractionResult.SUCCESS;
        }
        if (this.hasSolid()) {
            this.extractSolid(player);
            return InteractionResult.SUCCESS;
        }

        final ItemStack stack = player.getItemInHand(hand);

        // Drain rules work on partial barrels (glass bottle + concentrated honey).
        final BarrelRecipes.Recipe drainRule = this.drainRule(stack);
        if (drainRule != null) {
            return this.drainFluid(player, stack, drainRule);
        }

        // Item rules win over the generic fluid-container path. A milk bucket is
        // both an item and (when Forge's milk fluid is enabled) a fluid handler,
        // but the classic rule is "milk poured into a full water barrel".
        final BarrelRecipes.Recipe rule = this.itemMixRule(stack);
        if (rule != null) {
            final ItemStack remainder = remainderFor(stack);
            if (!player.isCreative()) {
                stack.shrink(1);
                giveRemainder(player, remainder);
            }
            if (rule.producesFluid()) {
                if (rule.delayTicks() > 0) {
                    this.startFluidConversion(rule);
                } else {
                    this.replaceFluid(rule.resultFluid());
                }
            } else if (rule.spawnsEntity()) {
                this.startCooking(rule);
            } else {
                this.setSolid(rule.result().copy());
            }
            return InteractionResult.SUCCESS;
        }

        if (isFluidContainer(stack)) {
            if (this.compostProgress > 0) {
                return InteractionResult.PASS;
            }
            final FluidStack contained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (!contained.isEmpty() && !this.canStore(contained) && !this.wouldSolidify(contained)) {
                player.displayClientMessage(
                        Component.translatable("message.exnihilosatellitium.barrel.hot_fluid"), true);
                return InteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(player, hand, this.tank)) {
                this.setChanged();
                this.sync();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // Mushroom stew seeds a full water barrel (classic "throw it in" recipe).
        if (stack.is(Items.MUSHROOM_STEW) && this.isFullWater()) {
            if (!player.isCreative()) {
                stack.shrink(1);
                final ItemStack bowl = new ItemStack(Items.BOWL);
                if (!player.getInventory().add(bowl)) {
                    player.drop(bowl, false);
                }
            }
            this.sporeProgress = Math.max(1, this.sporeProgress);
            if (this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW,
                        SoundSource.BLOCKS, 0.8F, 0.6F);
            }
            this.setChanged();
            this.sync();
            return InteractionResult.SUCCESS;
        }

        if (this.tank.getFluidAmount() > 0 || this.compostProgress >= COMPOST_MAX) {
            // The barrel owns compostable items even when it cannot take more:
            // CONSUME blocks the vanilla block placement, PASS lets other blocks
            // be placed on top as usual.
            return stack.is(ModTags.Items.COMPOSTABLE)
                    ? InteractionResult.CONSUME
                    : InteractionResult.PASS;
        }
        if (!stack.is(ModTags.Items.COMPOSTABLE)) {
            return InteractionResult.PASS;
        }

        final Item item = stack.getItem();
        final int value = Math.max(1, CompostRegistry.valueOf(stack));
        // A partially filled barrel only credits the room that is left, so the
        // recorded weights always add up to COMPOST_MAX.
        final int added = Math.min(value, COMPOST_MAX - this.compostProgress);
        if (added <= 0) {
            return InteractionResult.PASS;
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        this.compostProgress += added;
        this.addCompostIngredient(item, added);
        if (this.compostProgress >= COMPOST_MAX) {
            this.compostTicks = COMPOST_TICKS;
        }

        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_FILL,
                    SoundSource.BLOCKS, 1.0F, 0.8F + this.level.random.nextFloat() * 0.4F);
        }

        this.setChanged();
        this.sync();
        return InteractionResult.SUCCESS;
    }


    /** Empty bucket for a milk bucket, or the item's own crafting remainder. */
    private static ItemStack remainderFor(final ItemStack used) {
        final ItemStack remainder = used.getCraftingRemainingItem();
        if (!remainder.isEmpty()) {
            return remainder;
        }
        return used.is(Items.MILK_BUCKET) ? new ItemStack(Items.BUCKET) : ItemStack.EMPTY;
    }

    private static void giveRemainder(final Player player, final ItemStack remainder) {
        if (remainder.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(remainder)) {
            player.drop(remainder, false);
        }
    }

    /** Merges a new ingredient into the pile, keeping the "one entry per item" shape. */
    private void addCompostIngredient(final Item item, final int points) {
        for (int i = 0; i < this.compostIngredients.size(); i++) {
            final CompostIngredient existing = this.compostIngredients.get(i);
            if (existing.item() == item) {
                this.compostIngredients.set(i, new CompostIngredient(item, existing.points() + points));
                return;
            }
        }
        this.compostIngredients.add(new CompostIngredient(item, points));
    }

    private void clearCompost() {
        this.compostProgress = 0;
        this.compostTicks = 0;
        this.compostIngredients.clear();
    }

    /** Turns the tank contents into a block; the fluid is consumed. */
    private void setSolid(final ItemStack result) {
        this.solid = result.copyWithCount(1);
        this.tank.setFluid(FluidStack.EMPTY);
        this.dirtReady = false;
        this.clearCompost();
        this.setChanged();
        this.sync();
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.LAVA_EXTINGUISH,
                    SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    /** Drops the solid product and ready dirt on break; fluids have no item form. */
    public void dropContentsOnBreak(final Level level, final BlockPos pos) {
        if (this.dirtReady) {
            Block.popResource(level, pos, new ItemStack(Items.DIRT));
            this.dirtReady = false;
        }
        if (!this.solid.isEmpty()) {
            Block.popResource(level, pos, this.solid.copy());
            this.solid = ItemStack.EMPTY;
        }
    }

    private void extractSolid(final Player player) {
        final ItemStack out = this.solid.copy();
        if (!player.getInventory().add(out)) {
            player.drop(out, false);
        }
        this.solid = ItemStack.EMPTY;
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        this.setChanged();
        this.sync();
    }

    private void extractDirt(final Player player) {
        final ItemStack dirt = new ItemStack(Items.DIRT);
        if (!player.getInventory().add(dirt)) {
            player.drop(dirt, false);
        }

        this.dirtReady = false;
        this.clearCompost();

        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_EMPTY,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        this.setChanged();
        this.sync();
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final BarrelBlockEntity barrel) {
        if (barrel.tickCooking(level, pos)) {
            return;
        }
        if (barrel.dirtReady || barrel.hasSolid()) {
            return;
        }

        if (barrel.compostProgress >= COMPOST_MAX) {
            if (barrel.compostTicks > 0) {
                barrel.compostTicks--;
                // Keep the client's colour transition roughly in sync.
                if (barrel.compostTicks % 10 == 0) {
                    barrel.setChanged();
                    barrel.sync();
                }
                // A thin trail of compost particles makes the 30s transition visible
                // even when the colour shift is subtle.
                if (barrel.compostTicks % 40 == 0 && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                            pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                            2, 0.25D, 0.05D, 0.25D, 0.0D);
                }
                if (barrel.compostTicks == 0) {
                    barrel.finishCompost();
                }
                return;
            }

            barrel.finishCompost();
            return;
        }

        if (barrel.tickSpores(level, pos)) {
            return;
        }

        // A full barrel with the opposite fluid directly above solidifies
        // (the classic "put a lava/water source on top" setup). Flowing water
        // counts too: it is the same fluid as its source for the recipe lookup.
        if (barrel.tank.getFluidAmount() >= CAPACITY && !barrel.tank.getFluid().isEmpty()) {
            final Fluid above = normalizeFluid(level.getFluidState(pos.above()).getType());
            if (!above.isSame(Fluids.EMPTY)) {
                final ItemStack result = BarrelRecipes.resultForFluid(
                        barrel.tank.getFluid().getFluid(), above);
                if (!result.isEmpty()) {
                    barrel.setSolid(result);
                    return;
                }
            }
        }

        barrel.tickConcentratedHoney(level, pos);
        barrel.tickRain(level, pos);
    }

    /**
     * Concentrated honey attracts bees: every 100 ticks (5 s) there is a 1/6
     * chance to add one bee, capped at 5 within a 6-block radius. The barrel
     * does not need to be full; any amount of concentrated honey works.
     */
    private void tickConcentratedHoney(final Level level, final BlockPos pos) {
        if (level.isClientSide || this.tank.getFluidAmount() <= 0
                || this.tank.getFluid().getFluid() != ModFluids.CONCENTRATED_HONEY.get()) {
            return;
        }
        if (level.getGameTime() % 100L != 0L || level.random.nextInt(6) != 0) {
            return;
        }
        final AABB area = new AABB(pos).inflate(6.0D);
        if (level.getEntitiesOfClass(Bee.class, area).size() >= 5) {
            return;
        }
        final Bee bee = EntityType.BEE.create(level);
        if (bee == null) {
            return;
        }
        final double x = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 3.0D;
        final double y = pos.getY() + 1.0D + level.random.nextDouble() * 1.5D;
        final double z = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 3.0D;
        bee.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        bee.setPersistenceRequired();
        if (level.noCollision(bee)) {
            level.addFreshEntity(bee);
        }
    }

    /** Flowing water/lava map to their source fluid so the recipe table matches. */
    private static Fluid normalizeFluid(final Fluid fluid) {
        return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
    }

    /** Glass bottle + concentrated honey: drain 250 mB and hand back a honey bottle. */
    private InteractionResult drainFluid(final Player player, final ItemStack stack,
                                         final BarrelRecipes.Recipe rule) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        this.tank.drain(rule.drain(), FluidAction.EXECUTE);
        final ItemStack out = rule.result().copy();
        if (!player.getInventory().add(out)) {
            player.drop(out, false);
        }
        this.setChanged();
        this.sync();
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BOTTLE_FILL,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    /** Immediate fluid replacement (sugar + water -> sugar water). */
    private void replaceFluid(@Nullable final Fluid to) {
        if (to == null) {
            return;
        }
        final int amount = this.tank.getFluidAmount();
        if (amount <= 0) {
            return;
        }
        this.tank.setFluid(new FluidStack(to, amount));
        this.setChanged();
        this.sync();
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }

    /** Delayed fluid replacement (sugar water + dye -> concentrated honey). */
    private void startFluidConversion(final BarrelRecipes.Recipe rule) {
        if (rule.resultFluid() == null || this.tank.getFluid().isEmpty()) {
            return;
        }
        this.pendingFluidFrom = this.tank.getFluid().getFluid();
        this.pendingFluidTo = rule.resultFluid();
        this.pendingFluidTicks = Math.max(1, rule.delayTicks());
        this.setChanged();
        this.sync();
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, 0.8F, 1.4F);
        }
    }

    /** @return true while a fluid conversion timer is running */
    private boolean tickPendingFluid(final Level level, final BlockPos pos) {
        if (this.pendingFluidTo == null) {
            return false;
        }
        if (this.pendingFluidTicks > 0) {
            this.pendingFluidTicks--;
            if (this.pendingFluidTicks % 20 == 0) {
                this.setChanged();
                this.sync();
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FALLING_HONEY,
                            pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                            3, 0.25D, 0.1D, 0.25D, 0.0D);
                }
            }
            if (this.pendingFluidTicks == 0) {
                this.finishFluidConversion(level);
            }
            return true;
        }
        this.finishFluidConversion(level);
        return true;
    }

    private void finishFluidConversion(final Level level) {
        if (!level.isClientSide && this.pendingFluidTo != null) {
            final int amount = this.tank.getFluidAmount();
            if (amount > 0 && this.pendingFluidFrom != null
                    && this.tank.getFluid().getFluid() == this.pendingFluidFrom) {
                this.tank.setFluid(new FluidStack(this.pendingFluidTo, amount));
                level.playSound(null, this.worldPosition, SoundEvents.BOTTLE_FILL,
                        SoundSource.BLOCKS, 1.0F, 0.8F);
            }
        }
        this.pendingFluidFrom = null;
        this.pendingFluidTo = null;
        this.pendingFluidTicks = 0;
        this.setChanged();
        this.sync();
    }

    /** Dolls: consume the fluid and start a cooking timer that spawns an entity. */
    private void startCooking(final BarrelRecipes.Recipe rule) {
        this.pendingEntity = rule.entity();
        this.peacefulDrops.clear();
        this.peacefulDrops.addAll(rule.peacefulDrops());
        this.cookTicks = rule.cookTicks();
        this.tank.setFluid(FluidStack.EMPTY);
        this.dirtReady = false;
        this.clearCompost();
        this.setChanged();
        this.sync();
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, 1.0F, 0.6F);
        }
        // Dolls convert on the right-click: peaceful drops the loot, otherwise the
        // mob spawns right away. The cook machinery below only serves old saves
        // that still carry a pending CookEntity.
        if (this.level != null && !this.level.isClientSide) {
            this.finishCooking(this.level);
        }
    }

    /** @return true while cooking (the normal barrel tick must not run) */
    private boolean tickCooking(final Level level, final BlockPos pos) {
        if (this.tickPendingFluid(level, pos)) {
            return true;
        }
        if (this.pendingEntity == null) {
            return false;
        }
        if (this.cookTicks > 0) {
            this.cookTicks--;
            if (this.cookTicks % 20 == 0 && level instanceof ServerLevel serverLevel) {
                final boolean blaze = this.pendingEntity.getPath().contains("blaze");
                serverLevel.sendParticles(blaze ? ParticleTypes.FLAME : ParticleTypes.PORTAL,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        6, 0.3D, 0.2D, 0.3D, 0.01D);
            }
            return true;
        }

        this.finishCooking(level);
        return true;
    }

    /** Spawns the entity, or drops the peaceful loot, and empties the barrel. */
    private void finishCooking(final Level level) {
        if (!level.isClientSide) {
            if (level.getDifficulty() != Difficulty.PEACEFUL) {
                final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(this.pendingEntity).orElse(null);
                if (type != null) {
                    final Entity entity = type.create(level);
                    if (entity != null) {
                        entity.moveTo(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1.0D,
                                this.worldPosition.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                        level.addFreshEntity(entity);
                    }
                }
            } else if (!this.peacefulDrops.isEmpty()) {
                final Item drop = this.peacefulDrops.get(level.random.nextInt(this.peacefulDrops.size()));
                Block.popResource(level, this.worldPosition.above(), new ItemStack(drop));
            }
            level.playSound(null, this.worldPosition, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, 1.0F, 0.4F);
        }
        this.pendingEntity = null;
        this.peacefulDrops.clear();
        this.cookTicks = 0;
        this.setChanged();
        this.sync();
    }

    /** True while a doll is cooking in this barrel (synced to the client). */
    public boolean isCooking() {
        return this.pendingEntity != null || this.pendingFluidTo != null;
    }

    /** True while the barrel is converting one fluid into another (honey chain). */
    public boolean isConvertingFluid() {
        return this.pendingFluidTo != null;
    }

    /** Seconds left before the entity spawns / the fluid conversion finishes, for Jade. */
    public int getCookSeconds() {
        if (this.pendingFluidTo != null) {
            return (this.pendingFluidTicks + 19) / 20;
        }
        return (this.cookTicks + 19) / 20;
    }

    /** True when the tank holds a full 1000 mB of plain water. */
    private boolean isFullWater() {
        return this.tank.getFluidAmount() >= CAPACITY
                && this.tank.getFluid().getFluid() == Fluids.WATER;
    }

    /** Mycelium in the 3x3x3 around the barrel, like the original getNearbyBlocks. */
    private static int countMycelium(final Level level, final BlockPos pos) {
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (level.getBlockState(pos.offset(x, y, z)).is(Blocks.MYCELIUM)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Witch water: a full water barrel next to mycelium (or seeded with mushroom
     * stew) ferments. Original timing: {@code timer += 1 + mycelium / 2} per tick
     * against MAX_COMPOSTING_TIME, so one mycelium takes 50 s.
     *
     * @return true when the tank became witch water this tick
     */
    private boolean tickSpores(final Level level, final BlockPos pos) {
        if (this.compostProgress > 0 || this.hasSolid() || this.dirtReady) {
            return false;
        }
        if (!this.isFullWater()) {
            this.sporeProgress = 0;
            return false;
        }
        final int mycelium = countMycelium(level, pos);
        if (this.sporeProgress <= 0 && mycelium <= 0) {
            return false;
        }
        this.sporeProgress += 1 + mycelium / 2;
        if (this.sporeProgress < COMPOST_TICKS) {
            return false;
        }

        this.sporeProgress = 0;
        this.tank.setFluid(new FluidStack(ModFluids.WITCH_WATER.get(), CAPACITY));
        this.setChanged();
        this.sync();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                    16, 0.35D, 0.15D, 0.35D, 0.02D);
        }
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.5F);
        return true;
    }

    /** Open-to-sky barrels catch rain; ~100 s at rainfall 0.5 for a full barrel. */
    private void tickRain(final Level level, final BlockPos pos) {
        if (this.compostProgress > 0 || this.tank.getFluidAmount() >= CAPACITY) {
            this.rainBuffer = 0.0F;
            return;
        }
        if (!level.isRainingAt(pos.above())) {
            this.rainBuffer = 0.0F;
            return;
        }

        final float rainfall = level.getBiome(pos).value().getModifiedClimateSettings().downfall();
        if (rainfall <= 0.0F) {
            return;
        }

        this.rainBuffer += rainfall;
        if (this.rainBuffer < RAIN_BATCH) {
            return;
        }
        this.rainBuffer -= RAIN_BATCH;
        final int added = this.tank.fill(new FluidStack(Fluids.WATER, RAIN_BATCH), FluidAction.EXECUTE);
        if (added <= 0) {
            this.rainBuffer = 0.0F;
        }
    }

    private void finishCompost() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        this.dirtReady = true;
        this.compostTicks = 0;
        this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_READY,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 1.0D,
                    this.worldPosition.getZ() + 0.5D,
                    12, 0.3D, 0.1D, 0.3D, 0.01D);
        }
        this.setChanged();
        this.sync();
    }

    public int getComparatorOutput() {
        if (this.dirtReady || this.hasSolid() || this.pendingEntity != null
                || this.pendingFluidTo != null) {
            return 15;
        }
        final int fluid = this.tank.getFluidAmount() * 15 / this.tank.getCapacity();
        final int compost = this.compostProgress * 15 / COMPOST_MAX;
        return Math.max(fluid, compost);
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Nullable
    private static Fluid readFluid(final CompoundTag tag, final String key) {
        if (!tag.contains(key)) {
            return null;
        }
        final ResourceLocation id = ResourceLocation.tryParse(tag.getString(key));
        return id == null ? null : BuiltInRegistries.FLUID.getOptional(id).orElse(null);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(registries, tag);
        tag.putInt("Compost", this.compostProgress);
        tag.putInt("CompostTicks", this.compostTicks);
        if (!this.compostIngredients.isEmpty()) {
            final net.minecraft.nbt.ListTag ingredients = new net.minecraft.nbt.ListTag();
            for (final CompostIngredient ingredient : this.compostIngredients) {
                final CompoundTag entry = new CompoundTag();
                entry.putString("id", BuiltInRegistries.ITEM.getKey(ingredient.item()).toString());
                entry.putInt("points", ingredient.points());
                ingredients.add(entry);
            }
            tag.put("CompostItems", ingredients);
        }
        tag.putBoolean("DirtReady", this.dirtReady);
        tag.putInt("Spore", this.sporeProgress);
        if (this.pendingEntity != null) {
            tag.putString("CookEntity", this.pendingEntity.toString());
        }
        if (!this.peacefulDrops.isEmpty()) {
            final net.minecraft.nbt.ListTag drops = new net.minecraft.nbt.ListTag();
            for (final Item item : this.peacefulDrops) {
                drops.add(net.minecraft.nbt.StringTag.valueOf(
                        BuiltInRegistries.ITEM.getKey(item).toString()));
            }
            tag.put("PeacefulDrops", drops);
        }
        tag.putInt("CookTicks", this.cookTicks);
        if (this.pendingFluidFrom != null) {
            tag.putString("PendingFluidFrom",
                    BuiltInRegistries.FLUID.getKey(this.pendingFluidFrom).toString());
        }
        if (this.pendingFluidTo != null) {
            tag.putString("PendingFluidTo",
                    BuiltInRegistries.FLUID.getKey(this.pendingFluidTo).toString());
        }
        tag.putInt("PendingFluidTicks", this.pendingFluidTicks);
        if (this.hasSolid()) {
            tag.put("Solid", this.solid.save(registries));
        }
        // 0.1.x used a 0..100 scale and a 600-tick timer. New saves record the
        // scale so old worlds can be migrated without guessing.
        tag.putInt("CompostScale", COMPOST_MAX);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(registries, tag);
        this.compostProgress = tag.getInt("Compost");
        this.compostTicks = tag.getInt("CompostTicks");
        this.compostIngredients.clear();
        if (tag.contains("CompostItems")) {
            for (final net.minecraft.nbt.Tag element
                    : tag.getList("CompostItems", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                final CompoundTag entry = (CompoundTag) element;
                final ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
                final int points = entry.getInt("points");
                if (id == null || points <= 0) {
                    continue;
                }
                BuiltInRegistries.ITEM.getOptional(id)
                        .filter(item -> item != Items.AIR)
                        .ifPresent(item -> this.compostIngredients.add(
                                new CompostIngredient(item, points)));
            }
        }
        this.dirtReady = tag.getBoolean("DirtReady");
        this.sporeProgress = tag.getInt("Spore");
        this.pendingEntity = tag.contains("CookEntity")
                ? ResourceLocation.tryParse(tag.getString("CookEntity")) : null;
        this.peacefulDrops.clear();
        if (tag.contains("PeacefulDrops")) {
            for (final net.minecraft.nbt.Tag element : tag.getList("PeacefulDrops", net.minecraft.nbt.Tag.TAG_STRING)) {
                BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(element.getAsString()))
                        .ifPresent(this.peacefulDrops::add);
            }
        }
        this.cookTicks = tag.getInt("CookTicks");
        this.pendingFluidFrom = readFluid(tag, "PendingFluidFrom");
        this.pendingFluidTo = readFluid(tag, "PendingFluidTo");
        this.pendingFluidTicks = tag.getInt("PendingFluidTicks");
        if (this.pendingFluidTo == null) {
            this.pendingFluidFrom = null;
            this.pendingFluidTicks = 0;
        }
        this.solid = tag.contains("Solid")
                ? ItemStack.parseOptional(registries, tag.getCompound("Solid"))
                : ItemStack.EMPTY;

        if (!tag.contains("CompostScale")) {
            // Legacy barrel: 0..100 progress and a 600-tick timer.
            this.compostProgress = Math.min(COMPOST_MAX, this.compostProgress * 10);
            this.compostTicks = this.compostTicks * COMPOST_TICKS / 600;
        } else {
            final int storedScale = tag.getInt("CompostScale");
            if (storedScale > 0 && storedScale != COMPOST_MAX) {
                this.compostProgress = Math.min(COMPOST_MAX,
                        Math.round(this.compostProgress * (float) COMPOST_MAX / storedScale));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
