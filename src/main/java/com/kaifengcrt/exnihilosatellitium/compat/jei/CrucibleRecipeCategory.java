package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI page for crucible melting: one block in, one fluid out.
 *
 * <p>Uses JEI's native slot backgrounds and fluid renderer (same rule as the
 * barrel pages: no hand-drawn backgrounds).</p>
 */
public final class CrucibleRecipeCategory extends AbstractRecipeCategory<CrucibleRecipes.Melt> {

    private static final int WIDTH = 76;
    private static final int HEIGHT = 26;
    private static final int SLOT_Y = 4;
    private static final int INPUT_X = 4;
    private static final int ARROW_X = 26;
    private static final int OUTPUT_X = 54;

    private final IGuiHelper guiHelper;

    public CrucibleRecipeCategory(final IGuiHelper guiHelper) {
        super(CrucibleJeiPlugin.MELT_TYPE,
                Component.translatable("jei.exnihilosatellitium.crucible"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.CRUCIBLE.get())),
                WIDTH,
                HEIGHT);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final CrucibleRecipes.Melt recipe,
                          final IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.input().asItem()))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.crucible.hint")));

        builder.addOutputSlot(OUTPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.amount(), false, 16, 16)
                .addFluidStack(recipe.fluid(), recipe.amount())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.crucible.amount", recipe.amount())));
    }

    @Override
    public void draw(final CrucibleRecipes.Melt recipe, final IRecipeSlotsView slotsView,
                     final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final IDrawable arrow = this.guiHelper.getRecipeArrow();
        arrow.draw(graphics, ARROW_X, 6);
    }
}
