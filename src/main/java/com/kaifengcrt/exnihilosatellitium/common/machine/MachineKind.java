package com.kaifengcrt.exnihilosatellitium.common.machine;

/** The two automated work kinds; both share the manual judgement functions. */
public enum MachineKind {
    /** Needs a mesh; uses {@link SieveRolls}. */
    SIEVE,
    /** Needs a hammer; uses {@link HammerRolls}. */
    HAMMER
}
