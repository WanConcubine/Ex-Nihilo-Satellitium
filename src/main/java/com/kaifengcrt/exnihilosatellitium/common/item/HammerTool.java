package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for every item that should use the hammer crush table.
 *
 * <p>{@link HammerItem} is the plain durability tool; the optional Draconic
 * Evolution hammers extend DE's modular pickaxe and the optional Tinkers'
 * Construct hammer extends TiC's modifiable item, so none of them can share a
 * superclass. The event hooks only depend on this interface.</p>
 */
public interface HammerTool {

    /** 1 = wood ... 6 = netherite; 7-9 = the optional DE wyvern / draconic / chaotic tiers. */
    int tier();

    /**
     * Energy paid per broken block / machine operation. {@code 0} means the
     * tool uses durability instead (the plain {@link HammerItem} hammers).
     */
    default int energyPerUse() {
        return 0;
    }

    /**
     * Mining-speed floor applied to hammer-table blocks.
     *
     * <p>Plain hammers and the DE hammers have no vanilla tool stats, so the
     * hook gives them the classic 6.0. Returning {@code 0} disables the floor
     * and lets the tool's own stats decide (the Tinkers' hammer uses its head
     * material's mining speed, so material progression still matters).</p>
     */
    default float breakSpeed(final ItemStack stack) {
        return 6.0F;
    }

    /**
     * How many extra connected blocks one crush may take (0 = no chain).
     *
     * <p>The plain hammers and the machine never chain. The optional
     * integration hammers borrow their own progression system: the Tinkers'
     * hammer needs our {@code exnihilosatellitium:chain} modifier and the
     * Draconic Evolution hammer needs DE's AOE module installed.</p>
     */
    default int chainLimit(final ItemStack stack) {
        return 0;
    }

    /**
     * Pays one point of durability after a successful crush.
     *
     * <p>1.20.1 has no drop-replacement event, so {@code ToolEvents} cancels
     * the break and must charge the held stack itself. The default is the
     * vanilla wear path used by {@link HammerItem}; the optional Tinkers'
     * hammer overrides it with TiC's own damage hooks (overslime, reinforced,
     * durability multipliers, break animation).</p>
     */
    default void wear(final Player player, final ItemStack stack) {
        stack.hurtAndBreak(1, player,
                broken -> broken.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }
}
