package com.kaifengcrt.exnihilosatellitium.common.tag;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Project-wide tags.
 */
public final class ModTags {

    private ModTags() {
    }

    public static final class Blocks {

        /** All wooden crucible variants: passive melting, water only. */
        public static final TagKey<Block> WOODEN_CRUCIBLES =
                BlockTags.create(ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "wooden_crucibles"));

        private Blocks() {
        }
    }

    public static final class Items {

        /**
         * Items the barrel accepts for composting. The exact value per item
         * lives in {@code data/<namespace>/compost/*.json}; this tag only
         * answers "can the client start the interaction?".
         */
        public static final TagKey<Item> COMPOSTABLE =
                ItemTags.create(ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "compostable"));

        /**
         * "Any chest" for the capacity upgrade recipe: chest, trapped chest and
         * ender chest. A tag (instead of three recipe variants) also lets a mod
         * add its own chest without touching our recipe.
         */
        public static final TagKey<Item> CHESTS =
                ItemTags.create(ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "chests"));

        private Items() {
        }
    }
}
