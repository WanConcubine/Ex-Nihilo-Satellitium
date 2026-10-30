package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One JEI page per compost entry. Tags are expanded to the items the client's
 * tag manager knows about; entries that expand to nothing (an unknown tag) are
 * skipped instead of rendering an empty slot.
 */
public record CompostRecipe(List<ItemStack> inputs, int value) {

    public static List<CompostRecipe> all() {
        final List<CompostRecipe> recipes = new ArrayList<>();
        for (final CompostRegistry.Entry entry : CompostRegistry.all()) {
            final List<ItemStack> stacks = expand(entry);
            if (stacks.isEmpty()) {
                ExNihiloSatellitium.LOGGER.warn("[jei] compost entry {} expanded to no items",
                        entry.item() != null ? entry.item() : entry.tag());
                continue;
            }
            recipes.add(new CompostRecipe(List.copyOf(stacks), entry.value()));
        }
        return recipes;
    }

    private static List<ItemStack> expand(final CompostRegistry.Entry entry) {
        if (entry.item() != null) {
            return List.of(new ItemStack(entry.item()));
        }
        final List<ItemStack> stacks = new ArrayList<>();
        for (final Item item : BuiltInRegistries.ITEM) {
            final ItemStack stack = new ItemStack(item);
            if (stack.is(entry.tag())) {
                stacks.add(stack);
            }
        }
        return stacks;
    }
}
