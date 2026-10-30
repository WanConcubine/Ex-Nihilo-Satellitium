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

/** JEI: the hammer crush table (input block → item + count range). */
@JeiPlugin
public final class HammerJeiPlugin implements IModPlugin {

    public static final RecipeType<HammerRecipe> TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "hammer", HammerRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "hammer");
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new HammerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final List<HammerRecipe> recipes = HammerRecipe.all();
        registration.addRecipes(TYPE, recipes);
        ExNihiloSatellitium.LOGGER.info("[jei] registered {} hammer recipes", recipes.size());
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registration) {
        ModItems.HAMMERS.forEach(hammer ->
                registration.addRecipeCatalyst(new ItemStack(hammer.get()), TYPE));
    }
}
