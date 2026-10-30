package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * One JEI page for a sieve input. Built from the same {@link SieveRegistry}
 * table the block entity rolls against, so the two can never drift.
 */
public record SieveRecipe(ItemStack input, List<SieveDrop> drops) {

    public static List<SieveRecipe> all() {
        return SieveRegistry.all().entrySet().stream()
                .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey())))
                .map(SieveRecipe::of)
                .toList();
    }

    private static SieveRecipe of(final Map.Entry<Item, List<SieveDrop>> entry) {
        return new SieveRecipe(new ItemStack(entry.getKey()), List.copyOf(entry.getValue()));
    }
}
