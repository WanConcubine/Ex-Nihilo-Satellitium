package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Every crafting / smelting recipe the mod ships, generated from the same tables
 * the runtime reads ({@code materials.json}, the block/item registries).
 *
 * <p>Two deliberate choices keep the output equal to the hand-written JSON this
 * replaces:</p>
 * <ul>
 *   <li>advancements are dropped — the mod has never shipped
 *       {@code advancement/recipes/**} and adding 133 of them would change which
 *       recipes the recipe book unlocks;</li>
 *   <li>cooking recipes are built directly with {@link CookingBookCategory#MISC}
 *       instead of {@code SimpleCookingRecipeBuilder}, which would derive
 *       {@code food}/{@code blocks} from the result item and change the recipe-book
 *       categories.</li>
 * </ul>
 *
 * <p>The twelve {@code *_smelting_mekanism} recipes are <em>not</em> generated:
 * their result is an item from a mod that is not on the datagen classpath, and an
 * {@link ItemStack} cannot reference an unregistered item. They stay hand-written
 * under {@code src/main/resources}.</p>
 */
public final class ModRecipes extends RecipeProvider {

    /** {@code #c:rods/wooden}: the shared stick tag on 1.21.1. */
    private static final TagKey<Item> WOODEN_RODS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden"));

    private static final RecipeCategory MISC = RecipeCategory.MISC;

    public ModRecipes(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(final RecipeOutput output) {
        final RecipeOutput out = withoutAdvancements(output);

        oreChain(out);
        barrels(out);
        woodenCrucibles(out);
        sieveAndMeshes(out);
        tools(out);
        machines(out);
        upgrades(out);
        dolls(out);
        porcelain(out);
        silkwormCooking(out);
    }

    // ------------------------------------------------------------ ore chain

    /**
     * Nine materials → 3 pack recipes + 3 smelting recipes each. Materials with an
     * {@code ingots} candidate list get a conditional base recipe (our own ingot
     * only while none of those mods is loaded); the per-mod variants stay hand-written.
     */
    private static void oreChain(final RecipeOutput out) {
        for (final OreMaterial material : OreMaterials.all()) {
            final Map<String, DeferredHolder<Item, Item>> items =
                    ModOreItems.byMaterial().get(material.id());
            final Item broken = items.get("broken").get();
            final Item crushed = items.get("crushed").get();
            final Item powdered = items.get("powdered").get();
            final ItemLike gravel = items.get("ore_gravel").get();
            final ItemLike sand = items.get("ore_sand").get();
            final ItemLike dust = items.get("ore_dust").get();

            pack(out, material.id() + "_broken_to_ore_gravel", broken, gravel);
            pack(out, material.id() + "_crushed_to_ore_sand", crushed, sand);
            pack(out, material.id() + "_powdered_to_ore_dust", powdered, dust);

            final List<ICondition> loaded = new ArrayList<>();
            for (final OreMaterial.IngotCandidate candidate : material.ingotCandidates()) {
                if (candidate.mod() != null) {
                    loaded.add(new ModLoadedCondition(candidate.mod()));
                }
            }
            final ICondition[] conditions;
            if (material.ingotCandidates().isEmpty()) {
                conditions = new ICondition[0];
            } else if (loaded.isEmpty()) {
                // An unconditional candidate always wins, so there is no base recipe.
                ExNihiloSatellitium.LOGGER.warn("[datagen] {}: unconditional ingot candidate, "
                        + "no base smelting recipe generated", material.id());
                continue;
            } else {
                conditions = new ICondition[]{new NotCondition(
                        loaded.size() == 1 ? loaded.get(0) : new OrCondition(loaded))};
            }

            final Item ingot = smeltingTarget(material);
            smelt(out, material.id() + "_ore_gravel_smelting", gravel, ingot, 0.1F, 200, conditions);
            smelt(out, material.id() + "_ore_sand_smelting", sand, ingot, 0.1F, 200, conditions);
            smelt(out, material.id() + "_ore_dust_smelting", dust, ingot, 0.1F, 200, conditions);
        }
    }

    /** The ingot our own recipes point at; for candidates that is always our fallback. */
    private static Item smeltingTarget(final OreMaterial material) {
        if (!material.ingotCandidates().isEmpty() || material.ingot() == null) {
            final var holder = ModOreItems.byMaterial().get(material.id()).get("ingot");
            if (holder == null) {
                throw new IllegalStateException("material " + material.id() + " has no own ingot item: "
                        + "run datagen without its supported mods on the classpath");
            }
            return holder.get();
        }
        return requireItem(material.ingot());
    }

    /** 2×2 of one refinement stage back into the ore block. */
    private static void pack(final RecipeOutput out, final String name, final ItemLike input,
                             final ItemLike result) {
        ShapedRecipeBuilder.shaped(MISC, result)
                .pattern("xx")
                .pattern("xx")
                .define('x', input)
                .unlockedBy("has_" + path(input), has(input))
                .save(out, id(name));
    }

    // -------------------------------------------------------------- barrels

    private static void barrels(final RecipeOutput out) {
        for (final WoodTypes.Wood wood : WoodTypes.ALL) {
            ShapedRecipeBuilder.shaped(MISC, blockItem(wood.barrel()))
                    .pattern("x x")
                    .pattern("x x")
                    .pattern("xyx")
                    .define('x', wood.planks())
                    .define('y', wood.slab())
                    .unlockedBy("has_planks", has(ItemTags.PLANKS))
                    .save(out, id(wood.barrel()));
        }

        ShapedRecipeBuilder.shaped(MISC, blockItem("stone_barrel"))
                .pattern("x x")
                .pattern("x x")
                .pattern("xyx")
                .define('x', Items.STONE)
                .define('y', Items.SMOOTH_STONE_SLAB)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(out, id("stone_barrel"));
    }

    // ----------------------------------------------------- wooden crucibles

    private static void woodenCrucibles(final RecipeOutput out) {
        for (final WoodTypes.Wood wood : WoodTypes.ALL) {
            ShapedRecipeBuilder.shaped(MISC, blockItem(wood.crucible()))
                    .pattern("x x")
                    .pattern("x x")
                    .pattern("xyx")
                    .define('x', wood.log())
                    .define('y', ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_log", has(ItemTags.LOGS))
                    .save(out, id(wood.crucible()));
        }
    }

    // ------------------------------------------------------ sieve and meshes

    private static void sieveAndMeshes(final RecipeOutput out) {
        ShapedRecipeBuilder.shaped(MISC, ModItems.SIEVE.get())
                .pattern("x x")
                .pattern("xyx")
                .pattern("z z")
                .define('x', ItemTags.PLANKS)
                .define('y', ItemTags.WOODEN_SLABS)
                .define('z', WOODEN_RODS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(out, id("sieve"));

        mesh(out, "wood_mesh", ModItems.WOOD_MESH.get(), Items.STICK);
        mesh(out, "silk_mesh", ModItems.SILK_MESH.get(), Items.STRING);
        mesh(out, "iron_mesh", ModItems.IRON_MESH.get(), Items.IRON_BARS);
    }

    private static void mesh(final RecipeOutput out, final String name, final ItemLike result,
                             final ItemLike material) {
        ShapedRecipeBuilder.shaped(MISC, result)
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', material)
                .unlockedBy("has_" + path(material), has(material))
                .save(out, id(name));
    }

    // ----------------------------------------------------------------- tools

    private static void tools(final RecipeOutput out) {
        hammer(out, "wooden_hammer", ModItems.WOODEN_HAMMER.get(), ItemTags.PLANKS);
        hammer(out, "stone_hammer", ModItems.STONE_HAMMER.get(), Ingredient.of(Items.COBBLESTONE));
        hammer(out, "iron_hammer", ModItems.IRON_HAMMER.get(), Ingredient.of(Items.IRON_INGOT));
        hammer(out, "golden_hammer", ModItems.GOLDEN_HAMMER.get(), Ingredient.of(Items.GOLD_INGOT));
        hammer(out, "diamond_hammer", ModItems.DIAMOND_HAMMER.get(), Ingredient.of(Items.DIAMOND));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.DIAMOND_HAMMER.get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        MISC, ModItems.NETHERITE_HAMMER.get())
                .unlocks("has_diamond_hammer", has(ModItems.DIAMOND_HAMMER.get()))
                .save(out, id("netherite_hammer"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.WOODEN_CROOK.get())
                .pattern("xx ")
                .pattern(" x ")
                .pattern(" x ")
                .define('x', WOODEN_RODS)
                .unlockedBy("has_rods", has(WOODEN_RODS))
                .save(out, id("wooden_crook"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.BONE_CROOK.get())
                .pattern("xx ")
                .pattern(" x ")
                .pattern(" x ")
                .define('x', Items.BONE)
                .unlockedBy("has_bone", has(Items.BONE))
                .save(out, id("bone_crook"));
    }

    private static void hammer(final RecipeOutput out, final String name, final ItemLike result,
                               final TagKey<Item> head) {
        hammer(out, name, result, Ingredient.of(head));
    }

    private static void hammer(final RecipeOutput out, final String name, final ItemLike result,
                               final Ingredient head) {
        ShapedRecipeBuilder.shaped(MISC, result)
                .pattern(" x ")
                .pattern(" yx")
                .pattern("y  ")
                .define('x', head)
                .define('y', WOODEN_RODS)
                .unlockedBy("has_rods", has(WOODEN_RODS))
                .save(out, id(name));
    }

    // -------------------------------------------------------------- machines

    private static void machines(final RecipeOutput out) {
        ShapedRecipeBuilder.shaped(MISC, ModItems.ELECTRIC_SIEVE.get())
                .pattern("IRI")
                .pattern("RBR")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('B', Items.IRON_BLOCK)
                .define('S', ModItems.SIEVE.get())
                .unlockedBy("has_sieve", has(ModItems.SIEVE.get()))
                .save(out, id("electric_sieve"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.ELECTRIC_HAMMER.get())
                .pattern("IRI")
                .pattern("RHR")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('H', ModItems.IRON_HAMMER.get())
                .unlockedBy("has_iron_hammer", has(ModItems.IRON_HAMMER.get()))
                .save(out, id("electric_hammer"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.FUEL_SIEVE.get())
                .pattern("ICI")
                .pattern("CFC")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('F', Items.FURNACE)
                .define('S', ModItems.SIEVE.get())
                .unlockedBy("has_sieve", has(ModItems.SIEVE.get()))
                .save(out, id("fuel_sieve"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.FUEL_HAMMER.get())
                .pattern("ICI")
                .pattern("CFC")
                .pattern("IHI")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('F', Items.FURNACE)
                .define('H', ModItems.IRON_HAMMER.get())
                .unlockedBy("has_iron_hammer", has(ModItems.IRON_HAMMER.get()))
                .save(out, id("fuel_hammer"));
    }

    // -------------------------------------------------------------- upgrades

    private static void upgrades(final RecipeOutput out) {
        ShapedRecipeBuilder.shaped(MISC, ModItems.SPEED_UPGRADE_1.get())
                .pattern(" S ")
                .pattern("SGS")
                .pattern(" S ")
                .define('S', Items.SUGAR)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                .save(out, id("speed_upgrade_1"));
        ShapedRecipeBuilder.shaped(MISC, ModItems.SPEED_UPGRADE_2.get())
                .pattern("GSG")
                .pattern("SUS")
                .pattern("GSG")
                .define('G', Items.GOLD_INGOT)
                .define('S', Items.SUGAR)
                .define('U', ModItems.SPEED_UPGRADE_1.get())
                .unlockedBy("has_speed_upgrade_1", has(ModItems.SPEED_UPGRADE_1.get()))
                .save(out, id("speed_upgrade_2"));
        ShapedRecipeBuilder.shaped(MISC, ModItems.SPEED_UPGRADE_3.get())
                .pattern("DSD")
                .pattern("SUS")
                .pattern("DSD")
                .define('D', Items.DIAMOND)
                .define('S', Items.SUGAR)
                .define('U', ModItems.SPEED_UPGRADE_2.get())
                .unlockedBy("has_speed_upgrade_2", has(ModItems.SPEED_UPGRADE_2.get()))
                .save(out, id("speed_upgrade_3"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.FORTUNE_UPGRADE_1.get())
                .pattern(" G ")
                .pattern("GEG")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .define('E', Items.EMERALD)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(out, id("fortune_upgrade_1"));
        ShapedRecipeBuilder.shaped(MISC, ModItems.FORTUNE_UPGRADE_2.get())
                .pattern("GEG")
                .pattern("EFE")
                .pattern("GEG")
                .define('G', Items.GOLD_INGOT)
                .define('E', Items.EMERALD)
                .define('F', ModItems.FORTUNE_UPGRADE_1.get())
                .unlockedBy("has_fortune_upgrade_1", has(ModItems.FORTUNE_UPGRADE_1.get()))
                .save(out, id("fortune_upgrade_2"));
        ShapedRecipeBuilder.shaped(MISC, ModItems.FORTUNE_UPGRADE_3.get())
                .pattern("DED")
                .pattern("EFE")
                .pattern("DED")
                .define('D', Items.DIAMOND)
                .define('E', Items.EMERALD)
                .define('F', ModItems.FORTUNE_UPGRADE_2.get())
                .unlockedBy("has_fortune_upgrade_2", has(ModItems.FORTUNE_UPGRADE_2.get()))
                .save(out, id("fortune_upgrade_3"));
    }

    // ----------------------------------------------------------------- dolls

    private static void dolls(final RecipeOutput out) {
        doll(out, "doll_diamond", Items.DIAMOND);
        doll(out, "doll_emerald", Items.EMERALD);

        // The two nether dolls are symmetric: swapping the centre and bottom
        // ingredients gives the mirrored recipe, hence two ids per doll.
        netherDoll(out, "angry_doll", ModItems.ANGRY_DOLL.get(), Items.BLAZE_POWDER,
                Items.GLOWSTONE_DUST);
        netherDoll(out, "creepy_doll", ModItems.CREEPY_DOLL.get(), Items.INK_SAC,
                Items.LAPIS_LAZULI);

        ShapedRecipeBuilder.shaped(MISC, ModItems.SHULKER_DOLL.get())
                .pattern("xyx")
                .pattern("zwz")
                .pattern("xvx")
                .define('x', Items.PURPLE_DYE)
                .define('y', Items.END_STONE)
                .define('z', Items.GLOWSTONE_DUST)
                .define('v', Items.ENDER_PEARL)
                .define('w', ModItems.DOLL.get())
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                .save(out, id("shulker_doll"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.GUARDIAN_DOLL.get())
                .pattern("xyx")
                .pattern("zwz")
                .pattern("xvx")
                .define('x', Items.PRISMARINE_CRYSTALS)
                .define('y', Items.REDSTONE)
                .define('z', Items.GLOWSTONE_DUST)
                .define('v', Items.SALMON)
                .define('w', ModItems.DOLL.get())
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                .save(out, id("guardian_doll"));
    }

    private static void doll(final RecipeOutput out, final String name, final ItemLike centre) {
        ShapedRecipeBuilder.shaped(MISC, ModItems.DOLL.get())
                .pattern("xyx")
                .pattern(" x ")
                .pattern("x x")
                .define('x', ModItems.PORCELAIN.get())
                .define('y', centre)
                .unlockedBy("has_porcelain", has(ModItems.PORCELAIN.get()))
                .save(out, id(name));
    }

    private static void netherDoll(final RecipeOutput out, final String baseName, final ItemLike result,
                                   final ItemLike outer, final ItemLike accent) {
        dollPattern(out, baseName, result, outer, accent, Items.NETHER_WART, Items.REDSTONE);
        dollPattern(out, baseName + "_mirrored", result, outer, accent, Items.REDSTONE, Items.NETHER_WART);
    }

    private static void dollPattern(final RecipeOutput out, final String name, final ItemLike result,
                                    final ItemLike outer, final ItemLike accent, final ItemLike centre,
                                    final ItemLike bottom) {
        ShapedRecipeBuilder.shaped(MISC, result)
                .pattern("xyx")
                .pattern("zwz")
                .pattern("xvx")
                .define('x', outer)
                .define('y', centre)
                .define('z', accent)
                .define('w', ModItems.DOLL.get())
                .define('v', bottom)
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                .save(out, id(name));
    }

    // ------------------------------------------------------------- porcelain

    private static void porcelain(final RecipeOutput out) {
        ShapelessRecipeBuilder.shapeless(MISC, ModItems.PORCELAIN.get())
                .requires(Items.CLAY_BALL)
                .requires(Items.BONE_MEAL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(out, id("porcelain"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.UNFIRED_CRUCIBLE.get())
                .pattern("x x")
                .pattern("x x")
                .pattern("xxx")
                .define('x', ModItems.PORCELAIN.get())
                .unlockedBy("has_porcelain", has(ModItems.PORCELAIN.get()))
                .save(out, id("unfired_crucible"));

        ShapedRecipeBuilder.shaped(MISC, ModItems.UNFIRED_PORCELAIN_BUCKET.get())
                .pattern("x x")
                .pattern(" x ")
                .define('x', ModItems.PORCELAIN.get())
                .unlockedBy("has_porcelain", has(ModItems.PORCELAIN.get()))
                .save(out, id("unfired_porcelain_bucket"));

        smelt(out, "crucible_smelting", ModItems.UNFIRED_CRUCIBLE.get(), ModItems.CRUCIBLE.get(), 0.3F, 200);
        smelt(out, "porcelain_bucket_smelting", ModItems.UNFIRED_PORCELAIN_BUCKET.get(),
                ModItems.PORCELAIN_BUCKET.get(), 0.3F, 200);
    }

    // -------------------------------------------------------------- silkworm

    private static void silkwormCooking(final RecipeOutput out) {
        final ItemStack result = new ItemStack(ModItems.COOKED_SILKWORM.get());
        final Ingredient input = Ingredient.of(ModItems.SILKWORM.get());
        out.accept(id("cooked_silkworm_smelting"), new SmeltingRecipe("", CookingBookCategory.MISC,
                input, result, 0.1F, 200), null);
        out.accept(id("cooked_silkworm_smoking"), new SmokingRecipe("", CookingBookCategory.MISC,
                input, result, 0.1F, 100), null);
        out.accept(id("cooked_silkworm_campfire"), new CampfireCookingRecipe("", CookingBookCategory.MISC,
                input, result, 0.1F, 600), null);
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Cooking recipes are built directly rather than through
     * {@code SimpleCookingRecipeBuilder}, which would pick {@code food}/{@code blocks}
     * book categories; the hand-written JSON had none, i.e. {@code misc}.
     */
    private static void smelt(final RecipeOutput out, final String name, final ItemLike input,
                              final ItemLike result, final float experience, final int cookingTime,
                              final ICondition... conditions) {
        out.accept(id(name), new SmeltingRecipe("", CookingBookCategory.MISC, Ingredient.of(input),
                new ItemStack(result), experience, cookingTime), null, conditions);
    }

    /**
     * Drops the generated {@code advancement/recipes/**}: the mod has never shipped
     * recipe advancements, and adding them would silently change recipe-book unlocks.
     */
    private static RecipeOutput withoutAdvancements(final RecipeOutput delegate) {
        return new RecipeOutput() {
            @Override
            public void accept(final ResourceLocation id, final Recipe<?> recipe,
                               @Nullable final AdvancementHolder advancement, final ICondition... conditions) {
                delegate.accept(id, recipe, null, conditions);
            }

            @Override
            public Advancement.Builder advancement() {
                return delegate.advancement();
            }
        };
    }

    private static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, path);
    }

    /** Item id of a registered block/item, for the ids that only exist as strings. */
    private static Item requireItem(final String itemId) {
        final Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        if (item == null || item == Items.AIR) {
            throw new IllegalStateException("datagen recipe references unknown item " + itemId);
        }
        return item;
    }

    private static ItemLike blockItem(final String blockId) {
        final var holder = ModBlocks.BLOCKS.getEntries().stream()
                .filter(entry -> entry.getId().getPath().equals(blockId))
                .findFirst();
        if (holder.isEmpty()) {
            throw new IllegalStateException("datagen recipe references unknown block " + blockId);
        }
        return holder.get().get().asItem();
    }

    /** Registry path of an item, for criterion names only. */
    private static String path(final ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
