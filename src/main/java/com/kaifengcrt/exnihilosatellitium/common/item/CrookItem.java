package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.world.item.Item;

/**
 * Crook: breaking leaves with it adds the extra sapling/apple/silkworm drops
 * from the crook table. The tier only scales the chance; it has no other
 * gameplay effect yet.
 */
public class CrookItem extends Item {

    private final int tier;

    public CrookItem(final Properties properties, final int tier) {
        super(properties);
        this.tier = tier;
    }

    /** 1 = wood, 2 = bone. */
    public int tier() {
        return this.tier;
    }

    /** Bone crooks roll the table at 1.5x; capped at 100% per entry. */
    public float chanceMultiplier() {
        return this.tier >= 2 ? 1.5F : 1.0F;
    }
}
