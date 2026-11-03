package com.kaifengcrt.exnihilosatellitium.common.table;

import java.util.Locale;
import java.util.Optional;

/**
 * The seven data-driven gameplay tables.
 *
 * <p>The id is the datapack directory name <em>and</em> the script/command name:
 * {@code sieve}, {@code hammer}, {@code crook}, {@code barrel}, {@code compost},
 * {@code crucible_melt} and {@code crucible_heat}. Keeping one name for all three
 * customization paths means a fixture can be translated 1:1 between JSON, KubeJS
 * and CraftTweaker without a lookup table.</p>
 */
public enum TableId {
    SIEVE("sieve"),
    HAMMER("hammer"),
    CROOK("crook"),
    BARREL("barrel"),
    COMPOST("compost"),
    CRUCIBLE_MELT("crucible_melt"),
    CRUCIBLE_HEAT("crucible_heat");

    private final String id;

    TableId(final String id) {
        this.id = id;
    }

    /** Datapack directory / command / dump name. */
    public String id() {
        return this.id;
    }

    /** Case-insensitive lookup used by commands and, later, script bindings. */
    public static Optional<TableId> byId(final String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        final String normalized = raw.toLowerCase(Locale.ROOT);
        for (final TableId table : values()) {
            if (table.id.equals(normalized) || table.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(table);
            }
        }
        return Optional.empty();
    }
}
