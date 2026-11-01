package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** JEI-facing snapshot: one leaves table with its extra drops. */
public record CrookRecipe(Block leaves, List<CrookDrops.Drop> drops) {

    public static List<CrookRecipe> all() {
        return CrookDrops.all().stream()
                .map(entry -> new CrookRecipe(entry.getKey(), entry.getValue()))
                .toList();
    }
}
