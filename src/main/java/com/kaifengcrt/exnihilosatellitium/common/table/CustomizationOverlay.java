package com.kaifengcrt.exnihilosatellitium.common.table;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
 * <p><b>Ordering.</b> Operations are applied by ascending {@code priority}, then
 * by submission order. A higher priority is applied later, so it wins against a
 * lower one; two operations at the same priority fall back to "later submission
 * wins". Data pack files and patches are always applied before any operation, so
 * scripts are the last layer. The default priority is {@code 0}; KJS/CT (P2/P3)
 * should give their sources explicit tiers instead of relying on mod load order.</p>
 *
 * <p>Operations are cleared when the server stops, so a single-player world
 * switch cannot leak operations into the next world. Scripts re-submit on load;
 * the command entry is deliberately not persistent.</p>
 */
public final class CustomizationOverlay {

    /** Hard cap on queued operations; a runaway script should fail fast, not grow forever. */
    public static final int MAX_OPERATIONS = 4096;

    public enum Action {
        ADD,
        REMOVE,
        REPLACE
    }

    /** One queued operation; {@code payload} is defensively copied on submit. */
    public record Customization(TableId table, Action action, JsonObject payload, String source,
                                int priority, long sequence) {

        public String describe() {
            return this.table.id() + " " + this.action.name().toLowerCase(Locale.ROOT)
                    + " p" + this.priority + " " + this.payload;
        }
    }

    private static final List<Customization> OPERATIONS = new CopyOnWriteArrayList<>();
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private CustomizationOverlay() {
    }

    public static @Nullable Customization add(final TableId table, final JsonObject payload,
                                              final String source) {
        return submit(table, Action.ADD, payload, source, 0);
    }

    public static @Nullable Customization add(final TableId table, final JsonObject payload,
                                              final String source, final int priority) {
        return submit(table, Action.ADD, payload, source, priority);
    }

    public static @Nullable Customization remove(final TableId table, final JsonObject payload,
                                                 final String source) {
        return submit(table, Action.REMOVE, payload, source, 0);
    }

    public static @Nullable Customization remove(final TableId table, final JsonObject payload,
                                                 final String source, final int priority) {
        return submit(table, Action.REMOVE, payload, source, priority);
    }

    public static @Nullable Customization replace(final TableId table, final JsonObject payload,
                                                  final String source) {
        return submit(table, Action.REPLACE, payload, source, 0);
    }

    public static @Nullable Customization replace(final TableId table, final JsonObject payload,
                                                  final String source, final int priority) {
        return submit(table, Action.REPLACE, payload, source, priority);
    }

    /**
     * Queues one operation.
     *
     * @return the queued (or already queued identical) operation, or {@code null} when the
     *         payload is empty or the {@link #MAX_OPERATIONS cap} has been reached
     */
    public static @Nullable Customization submit(final TableId table, final Action action,
                                                 final JsonObject payload, final String source,
                                                 final int priority) {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(payload, "payload");
        final String origin = source == null || source.isBlank() ? "unknown" : source;
        if (payload.size() == 0) {
            ExNihiloSatellitium.LOGGER.warn("[customize] rejected empty payload from {}", origin);
            return null;
        }
        if (OPERATIONS.size() >= MAX_OPERATIONS) {
            ExNihiloSatellitium.LOGGER.error(
                    "[customize] operation cap {} reached, {} {} from {} rejected",
                    MAX_OPERATIONS, table.id(), action, origin);
            return null;
        }
        final JsonObject copy = payload.deepCopy();
        // Re-submitting the exact same operation from the same source (common when a
        // script reloads without clearing first) is a no-op instead of a second entry.
        for (final Customization existing : OPERATIONS) {
            if (existing.table() == table && existing.action() == action
                    && existing.priority() == priority
                    && existing.source().equals(origin)
                    && existing.payload().toString().equals(copy.toString())) {
                ExNihiloSatellitium.LOGGER.debug(
                        "[customize] duplicate {} from {} ignored", existing.describe(), origin);
                return existing;
            }
        }
        final Customization customization = new Customization(
                table, action, copy, origin, priority, SEQUENCE.incrementAndGet());
        OPERATIONS.add(customization);
        ExNihiloSatellitium.LOGGER.info("[customize] queued {} from {}",
                customization.describe(), origin);
        return customization;
    }

    /** Snapshot of the operations for one table: ascending priority, then submission order. */
    public static List<Customization> forTable(final TableId table) {
        final List<Customization> list = new ArrayList<>();
        for (final Customization customization : OPERATIONS) {
            if (customization.table() == table
                    && DataPackJson.modLoaded(customization.payload())) {
                list.add(customization);
            }
        }
        list.sort(Comparator.comparingInt(Customization::priority)
                .thenComparingLong(Customization::sequence));
        return List.copyOf(list);
    }

    /** Removes every operation contributed by one source; returns how many were dropped. */
    public static int clear(final String source) {
        final int before = OPERATIONS.size();
        OPERATIONS.removeIf(customization -> customization.source().equals(source));
        return before - OPERATIONS.size();
    }

    /** Drops every operation (server stop, tests, full script reload). */
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
