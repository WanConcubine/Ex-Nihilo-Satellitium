package com.kaifengcrt.exnihilosatellitium.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only wiring for the mod list's "Config" button.
 *
 * <p>NeoForge only enables that button for mods that register
 * {@link IConfigScreenFactory}; the screen itself is NeoForge's generic
 * {@link ConfigurationScreen}, which draws every {@code ModConfigSpec} value
 * (booleans, numbers, lists) and pops the "restart required" prompt for values
 * marked {@code gameRestart()} / {@code worldRestart()}.</p>
 *
 * <p>This class must never be loaded on a dedicated server: the caller guards it
 * with {@code FMLEnvironment.dist.isClient()}.</p>
 */
public final class ConfigScreens {

    private ConfigScreens() {
    }

    public static void register(final ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent));
    }
}
