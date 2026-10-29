package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative tab: one visible entry point for the mod's content.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExNihiloSatellitium.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ExNihiloSatellitium.MOD_ID))
                    .icon(() -> new ItemStack(ModOreItems.byMaterial().values().iterator().next()
                            .get("ore_gravel").get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PORCELAIN.get());
                        ModItems.BARREL_ITEMS.forEach(item -> output.accept(item.get()));
                        for (final OreMaterial material : OreMaterials.all()) {
                            final var entries = ModOreItems.byMaterial().get(material.id());
                            for (final String kind : ModOreBlocks.KINDS) {
                                output.accept(entries.get("ore_" + kind).get());
                            }
                            for (final String stage : ModOreItems.STAGES) {
                                output.accept(entries.get(stage).get());
                            }
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(final IEventBus modBus) {
        TABS.register(modBus);
    }
}
