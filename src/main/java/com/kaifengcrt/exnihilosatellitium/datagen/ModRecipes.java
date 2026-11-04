package com.kaifengcrt.exnihilosatellitium.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
            ItemTags.create(new ResourceLocation("forge", "rods/wooden"));

    private static final RecipeCategory MISC = RecipeCategory.MISC;

    public ModRecipes(final PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(final Consumer<FinishedRecipe> output) {
        // 1.20.1 has no RecipeOutput: wrap every finished recipe so the generated
        // advancement is dropped and optional `conditions` can be attached.
        final Consumer<FinishedRecipe> out = recipe -> output.accept(DatagenRecipe.of(recipe, null));
        final Map<String, JsonArray> gemPackConditions = gemPackConditions();

        oreChain(out, gemPackConditions);
        barrels(out);
        woodenCrucibles(out);
        sieveAndMeshes(out);
        pebbles(out);
        tools(out);
        machines(out);
        upgrades(out);
        dolls(out);
        villageExtras(out);
        porcelain(out);
        silkwormCooking(out);
    }

    /**
     * Pack-recipe conditions for mod-gated {@code gems}: the 2x2 recipe only exists
     * while one of its candidate mods is loaded. Metals have no condition.
     */
    private static Map<String, JsonArray> gemPackConditions() {
        final Map<String, JsonArray> out = new LinkedHashMap<>();
        for (final OreMaterial material : OreMaterials.all()) {
            if (!material.gem()) {
                continue;
            }
            final List<String> mods = new ArrayList<>();
            for (final OreMaterial.ProductCandidate candidate : material.candidates()) {
                if (candidate.mod() != null) {
                    mods.add(candidate.mod());
                }
            }
            if (!mods.isEmpty()) {
                out.put(material.id(), anyModLoaded(mods));
            }
        }
        return out;
    }

    /** {@code [{"type":"forge:or","values":[...]}]} or a single mod_loaded. */
    private static JsonArray anyModLoaded(final List<String> mods) {
        if (mods.size() == 1) {
            return modLoaded(mods);
        }
        final JsonObject or = new JsonObject();
        or.addProperty("type", "forge:or");
        or.add("values", modLoaded(mods));
        final JsonArray out = new JsonArray();
        out.add(or);
        return out;
    }

    // ------------------------------------------------------------ ore chain

    /**
     * Nine materials → 3 pack recipes + 3 smelting recipes each. Materials with an
     * {@code ingots} candidate list get a conditional base recipe (our own ingot
     * only while none of those mods is loaded); the per-mod variants stay hand-written.
     */
    private static void oreChain(final Consumer<FinishedRecipe> out,
                                 final Map<String, JsonArray> gemPackConditions) {
        for (final OreMaterial material : OreMaterials.all()) {
            final Map<String, RegistryObject<Item>> items =
                    ModOreItems.byMaterial().get(material.id());
            final Item broken = items.get("broken").get();
            final Item crushed = items.get("crushed").get();
            final Item powdered = items.get("powdered").get();
            final ItemLike gravel = items.get("ore_gravel").get();
            final ItemLike sand = items.get("ore_sand").get();
            final ItemLike dust = items.get("ore_dust").get();

            final JsonArray packConditions = gemPackConditions.get(material.id());
            pack(out, material.id() + "_broken_to_ore_gravel", broken, gravel, packConditions);
            pack(out, material.id() + "_crushed_to_ore_sand", crushed, sand, packConditions);
            pack(out, material.id() + "_powdered_to_ore_dust", powdered, dust, packConditions);

            // Gem/mineral entries have no own fallback: without the supported mod
            // the ore chain simply has no smelting endpoint. The per-mod variants
            // are hand-written, so datagen emits nothing for these.
            if (material.gem()) {
                continue;
            }

            final List<String> loadedMods = new ArrayList<>();
            for (final OreMaterial.ProductCandidate candidate : material.candidates()) {
                if (candidate.mod() != null) {
                    loadedMods.add(candidate.mod());
                }
            }
            final JsonArray conditions;
            if (material.candidates().isEmpty()) {
                conditions = null;
            } else if (loadedMods.isEmpty()) {
                // An unconditional candidate always wins, so there is no base recipe.
                ExNihiloSatellitium.LOGGER.warn("[datagen] {}: unconditional product candidate, "
                        + "no base smelting recipe generated", material.id());
                continue;
            } else {
                conditions = not(modLoaded(loadedMods));
            }

            final Item product = smeltingTarget(material);
            smelt(out, material.id() + "_ore_gravel_smelting", gravel, product, 0.1F, 200, conditions);
            smelt(out, material.id() + "_ore_sand_smelting", sand, product, 0.1F, 200, conditions);
            smelt(out, material.id() + "_ore_dust_smelting", dust, product, 0.1F, 200, conditions);
        }
    }

    /** The product our own recipes point at; for candidates that is always our fallback. */
    private static Item smeltingTarget(final OreMaterial material) {
        if (!material.candidates().isEmpty() || material.product() == null) {
            final var holder = ModOreItems.byMaterial().get(material.id()).get("product");
            if (holder == null) {
                throw new IllegalStateException("material " + material.id() + " has no own product item: "
                        + "run datagen without its supported mods on the classpath");
            }
            return holder.get();
        }
        return requireItem(material.product());
    }

    /** 2×2 of one refinement stage back into the ore block. */
    private static void pack(final Consumer<FinishedRecipe> out, final String name, final ItemLike input,
                             final ItemLike result, @Nullable final JsonArray conditions) {
        save(out, name, ShapedRecipeBuilder.shaped(MISC, result)
                .pattern("xx")
                .pattern("xx")
                .define('x', input)
                .unlockedBy("has_" + path(input), has(input)), conditions);
    }

    // -------------------------------------------------------------- barrels

    private static void barrels(final Consumer<FinishedRecipe> out) {
        for (final WoodTypes.Wood wood : WoodTypes.ALL) {
                    save(out, wood.barrel(), ShapedRecipeBuilder.shaped(MISC, blockItem(wood.barrel()))
                    .pattern("x x")
                    .pattern("x x")
                    .pattern("xyx")
                    .define('x', wood.planks())
                    .define('y', wood.slab())
                    .unlockedBy("has_planks", has(ItemTags.PLANKS))
                    );
        }

                save(out, "stone_barrel", ShapedRecipeBuilder.shaped(MISC, blockItem("stone_barrel"))
                .pattern("x x")
                .pattern("x x")
                .pattern("xyx")
                .define('x', Items.STONE)
                .define('y', Items.SMOOTH_STONE_SLAB)
                .unlockedBy("has_stone", has(Items.STONE))
                );
    }

    // ----------------------------------------------------- wooden crucibles

    private static void woodenCrucibles(final Consumer<FinishedRecipe> out) {
        for (final WoodTypes.Wood wood : WoodTypes.ALL) {
                    save(out, wood.crucible(), ShapedRecipeBuilder.shaped(MISC, blockItem(wood.crucible()))
                    .pattern("x x")
                    .pattern("x x")
                    .pattern("xyx")
                    .define('x', wood.log())
                    .define('y', ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_log", has(ItemTags.LOGS))
                    );
        }
    }

    // ------------------------------------------------------ sieve and meshes

    private static void sieveAndMeshes(final Consumer<FinishedRecipe> out) {
                save(out, "sieve", ShapedRecipeBuilder.shaped(MISC, ModItems.SIEVE.get())
                .pattern("x x")
                .pattern("xyx")
                .pattern("z z")
                .define('x', ItemTags.PLANKS)
                .define('y', ItemTags.WOODEN_SLABS)
                .define('z', WOODEN_RODS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                );

        mesh(out, "wood_mesh", ModItems.WOOD_MESH.get(), Items.STICK);
        mesh(out, "silk_mesh", ModItems.SILK_MESH.get(), Items.STRING);
        mesh(out, "iron_mesh", ModItems.IRON_MESH.get(), Items.IRON_BARS);
    }

    private static void mesh(final Consumer<FinishedRecipe> out, final String name, final ItemLike result,
                             final ItemLike material) {
                save(out, name, ShapedRecipeBuilder.shaped(MISC, result)
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', material)
                .unlockedBy("has_" + path(material), has(material))
                );
    }

    // ----------------------------------------------------------------- tools

    private static void tools(final Consumer<FinishedRecipe> out) {
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
                .save(out, id("netherite_hammer").toString());

                save(out, "wooden_crook", ShapedRecipeBuilder.shaped(MISC, ModItems.WOODEN_CROOK.get())
                .pattern("xx ")
                .pattern(" x ")
                .pattern(" x ")
                .define('x', WOODEN_RODS)
                .unlockedBy("has_rods", has(WOODEN_RODS))
                );

                save(out, "bone_crook", ShapedRecipeBuilder.shaped(MISC, ModItems.BONE_CROOK.get())
                .pattern("xx ")
                .pattern(" x ")
                .pattern(" x ")
                .define('x', Items.BONE)
                .unlockedBy("has_bone", has(Items.BONE))
                );
    }

    private static void hammer(final Consumer<FinishedRecipe> out, final String name, final ItemLike result,
                               final TagKey<Item> head) {
        hammer(out, name, result, Ingredient.of(head));
    }

    private static void hammer(final Consumer<FinishedRecipe> out, final String name, final ItemLike result,
                               final Ingredient head) {
                save(out, name, ShapedRecipeBuilder.shaped(MISC, result)
                .pattern(" x ")
                .pattern(" yx")
                .pattern("y  ")
                .define('x', head)
                .define('y', WOODEN_RODS)
                .unlockedBy("has_rods", has(WOODEN_RODS))
                );
    }

    // --------------------------------------------------------------- pebbles

    /** Four pebbles of one kind craft the matching stone block (2x2). */
    private static void pebbles(final Consumer<FinishedRecipe> out) {
        pebble(out, ModItems.COBBLESTONE_PEBBLE.get(), Items.COBBLESTONE);
        pebble(out, ModItems.ANDESITE_PEBBLE.get(), Items.ANDESITE);
        pebble(out, ModItems.DIORITE_PEBBLE.get(), Items.DIORITE);
        pebble(out, ModItems.GRANITE_PEBBLE.get(), Items.GRANITE);
    }

    private static void pebble(final Consumer<FinishedRecipe> out, final ItemLike pebble, final ItemLike block) {
        final String pebbleName = path(pebble);
        final String blockName = path(block);
                save(out, pebbleName + "_to_" + blockName, ShapedRecipeBuilder.shaped(MISC, block)
                .pattern("xx")
                .pattern("xx")
                .define('x', pebble)
                .unlockedBy("has_" + pebbleName, has(pebble))
                );
    }

    // -------------------------------------------------------------- machines

    private static void machines(final Consumer<FinishedRecipe> out) {
                save(out, "electric_sieve", ShapedRecipeBuilder.shaped(MISC, ModItems.ELECTRIC_SIEVE.get())
                .pattern("IRI")
                .pattern("RBR")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('B', Items.IRON_BLOCK)
                .define('S', ModItems.SIEVE.get())
                .unlockedBy("has_sieve", has(ModItems.SIEVE.get()))
                );

                save(out, "electric_hammer", ShapedRecipeBuilder.shaped(MISC, ModItems.ELECTRIC_HAMMER.get())
                .pattern("IRI")
                .pattern("RHR")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('H', ModItems.IRON_HAMMER.get())
                .unlockedBy("has_iron_hammer", has(ModItems.IRON_HAMMER.get()))
                );

                save(out, "fuel_sieve", ShapedRecipeBuilder.shaped(MISC, ModItems.FUEL_SIEVE.get())
                .pattern("ICI")
                .pattern("CFC")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('F', Items.FURNACE)
                .define('S', ModItems.SIEVE.get())
                .unlockedBy("has_sieve", has(ModItems.SIEVE.get()))
                );

                save(out, "fuel_hammer", ShapedRecipeBuilder.shaped(MISC, ModItems.FUEL_HAMMER.get())
                .pattern("ICI")
                .pattern("CFC")
                .pattern("IHI")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('F', Items.FURNACE)
                .define('H', ModItems.IRON_HAMMER.get())
                .unlockedBy("has_iron_hammer", has(ModItems.IRON_HAMMER.get()))
                );
    }

    // -------------------------------------------------------------- upgrades

    private static void upgrades(final Consumer<FinishedRecipe> out) {
                save(out, "speed_upgrade_1", ShapedRecipeBuilder.shaped(MISC, ModItems.SPEED_UPGRADE_1.get())
                .pattern(" S ")
                .pattern("SGS")
                .pattern(" S ")
                .define('S', Items.SUGAR)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                );
                save(out, "speed_upgrade_2", ShapedRecipeBuilder.shaped(MISC, ModItems.SPEED_UPGRADE_2.get())
                .pattern("GSG")
                .pattern("SUS")
                .pattern("GSG")
                .define('G', Items.GOLD_INGOT)
                .define('S', Items.SUGAR)
                .define('U', ModItems.SPEED_UPGRADE_1.get())
                .unlockedBy("has_speed_upgrade_1", has(ModItems.SPEED_UPGRADE_1.get()))
                );
                save(out, "speed_upgrade_3", ShapedRecipeBuilder.shaped(MISC, ModItems.SPEED_UPGRADE_3.get())
                .pattern("DSD")
                .pattern("SUS")
                .pattern("DSD")
                .define('D', Items.DIAMOND)
                .define('S', Items.SUGAR)
                .define('U', ModItems.SPEED_UPGRADE_2.get())
                .unlockedBy("has_speed_upgrade_2", has(ModItems.SPEED_UPGRADE_2.get()))
                );

                save(out, "fortune_upgrade_1", ShapedRecipeBuilder.shaped(MISC, ModItems.FORTUNE_UPGRADE_1.get())
                .pattern(" G ")
                .pattern("GEG")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .define('E', Items.EMERALD)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                );
                save(out, "fortune_upgrade_2", ShapedRecipeBuilder.shaped(MISC, ModItems.FORTUNE_UPGRADE_2.get())
                .pattern("GEG")
                .pattern("EFE")
                .pattern("GEG")
                .define('G', Items.GOLD_INGOT)
                .define('E', Items.EMERALD)
                .define('F', ModItems.FORTUNE_UPGRADE_1.get())
                .unlockedBy("has_fortune_upgrade_1", has(ModItems.FORTUNE_UPGRADE_1.get()))
                );
                save(out, "fortune_upgrade_3", ShapedRecipeBuilder.shaped(MISC, ModItems.FORTUNE_UPGRADE_3.get())
                .pattern("DED")
                .pattern("EFE")
                .pattern("DED")
                .define('D', Items.DIAMOND)
                .define('E', Items.EMERALD)
                .define('F', ModItems.FORTUNE_UPGRADE_2.get())
                .unlockedBy("has_fortune_upgrade_2", has(ModItems.FORTUNE_UPGRADE_2.get()))
                );
    }

    // ----------------------------------------------------------------- dolls

    private static void dolls(final Consumer<FinishedRecipe> out) {
        doll(out, "doll_diamond", Items.DIAMOND);
        doll(out, "doll_emerald", Items.EMERALD);

        // The two nether dolls are symmetric: swapping the centre and bottom
        // ingredients gives the mirrored recipe, hence two ids per doll.
        netherDoll(out, "angry_doll", ModItems.ANGRY_DOLL.get(), Items.BLAZE_POWDER,
                Items.GLOWSTONE_DUST);
        netherDoll(out, "creepy_doll", ModItems.CREEPY_DOLL.get(), Items.INK_SAC,
                Items.LAPIS_LAZULI);

                save(out, "shulker_doll", ShapedRecipeBuilder.shaped(MISC, ModItems.SHULKER_DOLL.get())
                .pattern("xyx")
                .pattern("zwz")
                .pattern("xvx")
                .define('x', Items.PURPLE_DYE)
                .define('y', Items.END_STONE)
                .define('z', Items.GLOWSTONE_DUST)
                .define('v', Items.ENDER_PEARL)
                .define('w', ModItems.DOLL.get())
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                );

                save(out, "guardian_doll", ShapedRecipeBuilder.shaped(MISC, ModItems.GUARDIAN_DOLL.get())
                .pattern("xyx")
                .pattern("zwz")
                .pattern("xvx")
                .define('x', Items.PRISMARINE_CRYSTALS)
                .define('y', Items.REDSTONE)
                .define('z', Items.GLOWSTONE_DUST)
                .define('v', Items.SALMON)
                .define('w', ModItems.DOLL.get())
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                );

                save(out, "villager_doll", ShapedRecipeBuilder.shaped(MISC, ModItems.VILLAGER_DOLL.get())
                .pattern("eee")
                .pattern("eDe")
                .pattern("eee")
                .define('e', Items.EMERALD)
                .define('D', ModItems.DOLL.get())
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                );
    }

    /** Brain Platinum, and the honey-bottle beehive alternative. */
    private static void villageExtras(final Consumer<FinishedRecipe> out) {
        save(out, "brain_platinum", ShapedRecipeBuilder.shaped(MISC, ModItems.BRAIN_PLATINUM.get())
                .pattern("ese")
                .pattern("sMs")
                .pattern("ese")
                .define('e', Items.EGG)
                .define('s', Items.SUGAR)
                .define('M', Items.EMERALD)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                );

        save(out, "beehive_honey_bottles", ShapedRecipeBuilder.shaped(MISC, Items.BEEHIVE)
                .pattern("ppp")
                .pattern("hhh")
                .pattern("ppp")
                .define('p', ItemTags.PLANKS)
                .define('h', Items.HONEY_BOTTLE)
                .unlockedBy("has_honey_bottle", has(Items.HONEY_BOTTLE))
                );
    }

    private static void doll(final Consumer<FinishedRecipe> out, final String name, final ItemLike centre) {
                save(out, name, ShapedRecipeBuilder.shaped(MISC, ModItems.DOLL.get())
                .pattern("xyx")
                .pattern(" x ")
                .pattern("x x")
                .define('x', ModItems.PORCELAIN.get())
                .define('y', centre)
                .unlockedBy("has_porcelain", has(ModItems.PORCELAIN.get()))
                );
    }

    private static void netherDoll(final Consumer<FinishedRecipe> out, final String baseName, final ItemLike result,
                                   final ItemLike outer, final ItemLike accent) {
        dollPattern(out, baseName, result, outer, accent, Items.NETHER_WART, Items.REDSTONE);
        dollPattern(out, baseName + "_mirrored", result, outer, accent, Items.REDSTONE, Items.NETHER_WART);
    }

    private static void dollPattern(final Consumer<FinishedRecipe> out, final String name, final ItemLike result,
                                    final ItemLike outer, final ItemLike accent, final ItemLike centre,
                                    final ItemLike bottom) {
                save(out, name, ShapedRecipeBuilder.shaped(MISC, result)
                .pattern("xyx")
                .pattern("zwz")
                .pattern("xvx")
                .define('x', outer)
                .define('y', centre)
                .define('z', accent)
                .define('w', ModItems.DOLL.get())
                .define('v', bottom)
                .unlockedBy("has_doll", has(ModItems.DOLL.get()))
                );
    }

    // ------------------------------------------------------------- porcelain

    private static void porcelain(final Consumer<FinishedRecipe> out) {
                save(out, "porcelain", ShapelessRecipeBuilder.shapeless(MISC, ModItems.PORCELAIN.get())
                .requires(Items.CLAY_BALL)
                .requires(Items.BONE_MEAL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                );

                save(out, "unfired_crucible", ShapedRecipeBuilder.shaped(MISC, ModItems.UNFIRED_CRUCIBLE.get())
                .pattern("x x")
                .pattern("x x")
                .pattern("xxx")
                .define('x', ModItems.PORCELAIN.get())
                .unlockedBy("has_porcelain", has(ModItems.PORCELAIN.get()))
                );

                save(out, "unfired_porcelain_bucket", ShapedRecipeBuilder.shaped(MISC, ModItems.UNFIRED_PORCELAIN_BUCKET.get())
                .pattern("x x")
                .pattern(" x ")
                .define('x', ModItems.PORCELAIN.get())
                .unlockedBy("has_porcelain", has(ModItems.PORCELAIN.get()))
                );

        smelt(out, "crucible_smelting", ModItems.UNFIRED_CRUCIBLE.get(), ModItems.CRUCIBLE.get(), 0.3F, 200,
                null);
        smelt(out, "porcelain_bucket_smelting", ModItems.UNFIRED_PORCELAIN_BUCKET.get(),
                ModItems.PORCELAIN_BUCKET.get(), 0.3F, 200, null);
    }

    // -------------------------------------------------------------- silkworm

    private static void silkwormCooking(final Consumer<FinishedRecipe> out) {
        final ItemStack result = new ItemStack(ModItems.COOKED_SILKWORM.get());
        final Ingredient input = Ingredient.of(ModItems.SILKWORM.get());
        out.accept(DatagenRecipe.cooking(id("cooked_silkworm_smelting"), RecipeSerializer.SMELTING_RECIPE,
                input, result, 0.1F, 200, null));
        out.accept(DatagenRecipe.cooking(id("cooked_silkworm_smoking"), RecipeSerializer.SMOKING_RECIPE,
                input, result, 0.1F, 100, null));
        out.accept(DatagenRecipe.cooking(id("cooked_silkworm_campfire"),
                RecipeSerializer.CAMPFIRE_COOKING_RECIPE, input, result, 0.1F, 600, null));
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Cooking recipes are built directly rather than through
     * {@code SimpleCookingRecipeBuilder}, which would pick {@code food}/{@code blocks}
     * book categories; the shipped JSON has none, i.e. {@code misc}.
     */
    private static void smelt(final Consumer<FinishedRecipe> out, final String name, final ItemLike input,
                              final ItemLike result, final float experience, final int cookingTime,
                              @Nullable final JsonArray conditions) {
        final ResourceLocation id = id(name);
        out.accept(DatagenRecipe.cooking(id, RecipeSerializer.SMELTING_RECIPE,
                Ingredient.of(input), new ItemStack(result), experience, cookingTime, conditions));
    }

    /**
     * 1.20.1's {@code save(Consumer, String)} refuses an id equal to the default one
     * (the result item's registry name), so route every builder through here.
     */
    private static void save(final Consumer<FinishedRecipe> out, final String name,
                             final RecipeBuilder builder) {
        save(out, name, builder, null);
    }

    private static void save(final Consumer<FinishedRecipe> out, final String name,
                             final RecipeBuilder builder, @Nullable final JsonArray conditions) {
        final Consumer<FinishedRecipe> target = conditions == null
                ? out
                : recipe -> out.accept(DatagenRecipe.of(recipe, conditions));
        final ResourceLocation wanted = id(name);
        if (RecipeBuilder.getDefaultRecipeId(builder.getResult()).equals(wanted)) {
            builder.save(target);
        } else {
            // The id must be namespace-qualified: a bare path would resolve to
            // `minecraft:<path>` and the file would land under data/minecraft/.
            builder.save(target, wanted.toString());
        }
    }

    /** {@code [{"type":"forge:mod_loaded","modid":...}, ...]} — the list is ANDed. */
    private static JsonArray modLoaded(final List<String> mods) {
        final JsonArray array = new JsonArray();
        for (final String mod : mods) {
            final JsonObject condition = new JsonObject();
            condition.addProperty("type", "forge:mod_loaded");
            condition.addProperty("modid", mod);
            array.add(condition);
        }
        return array;
    }

    /** {@code [{"type":"forge:not","value": <mod_loaded>}]}, or an {@code or} of several. */
    private static JsonArray not(final JsonArray modLoaded) {
        final JsonObject value;
        if (modLoaded.size() == 1) {
            value = modLoaded.get(0).getAsJsonObject();
        } else {
            value = new JsonObject();
            value.addProperty("type", "forge:or");
            value.add("values", modLoaded);
        }
        final JsonObject negated = new JsonObject();
        negated.addProperty("type", "forge:not");
        negated.add("value", value);
        final JsonArray array = new JsonArray();
        array.add(negated);
        return array;
    }

    private static ResourceLocation id(final String path) {
        return new ResourceLocation(ExNihiloSatellitium.MOD_ID, path);
    }

    /** Item id of a registered item, for the ids that only exist as strings. */
    private static Item requireItem(final String itemId) {
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
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
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }
}
