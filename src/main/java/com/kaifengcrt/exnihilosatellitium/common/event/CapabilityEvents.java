package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.item.PorcelainBucketFluidHandler;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineItemHandler;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;


/** Exposes the barrel tank as a fluid-handler capability (1.21 style). */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CapabilityEvents {

    private CapabilityEvents() {
    }

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.BARREL.get(),
                (barrel, side) -> barrel.getTank());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CRUCIBLE.get(),
                (crucible, side) -> crucible.getTank());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new PorcelainBucketFluidHandler(stack),
                ModItems.PORCELAIN_BUCKET.get());

        // M10: every machine exposes its slots as an item handler (hoppers / pipes).
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ELECTRIC_MACHINE.get(),
                (machine, side) -> new MachineItemHandler(machine));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FUEL_MACHINE.get(),
                (machine, side) -> new MachineItemHandler(machine));
        // Only electric machines expose an energy buffer; fuel machines return null.
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELECTRIC_MACHINE.get(),
                (machine, side) -> machine.getEnergy());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.FUEL_MACHINE.get(),
                (machine, side) -> machine.getEnergy());
    }
}
