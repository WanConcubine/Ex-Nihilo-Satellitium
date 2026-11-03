package com.kaifengcrt.exnihilosatellitium.common.material;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One entry of {@code materials.json}: a metal/mineral the mod owns a full chain for.
 *
 * <p>Metals end in an ingot and use the {@code ingot} / {@code ingots} keys.
 * Minerals (gems, dusts, crystals) end in the material itself and use
 * {@code gems}; their fallback item is {@code <id>} instead of {@code <id>_ingot},
 * because there is no such thing as a "fluorite ingot".</p>
 *
 * @param id         registry-safe material name, e.g. {@code copper}
 * @param color      0xRRGGBB identity colour, used for the block overlay tint
 * @param nameEn     English display name
 * @param nameZh     Chinese display name
 * @param tier       common / uncommon / rare / epic (documentation + validation)
 * @param rarity     the N in {@code 1/N} for sieve drops
 * @param product    the item this runtime smelts into, or {@code null} to register
 *                   our own fallback ({@code <id>_ingot}, or {@code <id>} for gems)
 * @param candidates the raw {@code ingots} / {@code gems} list, in declaration order,
 *                   so the hand-written conditional recipes and the runtime agree;
 *                   empty when the entry uses an unconditional {@code ingot} or has
 *                   no candidate list
 * @param gem        {@code true} when the entry uses {@code gems}: the fallback item
 *                   is {@code <id>} and it joins {@code forge:gems/<id>} instead of
 *                   {@code forge:ingots/<id>}
 */
public record OreMaterial(String id, int color, String nameEn, String nameZh, String tier, int rarity,
                          @Nullable String product, List<ProductCandidate> candidates, boolean gem) {

    public OreMaterial {
        candidates = List.copyOf(candidates);
    }

    /**
     * One {@code {"item": ..., "mod": ...}} candidate: the first entry whose mod is
     * loaded supplies the smelting product.
     *
     * @param mod {@code null} when the entry is unconditional
     */
    public record ProductCandidate(String item, @Nullable String mod) {
    }

    /**
     * True when this material owns its fallback item instead of reusing one.
     *
     * <p>Gem/mineral entries ({@code gems}) never own a fallback: their product is
     * supplied by the supported mod only, so an unmodded instance has no smelting
     * endpoint and must not register a fake "apatite" item.</p>
     */
    public boolean hasOwnProduct() {
        return !this.gem && this.product == null;
    }

    /** Registry path of the fallback ingot; only metals ever own one. */
    public String ownItemId() {
        return this.id + "_ingot";
    }

    /** Tag path of the fallback ingot; only metals ever own one. */
    public String productTag() {
        return "ingots/" + this.id;
    }
}
