package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Optional JEI integration. The class is only loaded when JEI is installed;
 * nothing from JEI is shipped in our jar.
 */
@JeiPlugin
public final class SieveJeiPlugin implements IModPlugin {

    public static final RecipeType<SieveRecipe> RECIPE_TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "sieve", SieveRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "sieve");
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new SieveRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final var recipes = SieveRecipe.all();
        registration.addRecipes(RECIPE_TYPE, recipes);
        ExNihiloSatellitium.LOGGER.info("[jei] registered {} sieve recipes", recipes.size());
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.SIEVE.get()), RECIPE_TYPE);
        // Looking up a mesh also shows the sieve recipes it can be used in.
        for (final DeferredHolder<Item, Item> mesh : ModItems.MESHES) {
            registration.addRecipeCatalyst(new ItemStack(mesh.get()), RECIPE_TYPE);
        }
    }
}
