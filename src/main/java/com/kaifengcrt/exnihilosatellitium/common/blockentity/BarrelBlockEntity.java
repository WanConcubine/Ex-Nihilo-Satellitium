package com.kaifengcrt.exnihilosatellitium.common.blockentity;

import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import com.kaifengcrt.exnihilosatellitium.common.tag.ModTags;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

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

    public static final int CAPACITY = 1000;
    /**
     * Compost capacity. Ex Nihilo Omnia uses a 1000 mB table, so 0.125
     * saplings become 125 and the 1/6 melon value becomes 150.
     */
    public static final int COMPOST_MAX = 1000;
    /** 50 seconds, matching the original Ex Nihilo {@code MAX_COMPOSTING_TIME}. */
    public static final int COMPOST_TICKS = 1000;
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
    /** A finished barrel block waiting to be taken out; empty when none. */
    private ItemStack solid = ItemStack.EMPTY;
    /** Fractional rain accumulation, reset when it cannot be added. */
    private float rainBuffer;
    /**
     * Witch-water spore progress: 0 = not sporing. Reaching {@link #COMPOST_TICKS}
     * turns the full water tank into witch water (M6).
     */
    private int sporeProgress;

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
    private ItemStack itemMixResult(final ItemStack held) {
        if (this.tank.getFluidAmount() < CAPACITY || this.tank.getFluid().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return BarrelRecipes.resultForItem(this.tank.getFluid().getFluid(), held);
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
        if (!this.itemMixResult(stack).isEmpty()) {
            return true;
        }
        if (stack.is(Items.MUSHROOM_STEW) && this.isFullWater()) {
            return true;
        }
        return this.tank.getFluidAmount() == 0
                && this.compostProgress < COMPOST_MAX
                && stack.is(ModTags.Items.COMPOSTABLE);
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

        final ItemStack mixed = this.itemMixResult(stack);
        if (!mixed.isEmpty()) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            this.setSolid(mixed);
            return InteractionResult.SUCCESS;
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
            return InteractionResult.PASS;
        }
        if (!stack.is(ModTags.Items.COMPOSTABLE)) {
            return InteractionResult.PASS;
        }

        final int value = Math.max(1, CompostRegistry.valueOf(stack));
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        this.compostProgress = Math.min(COMPOST_MAX, this.compostProgress + value);
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

    /** Turns the tank contents into a block; the fluid is consumed. */
    private void setSolid(final ItemStack result) {
        this.solid = result.copyWithCount(1);
        this.tank.setFluid(FluidStack.EMPTY);
        this.dirtReady = false;
        this.compostProgress = 0;
        this.compostTicks = 0;
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
        this.compostProgress = 0;
        this.compostTicks = 0;

        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_EMPTY,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        this.setChanged();
        this.sync();
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final BarrelBlockEntity barrel) {
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
        // (the classic "put a lava/water source on top" setup).
        if (barrel.tank.getFluidAmount() >= CAPACITY && !barrel.tank.getFluid().isEmpty()) {
            final Fluid above = level.getFluidState(pos.above()).getType();
            if (!above.isSame(Fluids.EMPTY)) {
                final ItemStack result = BarrelRecipes.resultForFluid(
                        barrel.tank.getFluid().getFluid(), above);
                if (!result.isEmpty()) {
                    barrel.setSolid(result);
                    return;
                }
            }
        }

        barrel.tickRain(level, pos);
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
        if (this.dirtReady || this.hasSolid()) {
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

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(registries, tag);
        tag.putInt("Compost", this.compostProgress);
        tag.putInt("CompostTicks", this.compostTicks);
        tag.putBoolean("DirtReady", this.dirtReady);
        tag.putInt("Spore", this.sporeProgress);
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
        this.dirtReady = tag.getBoolean("DirtReady");
        this.sporeProgress = tag.getInt("Spore");
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
