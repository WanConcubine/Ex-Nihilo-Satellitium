package com.kaifengcrt.exnihilosatellitium.common.blockentity;

import com.kaifengcrt.exnihilosatellitium.common.config.ModConfig;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * Crucible state: a configurable fluid tank plus a solid "melt" queue.
 *
 * <p>Insert a meltable block with a right-click (up to the configured melt
 * capacity, default {@link #DEFAULT_MELT_CAPACITY} mB of solid); while the block below is a heat source
 * ({@code crucible_heat/*.json}) the solid is converted into its fluid at
 * {@code speed} mB of solid per tick. The renderer shows the melt material
 * while the solid volume is larger than the fluid volume, and the fluid
 * otherwise (classic Ex Nihilo behaviour).</p>
 */
public class CrucibleBlockEntity extends BlockEntity {

    /** Wooden crucibles melt without a heat source, slowly (classic Creatio). */
    public static final float WOODEN_PASSIVE_SPEED = 0.2F;
    /** Wooden crucibles are not hot-fluid rated; same limit as wooden barrels. */
    public static final int WOOD_MAX_TEMPERATURE = 433;

    /** Default solid (melt) capacity; the live value comes from the config. */
    public static final int DEFAULT_MELT_CAPACITY = 2000;
    private static final int SYNC_INTERVAL = 10;

    private final FluidTank tank = new FluidTank(ModConfig.crucibleCapacity()) {
        @Override
        public int fill(final FluidStack resource, final FluidAction action) {
            if (CrucibleBlockEntity.this.isWooden() && !resource.isEmpty()
                    && resource.getFluidType().getTemperature() > WOOD_MAX_TEMPERATURE) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Override
        protected void onContentsChanged() {
            CrucibleBlockEntity.this.setChanged();
            CrucibleBlockEntity.this.dirty = true;
        }
    };

    /** Solid mB still waiting to melt. */
    private double solidVolume;
    /** Total solid mB of the current queue (for the solid→fluid ratio). */
    private int solidTotal;
    /** Total fluid mB the current queue will produce. */
    private int fluidTotal;
    private double fluidBuffer;
    private Fluid meltFluid = Fluids.EMPTY;
    /** The block being melted; synced to the client for the renderer/Jade. */
    private ItemStack pendingStack = ItemStack.EMPTY;
    private int syncCooldown;
    private boolean dirty;

    public CrucibleBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public FluidTank getTank() {
        return this.tank;
    }

    /** Solid mB still waiting to melt (rounded up for display). */
    public int getPending() {
        return (int) Math.ceil(this.solidVolume);
    }

    public double getSolidVolume() {
        return this.solidVolume;
    }

    /** 0..1 of the configured melt capacity. */
    public float getSolidRatio() {
        return (float) Math.min(1.0D, this.solidVolume / ModConfig.crucibleMeltCapacity());
    }

    /** 0..1 of the configured fluid capacity. */
    public float getFluidRatio() {
        final int capacity = this.tank.getCapacity();
        return capacity <= 0 ? 0.0F : (float) this.tank.getFluidAmount() / (float) capacity;
    }

    /** The renderer shows the melt while it out-volumes the fluid. */
    public boolean isMeltDominant() {
        return this.solidVolume > this.tank.getFluidAmount();
    }

    /** The block currently in the melt queue, or empty. */
    public ItemStack getPendingStack() {
        return this.pendingStack;
    }

    /** True for the wooden variant: passive melting, water only. */
    public boolean isWooden() {
        return this.getBlockState().is(ModBlocks.WOODEN_CRUCIBLE.get());
    }

    public boolean canAccept(final CrucibleRecipes.Melt melt) {
        if (this.isWooden() && melt.fluid() != Fluids.WATER) {
            return false;
        }
        if (!this.tank.getFluid().isEmpty() && this.tank.getFluid().getFluid() != melt.fluid()) {
            return false;
        }
        if (this.solidVolume > 0.0D) {
            if (this.meltFluid != melt.fluid()) {
                return false;
            }
            // One block type at a time: the icon and the melt ratio belong to it.
            if (!this.pendingStack.isEmpty()
                    && this.pendingStack.getItem() != melt.input().asItem()) {
                return false;
            }
        }
        if (this.solidVolume + melt.solidAmount() > ModConfig.crucibleMeltCapacity()) {
            return false;
        }
        return this.tank.getFluidAmount() + this.fluidTotal + melt.amount() <= this.tank.getCapacity();
    }

    /** Right-click: insert a meltable block, otherwise try the held fluid container. */
    public InteractionResult use(final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            final CrucibleRecipes.Melt melt = CrucibleRecipes.meltFor(Block.byItem(stack.getItem()));
            if (melt != null && this.canAccept(melt)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                this.solidVolume += melt.solidAmount();
                this.solidTotal += melt.solidAmount();
                this.fluidTotal += melt.amount();
                this.meltFluid = melt.fluid();
                if (this.pendingStack.isEmpty() || this.pendingStack.getItem() != stack.getItem()) {
                    this.pendingStack = stack.copyWithCount(1);
                }
                this.setChanged();
                this.sync();
                return InteractionResult.SUCCESS;
            }
        }
        if (this.isWooden()) {
            final FluidStack contained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (!contained.isEmpty()
                    && contained.getFluidType().getTemperature() > WOOD_MAX_TEMPERATURE) {
                player.displayClientMessage(
                        Component.translatable("message.exnihilosatellitium.crucible.hot_fluid"), true);
                return InteractionResult.SUCCESS;
            }
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, this.tank)) {
            this.setChanged();
            this.sync();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public int getComparatorOutput() {
        final int capacity = Math.max(1, this.tank.getCapacity());
        return this.tank.getFluidAmount() * 15 / capacity;
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final CrucibleBlockEntity crucible) {
        if (crucible.syncCooldown > 0) {
            crucible.syncCooldown--;
        } else if (crucible.dirty) {
            crucible.dirty = false;
            crucible.sync();
            crucible.syncCooldown = SYNC_INTERVAL;
        }

        final int configured = ModConfig.crucibleCapacity();
        if (crucible.tank.getCapacity() != configured) {
            crucible.tank.setCapacity(configured);
            final FluidStack current = crucible.tank.getFluid();
            if (!current.isEmpty() && current.getAmount() > configured) {
                crucible.tank.setFluid(new FluidStack(current.getFluid(), configured));
            }
            crucible.setChanged();
            crucible.dirty = true;
        }

        if (crucible.solidVolume <= 0.0D || crucible.meltFluid == Fluids.EMPTY
                || crucible.fluidTotal <= 0 || crucible.solidTotal <= 0) {
            return;
        }
        final float speed = crucible.isWooden()
                ? WOODEN_PASSIVE_SPEED
                : CrucibleHeat.speedFor(level.getBlockState(pos.below()))
                        * ModConfig.crucibleHeatMultiplier();
        if (speed <= 0.0F) {
            return;
        }

        final double ratio = (double) crucible.fluidTotal / (double) crucible.solidTotal;
        final int room = crucible.tank.getCapacity() - crucible.tank.getFluidAmount();
        final double solidByRoom = room * (double) crucible.solidTotal / (double) crucible.fluidTotal;
        final double solidStep = Math.min(Math.min(speed, crucible.solidVolume), solidByRoom);
        if (solidStep <= 0.0D) {
            return;
        }
        crucible.fluidBuffer += solidStep * ratio;
        int fluidStep = (int) crucible.fluidBuffer;
        crucible.fluidBuffer -= fluidStep;
        if (fluidStep > 0) {
            final int filled = crucible.tank.fill(
                    new FluidStack(crucible.meltFluid, fluidStep), IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) {
                return;
            }
            crucible.fluidBuffer += fluidStep - filled;
        }
        crucible.solidVolume -= solidStep;
        if (crucible.solidVolume < 1.0E-4D) {
            crucible.solidVolume = 0.0D;
            crucible.solidTotal = 0;
            crucible.fluidTotal = 0;
            crucible.fluidBuffer = 0.0D;
            crucible.meltFluid = Fluids.EMPTY;
            crucible.pendingStack = ItemStack.EMPTY;
        }
        crucible.setChanged();
        crucible.dirty = true;
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    Block.UPDATE_ALL);
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(registries, tag);
        tag.putDouble("SolidVolume", this.solidVolume);
        tag.putInt("SolidTotal", this.solidTotal);
        tag.putInt("FluidTotal", this.fluidTotal);
        tag.putDouble("FluidBuffer", this.fluidBuffer);
        if (this.meltFluid != Fluids.EMPTY) {
            tag.putString("MeltFluid", BuiltInRegistries.FLUID.getKey(this.meltFluid).toString());
        }
        if (!this.pendingStack.isEmpty()) {
            tag.put("PendingStack", this.pendingStack.save(registries));
        }
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(registries, tag);
        this.solidVolume = tag.getDouble("SolidVolume");
        this.solidTotal = tag.getInt("SolidTotal");
        this.fluidTotal = tag.getInt("FluidTotal");
        this.fluidBuffer = tag.getDouble("FluidBuffer");
        this.meltFluid = tag.contains("MeltFluid")
                ? BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(tag.getString("MeltFluid")))
                        .orElse(Fluids.EMPTY)
                : Fluids.EMPTY;
        this.pendingStack = tag.contains("PendingStack")
                ? ItemStack.parseOptional(registries, tag.getCompound("PendingStack"))
                : ItemStack.EMPTY;
        if (this.solidVolume <= 0.0D) {
            this.solidVolume = 0.0D;
            this.solidTotal = 0;
            this.fluidTotal = 0;
            this.pendingStack = ItemStack.EMPTY;
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
