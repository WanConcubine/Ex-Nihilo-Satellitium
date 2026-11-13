package com.kaifengcrt.exnihilosatellitium.common.machine;

/**
 * Machine upgrade categories. Speed and fortune stack by item count with their
 * own diminishing-return rules; capacity uses the item count as the page count.
 */
public enum UpgradeType {
    /** Shortens the ticks needed for one operation. */
    SPEED,
    /** Adds independent extra rolls of the same loot table (better yield, same table). */
    FORTUNE,
    /**
     * Adds one page (12 slots) to the machine output buffer. Stacking is the
     * whole point: every item in the stack is one page, up to eight.
     */
    CAPACITY
}
