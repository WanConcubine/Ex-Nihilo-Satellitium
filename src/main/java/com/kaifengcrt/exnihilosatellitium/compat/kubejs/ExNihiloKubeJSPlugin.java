package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;

/**
 * Optional KubeJS 7 bridge, discovered through {@code kubejs.plugins.txt}.
 *
 * <p>Bindings are only added to server scripts. They queue overlay operations with
 * the {@code kubejs} source and priority 100; the normal tag/reload rebuild (and
 * the explicit server-start rebuild) applies them. Before each script run,
 * {@link #beforeScriptsLoaded(ScriptManager)} drops the previous KubeJS
 * contributions so editing a script cannot leave stale entries behind.</p>
 */
public final class ExNihiloKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void beforeScriptsLoaded(final ScriptManager manager) {
        // Only server scripts contribute; a client script reload on an integrated
        // server must not wipe the server's queue.
        if (!manager.scriptType.isServer()) {
            return;
        }
        final int dropped = CustomizationOverlay.clear(KubeJSBridge.SOURCE);
        if (dropped > 0) {
            ExNihiloSatellitium.LOGGER.info(
                    "[customize] kubejs scripts reloading, dropped {} old op(s)", dropped);
        }
    }

    @Override
    public void registerBindings(final BindingRegistry event) {
        if (!event.type().isServer()) {
            return;
        }
        event.add("SieveEvents", new SieveEvents());
        event.add("HammerEvents", new HammerEvents());
        event.add("CrookEvents", new CrookEvents());
        event.add("BarrelEvents", new BarrelEvents());
        event.add("CompostEvents", new CompostEvents());
        event.add("CrucibleMeltEvents", new CrucibleMeltEvents());
        event.add("CrucibleHeatEvents", new CrucibleHeatEvents());
    }
}
