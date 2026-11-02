package com.kaifengcrt.exnihilosatellitium.datagen;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * The eleven woods that get a barrel and a wooden crucible, shared by the recipe
 * and the model providers so the two can never disagree about ids or textures.
 *
 * <p>Oak keeps the plain ids ({@code barrel}, {@code wooden_crucible}). Textures are
 * vanilla-sampled: barrels use that wood's planks plus the stripped log/stem inside,
 * crucibles use the log/stem itself plus the stripped face inside.</p>
 */
final class WoodTypes {

    private WoodTypes() {
    }

    /**
     * @param name       wood name, e.g. {@code oak}; also the texture prefix
     * @param planks     planks item (barrel sides)
     * @param slab       slab item (barrel floor)
     * @param log        log / stem / block item (crucible sides + recipe)
     * @param logTexture vanilla texture name of the log, e.g. {@code oak_log},
     *                   {@code bamboo_block} or {@code crimson_stem}
     */
    record Wood(String name, ItemLike planks, ItemLike slab, ItemLike log, String logTexture) {

        /** Barrel block/item id: {@code barrel} for oak, {@code <wood>_barrel} otherwise. */
        String barrel() {
            return "oak".equals(this.name) ? "barrel" : this.name + "_barrel";
        }

        /** Crucible block/item id: {@code wooden_crucible} for oak, {@code <wood>_wooden_crucible} otherwise. */
        String crucible() {
            return "oak".equals(this.name) ? "wooden_crucible" : this.name + "_wooden_crucible";
        }

        String planksTexture() {
            return this.name + "_planks";
        }

        String strippedTexture() {
            return "stripped_" + this.logTexture;
        }

        String logTopTexture() {
            return this.logTexture + "_top";
        }
    }

    /** Registration / creative order; the oak entry keeps the unsuffixed ids. */
    static final List<Wood> ALL = List.of(
            new Wood("oak", Items.OAK_PLANKS, Items.OAK_SLAB, Items.OAK_LOG, "oak_log"),
            new Wood("spruce", Items.SPRUCE_PLANKS, Items.SPRUCE_SLAB, Items.SPRUCE_LOG, "spruce_log"),
            new Wood("birch", Items.BIRCH_PLANKS, Items.BIRCH_SLAB, Items.BIRCH_LOG, "birch_log"),
            new Wood("jungle", Items.JUNGLE_PLANKS, Items.JUNGLE_SLAB, Items.JUNGLE_LOG, "jungle_log"),
            new Wood("acacia", Items.ACACIA_PLANKS, Items.ACACIA_SLAB, Items.ACACIA_LOG, "acacia_log"),
            new Wood("dark_oak", Items.DARK_OAK_PLANKS, Items.DARK_OAK_SLAB, Items.DARK_OAK_LOG, "dark_oak_log"),
            new Wood("mangrove", Items.MANGROVE_PLANKS, Items.MANGROVE_SLAB, Items.MANGROVE_LOG, "mangrove_log"),
            new Wood("cherry", Items.CHERRY_PLANKS, Items.CHERRY_SLAB, Items.CHERRY_LOG, "cherry_log"),
            new Wood("bamboo", Items.BAMBOO_PLANKS, Items.BAMBOO_SLAB, Items.BAMBOO_BLOCK, "bamboo_block"),
            new Wood("crimson", Items.CRIMSON_PLANKS, Items.CRIMSON_SLAB, Items.CRIMSON_STEM, "crimson_stem"),
            new Wood("warped", Items.WARPED_PLANKS, Items.WARPED_SLAB, Items.WARPED_STEM, "warped_stem"));
}
