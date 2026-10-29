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
 */
public record OreMaterial(String id, int color, String nameEn, String nameZh, String tier, int rarity) {
}
