package com.kaifengcrt.exnihilosatellitium.common.table;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Script-contributed table mutations that survive resource reloads.
 *
 * <p>KubeJS / CraftTweaker (and, until those land, the
 * {@code /exnihilo customize} test command) do not mutate a live table. They
 * queue an operation here, and every rebuild replays the queue <em>after</em>
 * all datapack files and patches. This is what makes {@code /reload} safe:
 * re-reading the JSON never drops script contributions, and replaying them on a
 * fresh builder never stacks them twice.</p>
 *
 * <p>An operation payload uses the same JSON shape as a datapack file for that
 * table, so the loader's parser and validation are reused verbatim:</p>
 * <ul>
 *   <li>{@link Action#ADD} — normal-file semantics (add / merge, first rule wins
 *       for one-rule-per-block tables);</li>
 *   <li>{@link Action#REPLACE} — the target key's old value is dropped and the
 *       payload becomes the only value;</li>
 *   <li>{@link Action#REMOVE} — delete the targeted rule/input (or, for list
 *       tables, the entries listed under {@code remove}).</li>
 * </ul>
 *
 * <p>Operations are applied in submission order; later operations win, and the
 * conflict is visible in the {@code [customize]} log. {@link #clear(String)}
 * lets a script reload drop only its own contributions.</p>
 */
public final class CustomizationOverlay {

    public enum Action {
        ADD,
        REMOVE,
        REPLACE
    }

    /** One queued operation; {@code payload} is defensively copied on submit. */
    public record Customization(TableId table, Action action, JsonObject payload, String source,
                                long sequence) {

        public String describe() {
            return this.table.id() + " " + this.action.name().toLowerCase(java.util.Locale.ROOT)
                    + " " + this.payload;
        }
    }

    private static final List<Customization> OPERATIONS = new CopyOnWriteArrayList<>();
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private CustomizationOverlay() {
    }

    public static Customization add(final TableId table, final JsonObject payload, final String source) {
        return submit(table, Action.ADD, payload, source);
    }

    public static Customization remove(final TableId table, final JsonObject payload, final String source) {
        return submit(table, Action.REMOVE, payload, source);
    }

    public static Customization replace(final TableId table, final JsonObject payload, final String source) {
        return submit(table, Action.REPLACE, payload, source);
    }

    public static Customization submit(final TableId table, final Action action, final JsonObject payload,
                                       final String source) {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(payload, "payload");
        final String origin = source == null || source.isBlank() ? "unknown" : source;
        final Customization customization = new Customization(
                table, action, payload.deepCopy(), origin, SEQUENCE.incrementAndGet());
        OPERATIONS.add(customization);
        ExNihiloSatellitium.LOGGER.info("[customize] queued {} from {}",
                customization.describe(), origin);
        return customization;
    }

    /** Snapshot of the operations for one table, in submission order. */
    public static List<Customization> forTable(final TableId table) {
        final List<Customization> list = new ArrayList<>();
        for (final Customization customization : OPERATIONS) {
            if (customization.table() == table) {
                list.add(customization);
            }
        }
        return List.copyOf(list);
    }

    /** Removes every operation contributed by one source; returns how many were dropped. */
    public static int clear(final String source) {
        final int before = OPERATIONS.size();
        OPERATIONS.removeIf(customization -> customization.source().equals(source));
        return before - OPERATIONS.size();
    }

    /** Drops every operation (used by tests and by a full script reload). */
    public static int clearAll() {
        final int size = OPERATIONS.size();
        OPERATIONS.clear();
        return size;
    }

    public static int size() {
        return OPERATIONS.size();
    }

    public static List<Customization> all() {
        return List.copyOf(OPERATIONS);
    }
}
