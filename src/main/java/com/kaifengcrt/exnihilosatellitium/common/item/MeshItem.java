package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.world.item.Item;

/**
 * Sieve mesh. The tier only decides the model and the durability; it does not
 * change the sift table (by design, matching Omnia).
 */
public class MeshItem extends Item {

    private final int tier;

    public MeshItem(final Properties properties, final int tier) {
        super(properties);
        this.tier = tier;
    }

    /** 1 = wood, 2 = silk, 3 = iron. */
    public int tier() {
        return this.tier;
    }
}
