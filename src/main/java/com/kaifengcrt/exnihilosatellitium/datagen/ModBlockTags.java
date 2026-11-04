package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.common.tag.ModTags;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Block tags, generated instead of hand-written. */
public final class ModBlockTags extends BlockTagsProvider {

    public ModBlockTags(final PackOutput output,
                        final CompletableFuture<HolderLookup.Provider> lookup,
                        final ExistingFileHelper helper) {
        super(output, lookup, ExNihiloSatellitium.MOD_ID, helper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        final var axe = this.tag(BlockTags.MINEABLE_WITH_AXE);
        for (final var barrel : ModBlocks.BARRELS) {
            if (barrel != ModBlocks.STONE_BARREL) {
                axe.add(barrel.get());
            }
        }
        for (final var crucible : ModBlocks.WOODEN_CRUCIBLES) {
            axe.add(crucible.get());
        }
        axe.add(ModBlocks.SIEVE.get());

        final var pickaxe = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        pickaxe.add(ModBlocks.STONE_BARREL.get(), ModBlocks.CRUCIBLE.get(),
                ModBlocks.UNFIRED_CRUCIBLE.get(), ModBlocks.CRUSHED_NETHERRACK.get(),
                ModBlocks.CRUSHED_END_STONE.get(), ModBlocks.ELECTRIC_SIEVE.get(),
                ModBlocks.ELECTRIC_HAMMER.get(), ModBlocks.FUEL_SIEVE.get(),
                ModBlocks.FUEL_HAMMER.get());

        final var shovel = this.tag(BlockTags.MINEABLE_WITH_SHOVEL);
        shovel.add(ModBlocks.DUST.get(), ModBlocks.CRUSHED_NETHERRACK.get(),
                ModBlocks.CRUSHED_END_STONE.get());

        this.tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.ELECTRIC_SIEVE.get(),
                ModBlocks.ELECTRIC_HAMMER.get(), ModBlocks.FUEL_SIEVE.get(),
                ModBlocks.FUEL_HAMMER.get());

        final var wooden = this.tag(ModTags.Blocks.WOODEN_CRUCIBLES);
        for (final var crucible : ModBlocks.WOODEN_CRUCIBLES) {
            wooden.add(crucible.get());
        }

        final var forgeOres = this.tag(forge("ores"));
        for (final OreMaterial material : OreMaterials.all()) {
            final var oreTag = forge("ores/" + material.id());
            final var blocks = ModOreBlocks.byMaterial().get(material.id());
            for (final String kind : ModOreBlocks.KINDS) {
                final Block block = blocks.get(kind).get();
                // Optional: a mod-gated gem material may not exist at runtime.
                shovel.addOptional(ForgeRegistries.BLOCKS.getKey(block));
                this.tag(oreTag).addOptional(ForgeRegistries.BLOCKS.getKey(block));
            }
            forgeOres.addTag(oreTag);
        }

        // Extreme Reactors tags yellorite ore as an uranium ore too; mirror it so
        // Mekanism / IE ore processing accepts our yellorium ore blocks.
        final var yelloriumBlocks = ModOreBlocks.byMaterial().get("yellorium");
        if (yelloriumBlocks != null) {
            final var uraniumOres = this.tag(forge("ores/uranium"));
            for (final String kind : ModOreBlocks.KINDS) {
                uraniumOres.addOptional(ForgeRegistries.BLOCKS.getKey(yelloriumBlocks.get(kind).get()));
            }
        }
    }

    static TagKey<Block> forge(final String path) {
        return BlockTags.create(new ResourceLocation("forge", path));
    }

    @Override
    public String getName() {
        return "Ex Nihilo Satellitium block tags";
    }
}
