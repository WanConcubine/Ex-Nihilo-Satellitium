package com.kaifengcrt.exnihilosatellitium.common.sieve;

import net.minecraft.world.item.Item;

/** One possible sieve output: an item and its independent per-sift chance. */
public record SieveDrop(Item item, float chance) {
}
