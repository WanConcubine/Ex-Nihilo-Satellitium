package com.kaifengcrt.exnihilosatellitium.common.material;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One entry of {@code materials.json}: a metal/ore the mod owns a full chain for.
 *
 * @param id     registry-safe material name, e.g. {@code copper}
 * @param color  0xRRGGBB identity colour, used for the block overlay tint
 * @param nameEn English display name
 * @param nameZh Chinese display name
 * @param tier   common / uncommon / rare / epic (documentation + validation)
 * @param rarity the N in {@code 1/N} for sieve drops
 * @param ingot           the ingot this runtime smelts into, or {@code null} to register
 *                        our own {@code <id>_ingot} — matching the classic "create the
 *                        ingot if no other mod supplies one" behaviour
 * @param ingotCandidates the raw {@code ingots} list, in declaration order, kept so the
 *                        hand-written conditional recipes and the runtime agree; empty
 *                        when the entry uses an unconditional {@code ingot}
 */
public record OreMaterial(String id, int color, String nameEn, String nameZh, String tier, int rarity,
                          @Nullable String ingot, List<IngotCandidate> ingotCandidates) {

    public OreMaterial {
        ingotCandidates = List.copyOf(ingotCandidates);
    }

    /**
     * One {@code {"item": ..., "mod": ...}} candidate: the first entry whose mod is
     * loaded supplies the ingot.
     *
     * @param mod {@code null} when the entry is unconditional
     */
    public record IngotCandidate(String item, @Nullable String mod) {
    }

    /** True when this material owns its ingot item instead of reusing one. */
    public boolean hasOwnIngot() {
        return this.ingot == null;
    }
}
