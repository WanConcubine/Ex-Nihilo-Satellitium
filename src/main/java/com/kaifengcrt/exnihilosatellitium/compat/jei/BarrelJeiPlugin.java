package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
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
 * JEI pages for the barrel: fluid/item transformations and the compost table.
 *
 * <p>Both categories read the same registries the block entity uses, so a JSON
 * edit shows up in JEI without touching this class. The plugin class is only
 * loaded when JEI is installed; nothing from JEI is shipped in our jar.</p>
 */
@JeiPlugin
public final class BarrelJeiPlugin implements IModPlugin {

    public static final RecipeType<BarrelRecipes.Recipe> TRANSFORM_TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "barrel", BarrelRecipes.Recipe.class);
    public static final RecipeType<CompostRecipe> COMPOST_TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "barrel_compost", CompostRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "barrel");
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new BarrelRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CompostRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final var transforms = BarrelRecipes.all();
        registration.addRecipes(TRANSFORM_TYPE, transforms);
        final var compost = CompostRecipe.all();
        registration.addRecipes(COMPOST_TYPE, compost);
        ExNihiloSatellitium.LOGGER.info(
                "[jei] registered {} barrel transforms, {} compost entries",
                transforms.size(), compost.size());
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registration) {
        for (final DeferredHolder<Item, Item> barrel : ModItems.BARREL_ITEMS) {
            registration.addRecipeCatalyst(new ItemStack(barrel.get()), TRANSFORM_TYPE, COMPOST_TYPE);
        }
    }
}
