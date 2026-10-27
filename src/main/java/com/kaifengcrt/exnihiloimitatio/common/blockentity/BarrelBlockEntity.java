package com.kaifengcrt.exnihiloimitatio.common.blockentity;

import com.kaifengcrt.exnihiloimitatio.common.block.BarrelBlock;
import com.kaifengcrt.exnihiloimitatio.common.compost.CompostRegistry;
import com.kaifengcrt.exnihiloimitatio.common.tag.ModTags;
import com.kaifengcrt.exnihiloimitatio.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
 * <p>Composting only works while the tank is empty. When the progress reaches
 * {@link #COMPOST_MAX}, a timer starts; once it expires the barrel turns into a
 * dirt block (the barrel is consumed).</p>
 */
public class BarrelBlockEntity extends BlockEntity {

    public static final int CAPACITY = 1000;
    public static final int COMPOST_MAX = 100;
    public static final int COMPOST_TICKS = 600;

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

    private static boolean isFluidContainer(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    /** Whether the held stack can do anything at all (used for client prediction). */
    public boolean canUse(final ItemStack stack) {
        if (isFluidContainer(stack)) {
            return this.compostProgress == 0;
        }
        return this.tank.getFluidAmount() == 0
                && this.compostProgress < COMPOST_MAX
                && stack.is(ModTags.Items.COMPOSTABLE);
    }

    /** Server-side interaction. */
    public InteractionResult use(final Player player, final InteractionHand hand) {
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

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final BarrelBlockEntity barrel) {
        if (barrel.compostProgress < COMPOST_MAX) {
            return;
        }

        if (barrel.compostTicks > 0) {
            barrel.compostTicks--;
            if (barrel.compostTicks % 20 == 0) {
                barrel.setChanged();
            }
            return;
        }

        barrel.finishCompost();
    }

    private void finishCompost() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (!(this.level.getBlockState(this.worldPosition).getBlock() instanceof BarrelBlock)) {
            return;
        }

        this.level.setBlockAndUpdate(this.worldPosition, Blocks.DIRT.defaultBlockState());
        this.level.playSound(null, this.worldPosition, SoundEvents.COMPOSTER_READY,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public int getComparatorOutput() {
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
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        this.tank.readFromNBT(tag);
        this.compostProgress = tag.getInt("Compost");
        this.compostTicks = tag.getInt("CompostTicks");
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
