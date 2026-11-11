package com.kaifengcrt.exnihilosatellitium.compat.tconstruct;

import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * The {@code exnihilosatellitium:chain} upgrade for the crushing hammer.
 *
 * <p>Tinkers' Construct only stores the modifier and draws its name; the
 * behaviour is driven by {@code ToolEvents} through
 * {@code HammerTool#chainLimit}, so this class deliberately has no hooks. Each
 * level lets one crush take {@link TinkersTools#CHAIN_BLOCKS_PER_LEVEL} more
 * connected blocks of the same kind.</p>
 */
public final class ChainModifier extends Modifier {

    public ChainModifier() {
        super();
    }
}
