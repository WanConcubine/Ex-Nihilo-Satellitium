package com.kaifengcrt.exnihilosatellitium.common.block;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.InfestedLeavesBlockEntity;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Infested leaves (M6). The block itself is invisible: a block entity renders the
 * leaf block it replaced plus white motes, and drops string when broken.
 */
public class InfestedLeavesBlock extends BaseEntityBlock {

    private static final MapCodec<InfestedLeavesBlock> CODEC = simpleCodec(InfestedLeavesBlock::new);

    public InfestedLeavesBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final net.minecraft.world.level.BlockGetter level,
                               final BlockPos pos, final CollisionContext context) {
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new InfestedLeavesBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.INFESTED_LEAVES.get(),
                        InfestedLeavesBlockEntity::serverTick);
    }

    @Override
    public BlockState playerWillDestroy(final Level level, final BlockPos pos, final BlockState state,
                                        final Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof InfestedLeavesBlockEntity leaves) {
            leaves.dropString(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
