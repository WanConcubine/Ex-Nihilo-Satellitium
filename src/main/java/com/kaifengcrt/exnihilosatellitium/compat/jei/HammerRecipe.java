package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** JEI-facing snapshot: one hammer rule (input block → result item, count range). */
public record HammerRecipe(Block input, Item result, int min, int max) {

    public static List<HammerRecipe> all() {
        return HammerRecipes.all().stream()
                // A rule that drops the block itself (ore dust) is a gameplay
                // safety net, not a crushing recipe; hide it from JEI.
                .filter(entry -> entry.getValue().item() != entry.getKey().asItem())
                .map(entry -> new HammerRecipe(entry.getKey(), entry.getValue().item(),
                        entry.getValue().min(), entry.getValue().max()))
                .toList();
    }

    public boolean single() {
        return this.min == this.max;
    }
}
