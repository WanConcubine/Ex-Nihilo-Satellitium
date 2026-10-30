package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
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

/** JEI: crucible melting table + the heat sources that drive it. */
@JeiPlugin
public final class CrucibleJeiPlugin implements IModPlugin {

    public static final RecipeType<CrucibleRecipes.Melt> MELT_TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "crucible", CrucibleRecipes.Melt.class);
    public static final RecipeType<CrucibleHeat.HeatSource> HEAT_TYPE =
            RecipeType.create(ExNihiloSatellitium.MOD_ID, "crucible_heat", CrucibleHeat.HeatSource.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "crucible");
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CrucibleRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CrucibleHeatCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final List<CrucibleRecipes.Melt> melts = List.copyOf(CrucibleRecipes.all());
        final List<CrucibleHeat.HeatSource> heat = List.copyOf(CrucibleHeat.all());
        registration.addRecipes(MELT_TYPE, melts);
        registration.addRecipes(HEAT_TYPE, heat);
        ExNihiloSatellitium.LOGGER.info(
                "[jei] registered {} crucible meltables, {} heat sources", melts.size(), heat.size());
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registration) {
        final ItemStack crucible = new ItemStack(ModItems.CRUCIBLE.get());
        registration.addRecipeCatalyst(crucible, MELT_TYPE);
        registration.addRecipeCatalyst(crucible, HEAT_TYPE);
    }
}
