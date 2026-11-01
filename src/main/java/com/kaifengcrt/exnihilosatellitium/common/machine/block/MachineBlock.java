package com.kaifengcrt.exnihilosatellitium.common.machine.block;

import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.ElectricMachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.FuelMachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.Containers;

/**
 * One block class for all four machines; the kind (sieve / hammer) and the
 * power tier (electric / fuel) are fixed at registration.
 *
 * <p>Right-click opens the machine menu. Contents are dropped when the block is
 * broken outside creative, so a machine full of meshes is never lost.</p>
 */
public class MachineBlock extends BaseEntityBlock {

    /** Sieve model is 12 px tall on four legs; hammer machines stay full cubes. */
    private static final VoxelShape SIEVE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    private final MachineKind kind;
    private final boolean electric;

    public MachineBlock(final Properties properties, final MachineKind kind, final boolean electric) {
        super(properties);
        this.kind = kind;
        this.electric = electric;
    }

    public MachineKind kind() {
        return this.kind;
    }

    public boolean isElectric() {
        return this.electric;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new MachineBlock(properties, this.kind, this.electric));
    }

    @Override
    protected RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level,
                                  final BlockPos pos, final CollisionContext context) {
        return this.kind == MachineKind.SIEVE ? SIEVE_SHAPE : Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level,
                                           final BlockPos pos, final CollisionContext context) {
        return this.kind == MachineKind.SIEVE ? SIEVE_SHAPE : Shapes.block();
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return this.electric
                ? new ElectricMachineBlockEntity(pos, state)
                : new FuelMachineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, blockState, entity) -> {
            if (entity instanceof MachineBlockEntity machine) {
                machine.serverTick();
            }
        };
    }

    @Override
    protected MenuProvider getMenuProvider(final BlockState state, final Level level,
                                           final BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state,
                                              final Level level, final BlockPos pos,
                                              final Player player, final InteractionHand hand,
                                              final BlockHitResult hitResult) {
        return this.open(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level,
                                               final BlockPos pos, final Player player,
                                               final BlockHitResult hitResult) {
        return this.open(level, pos, player).result();
    }

    private ItemInteractionResult open(final Level level, final BlockPos pos, final Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        final MenuProvider provider = this.getMenuProvider(level.getBlockState(pos), level, pos);
        if (provider != null && player instanceof ServerPlayer serverPlayer) {
            // The client menu factory reads the block position from the extra data;
            // without this writer the buffer is null and opening the GUI NPEs.
            serverPlayer.openMenu(provider, buffer -> buffer.writeBlockPos(pos));
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(final Level level, final BlockPos pos, final BlockState state,
                                        final Player player) {
        if (!level.isClientSide && !player.isCreative()
                && level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            Containers.dropContents(level, pos, machine.getInventory());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
