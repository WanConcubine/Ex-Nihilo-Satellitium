package com.kaifengcrt.exnihilosatellitium.common.sieve;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

/**
 * One possible sieve output: an item, its independent per-sift chance, and the
 * stack size range of a successful roll.
 *
 * <p>Most drops are a single item, so {@code min} and {@code max} default to
 * {@code 1}; pebbles and similar bulk drops use {@code 4..6}.</p>
 */
public record SieveDrop(Item item, float chance, int min, int max) {

    public SieveDrop(final Item item, final float chance) {
        this(item, chance, 1, 1);
    }

    /** How many items one successful roll yields (inclusive range). */
    public int count(final RandomSource random) {
        return this.max <= this.min ? this.min : this.min + random.nextInt(this.max - this.min + 1);
    }
}
