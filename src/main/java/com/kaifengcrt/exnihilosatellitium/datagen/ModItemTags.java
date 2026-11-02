package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.common.tag.ModTags;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Item tags (ores/ingots mirrored from the ore blocks) and compostables. */
public final class ModItemTags extends ItemTagsProvider {

    public ModItemTags(final PackOutput output,
                       final CompletableFuture<HolderLookup.Provider> lookup,
                       final CompletableFuture<TagLookup<Block>> blockTags,
                       final ExistingFileHelper helper) {
        super(output, lookup, blockTags, ExNihiloSatellitium.MOD_ID, helper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        final var forgeOres = this.tag(forge("ores"));
        for (final OreMaterial material : OreMaterials.all()) {
            final var oreTag = forge("ores/" + material.id());
            for (final String kind : ModOreBlocks.KINDS) {
                this.tag(oreTag).add(ModOreBlocks.byMaterial().get(material.id()).get(kind).get()
                        .asItem());
            }
            forgeOres.addTag(oreTag);
        }

        for (final OreMaterial material : OreMaterials.all()) {
            if (!material.hasOwnIngot()) {
                continue;
            }
            final var ingot = ModOreItems.byMaterial().get(material.id()).get("ingot");
            if (ingot != null) {
                this.tag(forge("ingots/" + material.id())).addOptional(ingot.getId());
            }
        }

        final var compostable = this.tag(ModTags.Items.COMPOSTABLE);
        compostable.addTag(ItemTags.SAPLINGS);
        compostable.addTag(ItemTags.LEAVES);
        compostable.addTag(ItemTags.FLOWERS);
        compostable.addTag(ItemTags.FISHES);
        compostable.addTag(forge("seeds"));
        compostable.addTag(forge("crops/wheat"));
        compostable.addTag(forge("crops/carrot"));
        compostable.addTag(forge("crops/beetroot"));
        compostable.addTag(forge("crops/potato"));
        compostable.addTag(forge("crops/nether_wart"));
        compostable.addTag(forge("eggs"));
        compostable.addTag(forge("string"));
        compostable.add(Items.GHAST_TEAR, Items.ROTTEN_FLESH, Items.SPIDER_EYE,
                Items.FERMENTED_SPIDER_EYE, Items.BREAD, Items.BROWN_MUSHROOM,
                Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS,
                Items.PUMPKIN_PIE, Items.APPLE, Items.GOLDEN_APPLE, Items.MELON_SLICE,
                Items.MELON, Items.PUMPKIN, Items.CARVED_PUMPKIN, Items.JACK_O_LANTERN,
                Items.CACTUS, Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.LILY_PAD,
                Items.VINE, Items.WEEPING_VINES, Items.TWISTING_VINES, Items.GRASS,
                Items.TALL_GRASS, Items.SUGAR_CANE, Items.SWEET_BERRIES, Items.GLOW_BERRIES,
                Items.SPORE_BLOSSOM, Items.KELP, Items.MANGROVE_ROOTS, Items.CRIMSON_ROOTS,
                Items.WARPED_ROOTS, Items.HANGING_ROOTS, Items.BIG_DRIPLEAF,
                Items.SMALL_DRIPLEAF, Items.FERN, Items.LARGE_FERN, Items.SEAGRASS,
                Items.BAMBOO, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD, Items.PORKCHOP,
                Items.COOKED_PORKCHOP, Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN,
                Items.COOKED_CHICKEN, Items.MUTTON, Items.COOKED_MUTTON, Items.RABBIT,
                Items.COOKED_RABBIT,
                ModItems.CRIMSON_NYLIUM_SPORES.get(), ModItems.WARPED_NYLIUM_SPORES.get());
    }

    static TagKey<Item> forge(final String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }

    @Override
    public String getName() {
        return "Ex Nihilo Satellitium item tags";
    }
}
