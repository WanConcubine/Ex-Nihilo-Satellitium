package com.kaifengcrt.exnihilosatellitium.common.block;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.CrucibleBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Fired crucible: an open bowl that melts blocks placed into it when a heat
 * source sits directly below. The tank and the melt queue live in
 * {@link CrucibleBlockEntity}.
 */
@SuppressWarnings("deprecation")
public class CrucibleBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public CrucibleBlock(final BlockBehaviour.Properties properties) {
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
        return new CrucibleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, ModBlockEntities.CRUCIBLE.get(), CrucibleBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                 final Player player, final InteractionHand hand,
                                 final BlockHitResult hit) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CrucibleBlockEntity crucible)) {
            return InteractionResult.PASS;
        }
        final ItemStack stack = player.getItemInHand(hand);
        // A meltable block must never fall through to "place it on top of the crucible":
        // a full crucible simply swallows the right-click (classic Ex Nihilo behaviour).
        // Sneak-placing still works, because sneaking skips use() entirely.
        final boolean meltable = !stack.isEmpty()
                && CrucibleRecipes.meltFor(Block.byItem(stack.getItem())) != null;
        if (level.isClientSide) {
            return stack.isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        final InteractionResult result = crucible.use(player, hand);
        if (result.consumesAction()) {
            return InteractionResult.SUCCESS;
        }
        // Rejected. FAIL would NOT stop vanilla: FAIL is not "consuming", so
        // ServerPlayerGameMode#useItemOn falls through to ItemStack#useOn and places the
        // block (or spills the bucket). CONSUME stops the chain without any visible effect.
        final boolean container = !stack.isEmpty() && FluidUtil.getFluidHandler(stack).isPresent();
        return meltable || container ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof CrucibleBlockEntity crucible ? crucible.getComparatorOutput() : 0;
    }
}
