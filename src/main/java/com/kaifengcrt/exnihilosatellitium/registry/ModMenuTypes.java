package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.FuelMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Menu types for the two machine GUIs (electric and fuel share one screen each). */
public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ExNihiloSatellitium.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricMachineMenu>> ELECTRIC_MACHINE =
            MENUS.register("electric_machine",
                    () -> IMenuTypeExtension.create(ElectricMachineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FuelMachineMenu>> FUEL_MACHINE =
            MENUS.register("fuel_machine",
                    () -> IMenuTypeExtension.create(FuelMachineMenu::new));

    private ModMenuTypes() {
    }

    public static void register(final IEventBus modBus) {
        MENUS.register(modBus);
    }
}
