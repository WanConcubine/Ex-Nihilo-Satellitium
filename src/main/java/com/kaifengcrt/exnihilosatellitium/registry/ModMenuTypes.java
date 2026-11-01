package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.FuelMachineMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Menu types for the four machines. The client factory reads the machine block
 * position from the extra data written by {@code MachineBlock#use}.
 */
public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ExNihiloSatellitium.MOD_ID);

    public static final RegistryObject<MenuType<ElectricMachineMenu>> ELECTRIC_MACHINE =
            MENU_TYPES.register("electric_machine",
                    () -> IForgeMenuType.create(ElectricMachineMenu::new));

    public static final RegistryObject<MenuType<FuelMachineMenu>> FUEL_MACHINE =
            MENU_TYPES.register("fuel_machine",
                    () -> IForgeMenuType.create(FuelMachineMenu::new));

    private ModMenuTypes() {
    }

    public static void register(final IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
