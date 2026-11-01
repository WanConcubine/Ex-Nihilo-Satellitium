package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Runtime hook for the machine screens: clicking the progress bar opens JEI with
 * the current input as the focus (or the whole sieve/hammer category when the
 * input slot is empty).
 *
 * <p>The plugin also keeps the JEI runtime handle; without JEI installed this
 * class is never loaded (and must not be touched — see {@code MachineGui}).</p>
 */
@JeiPlugin
public final class MachineJeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ExNihiloSatellitium.MOD_ID, "machine");
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    /**
     * @return {@code true} when JEI was open and the request was handled
     */
    public static boolean showFor(final ItemStack input, final boolean sieve) {
        final IJeiRuntime current = runtime;
        if (current == null) {
            return false;
        }
        if (!input.isEmpty()) {
            final IFocus<ItemStack> focus = current.getJeiHelpers().getFocusFactory()
                    .createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, input);
            current.getRecipesGui().show(List.<IFocus<?>>of(focus));
        } else {
            current.getRecipesGui().showTypes(List.<RecipeType<?>>of(
                    sieve ? SieveJeiPlugin.RECIPE_TYPE : HammerJeiPlugin.TYPE));
        }
        return true;
    }
}
