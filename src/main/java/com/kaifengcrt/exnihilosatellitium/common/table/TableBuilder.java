package com.kaifengcrt.exnihilosatellitium.common.table;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * Ordered working set shared by every JSON loader and by the script overlay.
 *
 * <p>A table rebuild is always: create a builder, feed it the datapack files
 * (via the table's own parser), then feed it the queued
 * {@linkplain CustomizationOverlay overlay operations}, then publish
 * {@link #build()} as one immutable snapshot. Because both sources write through
 * this class, add / remove / replace have exactly one implementation per table
 * instead of one in the JSON loader and another in the script layer.</p>
 *
 * <p>Insertion order is preserved so the published snapshot is deterministic:
 * the same files plus the same overlay always produce the same order, which
 * makes {@code /exnihilo dump} diffable and makes {@code /reload} idempotent.</p>
 *
 * <p>The builder also remembers which source last wrote a key. The dump command
 * and the conflict log use it to answer "why is this entry here?".</p>
 *
 * @param <K> key type (registry object, composite string, ...)
 * @param <V> immutable entry type ({@code SieveDrop} list, {@code Rule}, ...)
 */
public final class TableBuilder<K, V> {

    private final String table;
    private final LinkedHashMap<K, V> entries = new LinkedHashMap<>();
    private final Map<K, String> sources = new HashMap<>();

    public TableBuilder(final String table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public String table() {
        return this.table;
    }

    /**
     * Adds a key only when it is absent — the "normal datapack file" rule for
     * one-rule-per-key tables, and the "add" rule for the overlay.
     *
     * @return {@code true} when the entry was added, {@code false} when the key already existed
     */
    public boolean putIfAbsent(final K key, final V value, final String source) {
        if (this.entries.containsKey(key)) {
            return false;
        }
        this.entries.put(key, value);
        this.sources.put(key, source);
        return true;
    }

    /**
     * Inserts or overwrites a key — the patch / replace rule.
     *
     * @return {@code true} when the key was new
     */
    public boolean put(final K key, final V value, final String source) {
        final boolean added = !this.entries.containsKey(key);
        this.entries.put(key, value);
        this.sources.put(key, source);
        return added;
    }

    /**
     * Merges into an existing value (sieve / crook drop lists); creates the key
     * when absent. The merger must return an immutable value.
     */
    public void merge(final K key, final V value, final BinaryOperator<V> merger, final String source) {
        final V previous = this.entries.get(key);
        this.entries.put(key, previous == null ? value : merger.apply(previous, value));
        this.sources.put(key, source);
    }

    public boolean remove(final K key) {
        this.sources.remove(key);
        return this.entries.remove(key) != null;
    }

    public V get(final K key) {
        return this.entries.get(key);
    }

    public boolean contains(final K key) {
        return this.entries.containsKey(key);
    }

    public int size() {
        return this.entries.size();
    }

    public Set<K> keys() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    public Collection<V> values() {
        return Collections.unmodifiableCollection(this.entries.values());
    }

    /** Which source last wrote this key; {@code "?"} when unknown. */
    public String sourceOf(final K key) {
        return this.sources.getOrDefault(key, "?");
    }

    /** Immutable, insertion-ordered snapshot handed to gameplay/JEI. */
    public Map<K, V> build() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(this.entries));
    }
}
