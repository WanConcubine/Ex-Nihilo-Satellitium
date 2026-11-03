package com.kaifengcrt.exnihilosatellitium.common.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;

/**
 * End Cake: a cake that teleports the eater to the End.
 *
 * <p>Placed with a single slice left (like the 1.10 Omnia cake); each ender eye
 * added to the cake restores one slice, up to a full cake. Eating a slice
 * feeds the player and is always edible: in the Overworld it sends them to the
 * End, and in the End it sends them back to their Overworld respawn point (or
 * world spawn).</p>
 */
public final class EndCakeBlock extends CakeBlock {

    public static final MapCodec<EndCakeBlock> CODEC = simpleCodec(EndCakeBlock::new);

    public EndCakeBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<CakeBlock> codec() {
        // The codec is only used for this block's own type; decoding always
        // produces an EndCakeBlock, which is a CakeBlock.
        return (MapCodec<CakeBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        // One use by default; ender eyes buy more slices.
        return this.defaultBlockState().setValue(BITES, MAX_BITES);
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state,
                                              final Level level, final BlockPos pos,
                                              final Player player, final InteractionHand hand,
                                              final BlockHitResult hit) {
        if (!stack.is(Items.ENDER_EYE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        final int bites = state.getValue(BITES);
        if (bites > 0) {
            level.setBlock(pos, state.setValue(BITES, bites - 1), 3);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level,
                                               final BlockPos pos, final Player player,
                                               final BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        serverPlayer.awardStat(Stats.EAT_CAKE_SLICE);
        serverPlayer.getFoodData().eat(2, 0.1F);
        final int bites = state.getValue(BITES);
        level.gameEvent(player, GameEvent.EAT, pos);
        if (bites < MAX_BITES) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }

        if (level.dimension() == Level.OVERWORLD) {
            teleportToEnd(serverPlayer);
        } else if (level.dimension() == Level.END) {
            teleportToOverworld(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    /** Sends the player back to their Overworld respawn point, or the world spawn. */
    private static void teleportToOverworld(final ServerPlayer player) {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        final ServerLevel overworld = server.overworld();
        BlockPos target = player.getRespawnPosition();
        if (target == null || player.getRespawnDimension() != Level.OVERWORLD) {
            target = overworld.getSharedSpawnPos();
        }
        final double x = target.getX() + 0.5D;
        final double y = target.getY() + 1.0D;
        final double z = target.getZ() + 0.5D;
        player.teleportTo(overworld, x, y, z, player.getYRot(), player.getXRot());
        overworld.sendParticles(ParticleTypes.PORTAL, x, y + 0.5D, z, 32,
                0.5D, 1.0D, 0.5D, 0.1D);
        overworld.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** Sends the player to the central End island. */
    private static void teleportToEnd(final ServerPlayer player) {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        final ServerLevel end = server.getLevel(Level.END);
        if (end == null) {
            return;
        }
        // Force the central island chunk to generate before reading its height.
        end.getChunk(0, 0);
        final BlockPos surface = end.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.ZERO);
        final double x = 0.5D;
        final double y = Math.max(surface.getY() + 1, 65.0D);
        final double z = 0.5D;
        player.teleportTo(end, x, y, z, player.getYRot(), player.getXRot());
        end.sendParticles(ParticleTypes.PORTAL, x, y + 0.5D, z, 32,
                0.5D, 1.0D, 0.5D, 0.1D);
        end.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
