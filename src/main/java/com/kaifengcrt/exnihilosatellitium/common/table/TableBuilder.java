package com.kaifengcrt.exnihilosatellitium.common.table;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

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
 * <p>The builder remembers which source last wrote a key. When an overlay source
 * (a source name starting with {@code customize:}) overwrites, deletes or fails
 * to add a key, the builder records a {@link Conflict}; {@link #logConflicts()}
 * prints one line per conflict with the previous source, so "who won" is in the
 * log instead of having to be guessed from load order.</p>
 *
 * @param <K> key type (registry object, composite string, ...)
 * @param <V> immutable entry type ({@code SieveDrop} list, {@code Rule}, ...)
 */
public final class TableBuilder<K, V> {

    /** One overlay write that interacted with an existing entry. */
    public record Conflict<K>(K key, Kind kind, String previousSource, String newSource) {

        public enum Kind {
            REPLACE,
            REMOVE,
            ADD_IGNORED
        }
    }

    private static final String OVERLAY_PREFIX = "customize:";

    private final String table;
    private final Function<K, String> keyLabel;
    private final LinkedHashMap<K, V> entries = new LinkedHashMap<>();
    private final Map<K, String> sources = new HashMap<>();
    private final List<Conflict<K>> conflicts = new ArrayList<>();

    public TableBuilder(final String table) {
        this(table, String::valueOf);
    }

    public TableBuilder(final String table, final Function<K, String> keyLabel) {
        this.table = Objects.requireNonNull(table, "table");
        this.keyLabel = Objects.requireNonNull(keyLabel, "keyLabel");
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
            if (isOverlay(source)) {
                record(key, Conflict.Kind.ADD_IGNORED, this.sources.get(key), source);
            }
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
        if (!added && isOverlay(source)) {
            record(key, Conflict.Kind.REPLACE, this.sources.get(key), source);
        }
        this.entries.put(key, value);
        this.sources.put(key, source);
        return added;
    }

    /**
     * Updates a value without reporting a conflict; used by overlay operations
     * that only edit part of an existing list (e.g. removing one sieve drop).
     */
    public void set(final K key, final V value, final String source) {
        this.entries.put(key, value);
        this.sources.put(key, source);
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
        return remove(key, null);
    }

    public boolean remove(final K key, final String source) {
        final String previous = this.sources.remove(key);
        final boolean removed = this.entries.remove(key) != null;
        if (removed && isOverlay(source)) {
            record(key, Conflict.Kind.REMOVE, previous, source);
        }
        return removed;
    }

    /**
     * Records that an overlay removed part of an existing list value without
     * replacing the whole key (sieve / crook drop removal).
     */
    public void recordRemoval(final K key, final String source) {
        if (isOverlay(source) && this.entries.containsKey(key)) {
            record(key, Conflict.Kind.REMOVE, this.sources.get(key), source);
        }
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

    /** Conflicts seen during this rebuild, in write order. */
    public List<Conflict<K>> conflicts() {
        return List.copyOf(this.conflicts);
    }

    /**
     * Prints one {@code [customize]} line per overlay conflict. Called after the
     * snapshot is published so a reload shows the same winners every time.
     */
    public void logConflicts() {
        for (final Conflict<K> conflict : this.conflicts) {
            final String label = this.keyLabel.apply(conflict.key());
            final String previous = conflict.previousSource() == null
                    ? "?" : conflict.previousSource();
            switch (conflict.kind()) {
                case REPLACE -> ExNihiloSatellitium.LOGGER.info(
                        "[customize] {} replaced {} (was {})", this.table, label, previous);
                case REMOVE -> ExNihiloSatellitium.LOGGER.info(
                        "[customize] {} removed {} (was {})", this.table, label, previous);
                case ADD_IGNORED -> ExNihiloSatellitium.LOGGER.info(
                        "[customize] {} add ignored for {} (kept {})", this.table, label, previous);
            }
        }
    }

    /** Immutable, insertion-ordered snapshot handed to gameplay/JEI. */
    public Map<K, V> build() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(this.entries));
    }

    private void record(final K key, final Conflict.Kind kind, final String previousSource,
                        final String newSource) {
        this.conflicts.add(new Conflict<>(key, kind, previousSource, newSource));
    }

    private static boolean isOverlay(final String source) {
        return source != null && source.startsWith(OVERLAY_PREFIX);
    }
}
