package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Self-drop loot tables for every block that has one. */
public final class ModLootTables extends BlockLootSubProvider {

    public ModLootTables(final HolderLookup.Provider registries) {
        super(Set.<Item>of(), FeatureFlagSet.of(net.minecraft.world.flag.FeatureFlags.VANILLA), registries);
    }

    @Override
    protected void generate() {
        for (final Block block : blocks()) {
            this.dropSelf(block);
        }
    }

    /**
     * Validation iterates this list; keeping it to the exact set we generate
     * avoids the "non-blocks" check comparing against every block in the game
     * (including blocks that intentionally have no loot table, e.g. infested
     * leaves and the witch-water fluid block).
     */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return blocks();
    }

    private static List<Block> blocks() {
        final List<Block> blocks = new ArrayList<>();
        for (final var barrel : ModBlocks.BARRELS) {
            blocks.add(barrel.get());
        }
        for (final var crucible : ModBlocks.WOODEN_CRUCIBLES) {
            blocks.add(crucible.get());
        }
        for (final OreMaterial material : OreMaterials.all()) {
            for (final String kind : ModOreBlocks.KINDS) {
                blocks.add(ModOreBlocks.byMaterial().get(material.id()).get(kind).get());
            }
        }
        for (final Block block : new Block[]{
                ModBlocks.DUST.get(), ModBlocks.CRUCIBLE.get(), ModBlocks.UNFIRED_CRUCIBLE.get(),
                ModBlocks.CRUSHED_NETHERRACK.get(), ModBlocks.CRUSHED_END_STONE.get(),
                ModBlocks.SIEVE.get(), ModBlocks.ELECTRIC_SIEVE.get(),
                ModBlocks.ELECTRIC_HAMMER.get(), ModBlocks.FUEL_SIEVE.get(),
                ModBlocks.FUEL_HAMMER.get()}) {
            blocks.add(block);
        }
        return blocks;
    }
}
