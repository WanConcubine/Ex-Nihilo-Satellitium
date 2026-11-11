package com.kaifengcrt.exnihilosatellitium.common.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds the connected blocks a chain-mining hammer may take along with the
 * block the player aimed at.
 *
 * <p>Only the exact same block as the origin is followed, and only blocks that
 * still have a hammer-table rule. The search is bounded twice: at the tool's
 * own chain limit and at {@link #MAX_VISITED} positions, so a bad data pack
 * cannot turn one click into an unbounded world scan.</p>
 */
public final class ChainMining {

    /** Hard stop for the flood fill, independent of the tool's limit. */
    private static final int MAX_VISITED = 1024;

    private ChainMining() {
    }

    /**
     * Breadth-first search over the six face neighbours of the origin.
     *
     * @param limit maximum number of extra positions to return; must be &gt; 0
     * @return the extra positions, never including {@code origin}
     */
    public static List<BlockPos> collect(final ServerLevel level, final BlockPos origin,
                                         final BlockState originState, final int limit) {
        final List<BlockPos> found = new ArrayList<>(Math.min(limit, 64));
        if (limit <= 0) {
            return found;
        }
        final Set<BlockPos> visited = new HashSet<>();
        final ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty() && found.size() < limit && visited.size() < MAX_VISITED) {
            final BlockPos current = queue.poll();
            for (final Direction direction : Direction.values()) {
                final BlockPos next = current.relative(direction);
                if (!visited.add(next) || !level.isLoaded(next)) {
                    continue;
                }
                final BlockState state = level.getBlockState(next);
                if (state.getBlock() != originState.getBlock()) {
                    continue;
                }
                if (HammerRecipes.ruleFor(state) == null) {
                    continue;
                }
                found.add(next);
                if (found.size() >= limit) {
                    return found;
                }
                queue.add(next);
            }
        }
        return found;
    }
}
