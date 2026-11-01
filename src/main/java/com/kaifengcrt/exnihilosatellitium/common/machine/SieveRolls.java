package com.kaifengcrt.exnihilosatellitium.common.machine;

import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The single sift judgement shared by the manual sieve (and, later, every
 * automatic sieve). Machines must never keep a second copy of the drop table.
 *
 * <p>Each drop is rolled independently for every roll. Fortune adds whole
 * independent rolls of the table (not a chance multiplier).</p>
 */
public final class SieveRolls {

    private SieveRolls() {
    }

    /** Empty when the input has no sift table. */
    public static List<ItemStack> roll(final ItemStack input, final int extraRolls,
                                       final RandomSource random) {
        final List<SieveDrop> drops = SieveRegistry.dropsFor(input);
        if (drops.isEmpty()) {
            return List.of();
        }
        final int rolls = 1 + Math.max(0, extraRolls);
        final List<ItemStack> outputs = new ArrayList<>();
        for (int roll = 0; roll < rolls; roll++) {
            for (final SieveDrop drop : drops) {
                if (random.nextFloat() < drop.chance()) {
                    outputs.add(new ItemStack(drop.item()));
                }
            }
        }
        return outputs;
    }
}
