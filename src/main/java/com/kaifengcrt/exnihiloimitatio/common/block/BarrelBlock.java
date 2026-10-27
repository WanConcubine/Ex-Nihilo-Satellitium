package com.kaifengcrt.exnihiloimitatio.common.block;

import com.kaifengcrt.exnihiloimitatio.common.blockentity.BarrelBlockEntity;
import com.kaifengcrt.exnihiloimitatio.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The barrel: the first block of the progression.
 *
 * <p>It stores one bucket of fluid and turns compostable items into dirt over
 * time. The actual state lives in {@link BarrelBlockEntity}; this class only
 * dispatches interactions and ticking.</p>
 *
 * <p>Forge 47.4.x marks {@code BlockBehaviour#use} and the analog-signal
 * methods as deprecated, but 1.20.1 exposes no replacement; suppressing the
 * warning keeps the build clean without changing behaviour.</p>
 */
@SuppressWarnings("deprecation")
public class BarrelBlock extends BaseEntityBlock {

    /** Inset body: 14x16x14, leaving a 1px gap on each side. */
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public BarrelBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                               final CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                        final CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, ModBlockEntities.BARREL.get(), BarrelBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                 final Player player, final InteractionHand hand, final BlockHitResult hit) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            // Client-side prediction only: never mutate the tank here.
            return barrel.canUse(player.getItemInHand(hand))
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        return barrel.use(player, hand);
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof BarrelBlockEntity barrel ? barrel.getComparatorOutput() : 0;
    }
}
