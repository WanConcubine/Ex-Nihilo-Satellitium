package com.kaifengcrt.exnihilosatellitium.common.table;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;

/**
 * One place that knows how to rebuild / measure every table.
 *
 * <p>The command layer and the tag-update event both use this, so "rebuild after
 * an overlay change" and "rebuild after /reload" go through the exact same
 * entry points. Each registry keeps its previous immutable snapshot when its
 * own rebuild throws, so a broken overlay cannot leave a half-published table.</p>
 */
public final class Tables {

    private Tables() {
    }

    public static void rebuild(final TableId table, final boolean clientView) {
        try {
            switch (table) {
                case SIEVE -> SieveRegistry.rebuild(clientView);
                case HAMMER -> HammerRecipes.rebuild(clientView);
                case CROOK -> CrookDrops.rebuild(clientView);
                case BARREL -> BarrelRecipes.rebuild(clientView);
                case COMPOST -> CompostRegistry.rebuild(clientView);
                case CRUCIBLE_MELT -> CrucibleRecipes.rebuild(clientView);
                case CRUCIBLE_HEAT -> CrucibleHeat.rebuild(clientView);
            }
        } catch (final RuntimeException exception) {
            ExNihiloSatellitium.LOGGER.error("[customize] rebuilding {} failed, keeping previous table",
                    table.id(), exception);
        }
    }

    public static void rebuildAll(final boolean clientView) {
        for (final TableId table : TableId.values()) {
            rebuild(table, clientView);
        }
    }
}
