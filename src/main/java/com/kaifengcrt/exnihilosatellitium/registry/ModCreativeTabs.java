package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative tab. Keeps the mod's content in one place and gives the mod a
 * visible entry point in the creative inventory.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExNihiloSatellitium.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ExNihiloSatellitium.MOD_ID))
            .icon(() -> new ItemStack(ModItems.PORCELAIN.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.PORCELAIN.get());
                ModItems.BARREL_ITEMS.forEach(item -> output.accept(item.get()));
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(final IEventBus modBus) {
        TABS.register(modBus);
    }
}
