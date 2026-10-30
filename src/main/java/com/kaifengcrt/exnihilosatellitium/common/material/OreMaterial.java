package com.kaifengcrt.exnihilosatellitium.common.material;

/**
 * One entry of {@code materials.json}: a metal/ore the mod owns a full chain for.
 *
 * @param id     registry-safe material name, e.g. {@code copper}
 * @param color  0xRRGGBB identity colour, used for the block overlay tint
 * @param nameEn English display name
 * @param nameZh Chinese display name
 * @param tier   common / uncommon / rare / epic (documentation + validation)
 * @param rarity the N in {@code 1/N} for sieve drops
 * @param ingot  existing ingot item id to smelt into, or {@code null} to register
 *               our own {@code <id>_ingot} (matching the classic "create the
 *               ingot if no other mod supplies one" behaviour)
 */
public record OreMaterial(String id, int color, String nameEn, String nameZh, String tier, int rarity,
                          String ingot) {

    /** True when this material owns its ingot item instead of reusing one. */
    public boolean hasOwnIngot() {
        return this.ingot == null;
    }
}
