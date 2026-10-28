package com.kaifengcrt.exnihiloimitatio.common.blockentity;

import com.kaifengcrt.exnihiloimitatio.common.compost.CompostRegistry;
import com.kaifengcrt.exnihiloimitatio.common.tag.ModTags;
import com.kaifengcrt.exnihiloimitatio.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * Barrel state: a 1000 mB fluid tank plus a compost progress bar.
 *
 * <p>The barrel is never replaced. Composting only works while the tank is
 * empty. When the progress reaches {@link #COMPOST_MAX}, a timer starts; while
 * it runs the renderer blends the compost colour towards dirt. Once it ends the
 * barrel enters the DIRT state; right-clicking extracts one dirt item and
 * resets the barrel.</p>
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

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            BarrelBlockEntity.this.setChanged();
            BarrelBlockEntity.this.sync();
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this.tank);

    private int compostProgress;
    private int compostTicks;
    private boolean dirtReady;

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

    private static boolean isFluidContainer(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    /** Whether the held stack can do anything at all (used for client prediction). */
    public boolean canUse(final ItemStack stack) {
        if (this.dirtReady) {
            return true;
        }
        if (isFluidContainer(stack)) {
            return this.compostProgress == 0;
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

        final ItemStack stack = player.getItemInHand(hand);

        if (isFluidContainer(stack)) {
            if (this.compostProgress > 0) {
                return InteractionResult.PASS;
            }
            if (FluidUtil.interactWithFluidHandler(player, hand, this.tank)) {
                this.setChanged();
                this.sync();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
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
        if (barrel.dirtReady || barrel.compostProgress < COMPOST_MAX) {
            return;
        }

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
        if (this.dirtReady) {
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
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        this.tank.writeToNBT(tag);
        tag.putInt("Compost", this.compostProgress);
        tag.putInt("CompostTicks", this.compostTicks);
        tag.putBoolean("DirtReady", this.dirtReady);
        // 0.1.x used a 0..100 scale and a 600-tick timer. New saves record the
        // scale so old worlds can be migrated without guessing.
        tag.putInt("CompostScale", COMPOST_MAX);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        this.tank.readFromNBT(tag);
        this.compostProgress = tag.getInt("Compost");
        this.compostTicks = tag.getInt("CompostTicks");
        this.dirtReady = tag.getBoolean("DirtReady");

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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, @Nullable final Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return this.fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidHandler.invalidate();
    }
}
