package com.kaifengcrt.exnihilosatellitium.common.tag;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Project-wide tags.
 */
public final class ModTags {

    private ModTags() {
    }

    public static final class Items {

        /**
         * Items the barrel accepts for composting. The exact value per item
         * lives in {@code data/<namespace>/compost/*.json}; this tag only
         * answers "can the client start the interaction?".
         */
        public static final TagKey<Item> COMPOSTABLE =
                ItemTags.create(ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "compostable"));

        private Items() {
        }
    }
}
