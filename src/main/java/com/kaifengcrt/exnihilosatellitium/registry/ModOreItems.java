package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Block items for the three ore-block states plus the three item stages
 * (broken / crushed / powdered) of every material. Materials without an
 * {@code ingot} entry in {@code materials.json} also get their own ingot item,
 * so the chain ends somewhere even when no other mod supplies that metal.
 */
public final class ModOreItems {

    public static final List<String> STAGES = List.of("broken", "crushed", "powdered");

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ExNihiloSatellitium.MOD_ID);

    private static final Map<String, Map<String, DeferredHolder<Item, Item>>> BY_MATERIAL = new LinkedHashMap<>();

    static {
        for (final OreMaterial material : OreMaterials.all()) {
            final Map<String, DeferredHolder<Item, Item>> entries = new LinkedHashMap<>();
            for (final String kind : ModOreBlocks.KINDS) {
                final DeferredHolder<Block, Block> block = ModOreBlocks.byMaterial().get(material.id()).get(kind);
                entries.put("ore_" + kind, ITEMS.register(material.id() + "_ore_" + kind,
                        () -> new BlockItem(block.get(), new Item.Properties())));
            }
            for (final String stage : STAGES) {
                entries.put(stage, ITEMS.register(material.id() + "_" + stage,
                        () -> new Item(new Item.Properties())));
            }
            if (material.hasOwnIngot()) {
                entries.put("ingot", ITEMS.register(material.id() + "_ingot",
                        () -> new Item(new Item.Properties())));
            }
            BY_MATERIAL.put(material.id(), entries);
        }
    }

    private ModOreItems() {
    }

    public static Map<String, Map<String, DeferredHolder<Item, Item>>> byMaterial() {
        return BY_MATERIAL;
    }

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
