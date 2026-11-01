package com.kaifengcrt.exnihilosatellitium.common.machine;

/** Machine upgrade categories. Each machine uses only the highest tier it holds. */
public enum UpgradeType {
    /** Shortens the ticks needed for one operation. */
    SPEED,
    /** Adds independent extra rolls of the same loot table (better yield, same table). */
    FORTUNE
}
