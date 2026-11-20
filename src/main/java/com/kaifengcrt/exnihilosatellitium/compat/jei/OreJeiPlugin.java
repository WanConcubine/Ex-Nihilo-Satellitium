package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Hides the ore materials that {@code ores.hideUnprovided} suppresses.
 *
 * <p>The sift/hammer tables are already filtered server-side, so their JEI pages
 * disappear on their own. What is left is the item list and the vanilla recipes
 * (broken -> ore gravel, smelting), which are static JSON and cannot know about
 * the config; this plugin removes those ingredients and recipe pages at runtime.
 * Loaded only when JEI is installed.</p>
 */
@JeiPlugin
public final class OreJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ExNihiloSatellitium.MOD_ID, "ore_hiding");
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime runtime) {
        final Set<String> hidden = OreMaterials.hiddenPrefixes();
        if (hidden.isEmpty()) {
            return;
        }
        final List<ItemStack> stacks = hiddenStacks();
        if (!stacks.isEmpty()) {
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        }
        hideVanillaRecipes(runtime, hidden);
        ExNihiloSatellitium.LOGGER.info(
                "[jei] ores.hideUnprovided: hid {} ({} item stacks)", hidden, stacks.size());
    }

    /** Every registered item of the hidden materials: ore blocks, the three stages, the product. */
    private static List<ItemStack> hiddenStacks() {
        final List<ItemStack> out = new ArrayList<>();
        for (final OreMaterial material : OreMaterials.all()) {
            if (!OreMaterials.hidden(material)) {
                continue;
            }
            final var entries = ModOreItems.byMaterial().get(material.id());
            if (entries == null) {
                continue;
            }
            for (final String kind : ModOreBlocks.KINDS) {
                out.add(new ItemStack(entries.get("ore_" + kind).get()));
            }
            for (final String stage : ModOreItems.STAGES) {
                out.add(new ItemStack(entries.get(stage).get()));
            }
            final var product = entries.get("product");
            if (product != null) {
                out.add(new ItemStack(product.get()));
            }
        }
        return out;
    }

    /** Hides the vanilla recipe pages whose id starts with a hidden material id. */
    private static void hideVanillaRecipes(final IJeiRuntime runtime, final Set<String> hidden) {
        final var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        final var recipes = level.getRecipeManager();

        final List<CraftingRecipe> crafting = new ArrayList<>();
        for (final CraftingRecipe recipe : recipes.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (OreMaterials.hides(hidden, recipe.getId())) {
                crafting.add(recipe);
            }
        }
        if (!crafting.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(RecipeTypes.CRAFTING, crafting);
        }

        final List<SmeltingRecipe> smelting = new ArrayList<>();
        for (final SmeltingRecipe recipe : recipes.getAllRecipesFor(RecipeType.SMELTING)) {
            if (OreMaterials.hides(hidden, recipe.getId())) {
                smelting.add(recipe);
            }
        }
        if (!smelting.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(RecipeTypes.SMELTING, smelting);
        }

        final List<BlastingRecipe> blasting = new ArrayList<>();
        for (final BlastingRecipe recipe : recipes.getAllRecipesFor(RecipeType.BLASTING)) {
            if (OreMaterials.hides(hidden, recipe.getId())) {
                blasting.add(recipe);
            }
        }
        if (!blasting.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(RecipeTypes.BLASTING, blasting);
        }
    }
}
