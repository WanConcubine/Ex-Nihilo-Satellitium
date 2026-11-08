package com.kaifengcrt.exnihilosatellitium.common.item;

/**
 * Marker for every item that should use the hammer crush table.
 *
 * <p>{@link HammerItem} is the plain durability tool; the optional Draconic
 * Evolution hammers extend DE's modular pickaxe and therefore cannot share that
 * superclass, so the event hooks only depend on this interface.</p>
 */
public interface HammerTool {

    /** 1 = wood ... 6 = netherite; 7-9 = the optional DE wyvern / draconic / chaotic tiers. */
    int tier();
}
