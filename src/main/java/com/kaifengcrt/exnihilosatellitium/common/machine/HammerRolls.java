package com.kaifengcrt.exnihilosatellitium.common.machine;

import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The single crush judgement shared by the manual hammer and the automatic
 * hammer. Fortune adds independent rolls of the same rule and merges them into
 * one stack, so fortune II on a 4-6 drop yields 12-18.
 */
public final class HammerRolls {

    private HammerRolls() {
    }

    public static boolean isCrushable(final ItemStack input) {
        return ruleFor(input) != null;
    }

    /** Rule lookup for a machine input item; {@code null} when the item is not a hammer-able block. */
    public static HammerRecipes.Rule ruleFor(final ItemStack input) {
        if (input.isEmpty() || !(input.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        return HammerRecipes.ruleFor(blockItem.getBlock().defaultBlockState());
    }

    /** Empty when the item is not a hammer-able block. */
    public static List<ItemStack> roll(final ItemStack input, final int extraRolls,
                                       final RandomSource random) {
        final HammerRecipes.Rule rule = ruleFor(input);
        return rule == null ? List.of() : List.of(roll(rule, extraRolls, random));
    }

    /** Empty when the block has no hammer rule. */
    public static List<ItemStack> roll(final BlockState state, final int extraRolls,
                                       final RandomSource random) {
        final HammerRecipes.Rule rule = HammerRecipes.ruleFor(state);
        return rule == null ? List.of() : List.of(roll(rule, extraRolls, random));
    }

    private static ItemStack roll(final HammerRecipes.Rule rule, final int extraRolls,
                                  final RandomSource random) {
        int count = 0;
        final int rolls = 1 + Math.max(0, extraRolls);
        for (int roll = 0; roll < rolls; roll++) {
            count += rule.roll(random).getCount();
        }
        return new ItemStack(rule.item(), count);
    }
}
