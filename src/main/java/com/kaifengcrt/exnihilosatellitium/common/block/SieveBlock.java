package com.kaifengcrt.exnihilosatellitium.common.block;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.SieveBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
@SuppressWarnings("deprecation")
public class SieveBlock extends BaseEntityBlock {

    public static final IntegerProperty MESH = IntegerProperty.create("mesh", 0, 3);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);

    public SieveBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MESH, 0));
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
    public RenderShape getRenderShape(final BlockState state) {
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
        return level.isClientSide
                ? null
                : createTickerHelper(type, ModBlockEntities.SIEVE.get(), SieveBlockEntity::serverTick);
    }

    @Override
    public void playerWillDestroy(final Level level, final BlockPos pos, final BlockState state,
                                  final Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SieveBlockEntity sieve) {
            sieve.dropContentsOnBreak(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                 final Player player, final InteractionHand hand,
                                 final BlockHitResult hit) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SieveBlockEntity sieve)) {
            return InteractionResult.PASS;
        }
        final ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Install a mesh.
        if (stack.getItem() instanceof MeshItem && !sieve.hasMesh()) {
            sieve.setMesh(stack);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        if (!sieve.hasMesh()) {
            player.displayClientMessage(Component.translatable("message.exnihilosatellitium.need_mesh"), true);
            return InteractionResult.SUCCESS;
        }

        // Sneak + empty hand: empty the contents first, otherwise take the mesh back.
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            if (!sieve.getContents().isEmpty()) {
                sieve.ejectContents(player);
            } else {
                sieve.dropMesh(player);
            }
            return InteractionResult.SUCCESS;
        }

        // Load one item, or add a click of work to the item already in the sieve.
        if (sieve.insert(stack)) {
            return InteractionResult.SUCCESS;
        }
        if (sieve.keepUsing()) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
