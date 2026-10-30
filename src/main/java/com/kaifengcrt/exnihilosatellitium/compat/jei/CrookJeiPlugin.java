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
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** JEI: crook leaf tables (leaves → extra sapling/apple/silkworm drops). */
@JeiPlugin
public final class CrookJeiPlugin implements IModPlugin {

    public static final RecipeType<CrookRecipe> TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "crook", CrookRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "crook");
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CrookRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final List<CrookRecipe> recipes = CrookRecipe.all();
        registration.addRecipes(TYPE, recipes);
        ExNihiloSatellitium.LOGGER.info("[jei] registered {} crook recipes", recipes.size());
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registration) {
        ModItems.CROOKS.forEach(crook ->
                registration.addRecipeCatalyst(new ItemStack(crook.get()), TYPE));
    }
}
