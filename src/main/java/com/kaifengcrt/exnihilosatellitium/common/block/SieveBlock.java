package com.kaifengcrt.exnihilosatellitium.common.block;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.SieveBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Sieve: place it, drop a mesh in, then right-click with gravel / sand / dust.
 *
 * <p>The mesh is a consumable with durability; a broken mesh simply vanishes.</p>
 */
public class SieveBlock extends BaseEntityBlock {

    public static final IntegerProperty MESH = IntegerProperty.create("mesh", 0, 3);

    private static final MapCodec<SieveBlock> CODEC = simpleCodec(SieveBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);

    public SieveBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MESH, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MESH);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                               final CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(final BlockState state) {
        // BaseEntityBlock defaults to INVISIBLE; we want the JSON model drawn.
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new SieveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level,
                                              final BlockPos pos, final Player player,
                                              final InteractionHand hand, final BlockHitResult hit) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SieveBlockEntity sieve)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // Install a mesh.
        if (stack.getItem() instanceof MeshItem meshItem && !sieve.hasMesh()) {
            sieve.setMesh(new ItemStack(stack.getItem()));
            sieve.updateBlockMeshState();
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // Sneak + empty hand: take the mesh back out.
        if (sieve.hasMesh() && stack.isEmpty() && player.isShiftKeyDown()) {
            player.getInventory().placeItemBackInInventory(sieve.getMesh().copy());
            sieve.setMesh(ItemStack.EMPTY);
            sieve.updateBlockMeshState();
            return ItemInteractionResult.SUCCESS;
        }

        if (!sieve.hasMesh()) {
            player.displayClientMessage(Component.translatable("message.exnihilosatellitium.need_mesh"), true);
            return ItemInteractionResult.SUCCESS;
        }

        if (sieve.sift(player, stack)) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
