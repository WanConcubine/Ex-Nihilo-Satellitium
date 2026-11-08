package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.DataComponentAccessor;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.items.equipment.IModularEnergyItem;
import com.brandon3055.draconicevolution.api.modules.lib.ModularOPStorage;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Registration entry point for the optional Draconic Evolution hammers.
 *
 * <p>This class lives in the {@code de} source set, which is only compiled when
 * the DE jars are present in {@code libs/de}. Core code reaches it through
 * reflection, so a normal build / game without Draconic Evolution never loads
 * a DE class.</p>
 */
public final class DEHammers {

    private static final List<DeferredHolder<Item, Item>> HAMMERS = new ArrayList<>();

    private DEHammers() {
    }

    public static List<DeferredHolder<Item, Item>> register(final DeferredRegister<Item> items) {
        HAMMERS.clear();
        HAMMERS.add(register(items, "wyvern_hammer", TechLevel.WYVERN, 7));
        HAMMERS.add(register(items, "draconic_hammer", TechLevel.DRACONIC, 8));
        HAMMERS.add(register(items, "chaotic_hammer", TechLevel.CHAOTIC, 9));
        return List.copyOf(HAMMERS);
    }

    private static DeferredHolder<Item, Item> register(final DeferredRegister<Item> items,
                                                       final String name, final TechLevel level,
                                                       final int tier) {
        return items.register(name, () -> new DEHammerItem(level, tier));
    }

    /**
     * DE only exposes its module / energy capabilities for items in its own
     * registry, so the optional hammers register the same providers here.
     */
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        if (HAMMERS.isEmpty()) {
            return;
        }
        final Item[] items = HAMMERS.stream()
                .map(DeferredHolder::get)
                .toArray(Item[]::new);
        event.registerItem(DECapabilities.Host.ITEM,
                (stack, context) -> {
                    final var host = ((IModularItem) stack.getItem())
                            .createHostCapForRegistration(stack);
                    host.updateDataAccess(DataComponentAccessor.itemStack(stack));
                    return host;
                },
                items);
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> {
                    final ModularOPStorage storage = ((IModularEnergyItem) stack.getItem())
                            .createOPCapForRegistration(stack);
                    storage.updateDataAccess(DataComponentAccessor.itemStack(stack));
                    // DE's ModularOPStorage is receive-only; wrap it so our machine /
                    // manual drain can actually extract, while DE itself still uses
                    // the same backing storage.
                    return new IEnergyStorage() {
                        @Override
                        public int receiveEnergy(final int maxReceive, final boolean simulate) {
                            return storage.receiveEnergy(maxReceive, simulate);
                        }

                        @Override
                        public int extractEnergy(final int maxExtract, final boolean simulate) {
                            if (simulate) {
                                return (int) Math.min(maxExtract, storage.getOPStored());
                            }
                            return (int) Math.abs(storage.modifyEnergyStored(-maxExtract));
                        }

                        @Override
                        public int getEnergyStored() {
                            return (int) Math.min(Integer.MAX_VALUE, storage.getOPStored());
                        }

                        @Override
                        public int getMaxEnergyStored() {
                            return (int) Math.min(Integer.MAX_VALUE, storage.getMaxOPStored());
                        }

                        @Override
                        public boolean canExtract() {
                            return true;
                        }

                        @Override
                        public boolean canReceive() {
                            return storage.canReceive();
                        }
                    };
                },
                items);
    }
}
