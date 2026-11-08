package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.brandonscore.api.TechLevel;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Registration entry point for the optional Draconic Evolution hammers. */
public final class DEHammers {

    private static final List<RegistryObject<Item>> HAMMERS = new ArrayList<>();

    private DEHammers() {
    }

    public static List<RegistryObject<Item>> register(final DeferredRegister<Item> items) {
        HAMMERS.clear();
        HAMMERS.add(items.register("wyvern_hammer",
                () -> new DEHammerItem(TechLevel.WYVERN, 7)));
        HAMMERS.add(items.register("draconic_hammer",
                () -> new DEHammerItem(TechLevel.DRACONIC, 8)));
        HAMMERS.add(items.register("chaotic_hammer",
                () -> new DEHammerItem(TechLevel.CHAOTIC, 9)));
        return List.copyOf(HAMMERS);
    }
}
