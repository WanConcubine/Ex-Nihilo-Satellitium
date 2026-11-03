package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

/**
 * Optional 1.20.1 KubeJS bridge, discovered through {@code kubejs.plugins.txt}.
 *
 * <p>The 2001 line has a different plugin API from the 1.21.1 2101 line
 * ({@code KubeJSPlugin} is a class and bindings arrive as a {@link BindingsEvent}),
 * but the binding objects below are identical. They submit to the shared
 * {@link CustomizationOverlay} with source {@code kubejs} and priority 100.</p>
 *
 * <p>{@link #registerBindings(BindingsEvent)} runs before each server script batch;
 * clearing the previous KubeJS contributions here makes a script edit on
 * {@code /reload} replace the old entries instead of stacking them. The tables
 * are rebuilt by the tag reload and the server-start rebuild, like the main line.</p>
 */
public final class ExNihiloKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(final BindingsEvent event) {
        if (!event.getType().isServer()) {
            return;
        }
        final int dropped = CustomizationOverlay.clear(KubeJSBridge.SOURCE);
        if (dropped > 0) {
            ExNihiloSatellitium.LOGGER.info(
                    "[customize] kubejs server scripts reloading, dropped {} old op(s)", dropped);
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
