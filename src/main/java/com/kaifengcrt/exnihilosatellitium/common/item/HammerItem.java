package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.world.item.Item;

/**
 * Hammer: breaking a block with it replaces the block's drops with the entry
 * from {@code data/<ns>/hammer/*.json} (see {@code ToolEvents}).
 *
 * <p>Like classic Ex Nihilo, the hammer only acts through the normal
 * block-break path — there is deliberately no right-click shortcut. The tier
 * only orders the materials (and their durability); it does not change the
 * crush table.</p>
 */
public class HammerItem extends Item {

    private final int tier;

    public HammerItem(final Properties properties, final int tier) {
        super(properties);
        this.tier = tier;
    }

    /** 1 = wood, 2 = stone, 3 = iron, 4 = gold, 5 = diamond, 6 = netherite. */
    public int tier() {
        return this.tier;
    }
}
