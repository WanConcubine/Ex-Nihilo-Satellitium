package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.modules.Module;
import com.brandon3055.draconicevolution.api.modules.items.ModuleItem;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleImpl;
import com.brandon3055.draconicevolution.init.DEModules;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * The dedicated {@code chain_mining} module for our Draconic Evolution hammers.
 *
 * <p>DE ships an AOE (mining radius) module, but that one only breaks the blocks
 * a pickaxe is the right tool for, so gravel / sand are skipped. This module is
 * the chain switch: when installed, {@link DEHammerItem#chainLimit} takes
 * {@link #BLOCKS} more connected hammer-table blocks per crush.</p>
 *
 * <p>DE's module registry is created by DE itself through {@code NewRegistryEvent};
 * we therefore register our module inside DE's key with {@link RegisterEvent}
 * instead of creating a second {@code DeferredRegister} for that registry.</p>
 */
public final class ChainModule {

    /** Extra blocks per crush; one module is enough, no tiers. */
    public static final int BLOCKS = 32;

    public static final ChainModuleType TYPE = new ChainModuleType();

    private static RegistryObject<Item> item;
    private static Module<ChainData> module;

    private ChainModule() {
    }

    /** Registers the module item now and the module itself on DE's registry event. */
    public static void register(final IEventBus modBus, final DeferredRegister<Item> items) {
        item = items.register("chain_mining_module", () -> new ChainModuleItem(() -> module));
        modBus.addListener(ChainModule::registerModule);
    }

    private static void registerModule(final RegisterEvent event) {
        if (!event.getRegistryKey().equals(DEModules.MODULE_KEY)) {
            return;
        }
        module = new ModuleImplImpl(item);
        event.register(DEModules.MODULE_KEY,
                new ResourceLocation(ExNihiloSatellitium.MOD_ID, ChainModuleType.NAME),
                () -> module);
    }

    /** The module item, or {@code null} before registration / without DE. */
    public static Item item() {
        return item == null ? null : item.get();
    }

    /** The registered module, or {@code null} before the registry event. */
    public static Module<ChainData> module() {
        return module;
    }

    /**
     * {@code ModuleImpl} ties itself to an {@link Item} at construction time. The
     * item is registered in the same session, so resolve it lazily instead.
     */
    private static final class ModuleImplImpl extends ModuleImpl<ChainData> {

        private final RegistryObject<Item> item;

        private ModuleImplImpl(final RegistryObject<Item> item) {
            super(TYPE, TechLevel.WYVERN, m -> new ChainData(BLOCKS));
            this.item = item;
        }

        @Override
        public Item getItem() {
            return this.item.get();
        }
    }

    private static final class ChainModuleItem extends ModuleItem<ChainData> {

        private ChainModuleItem(final Supplier<Module<?>> module) {
            super(new Item.Properties(), module);
        }
    }
}
