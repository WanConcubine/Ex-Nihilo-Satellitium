package com.kaifengcrt.exnihilosatellitium.compat.jei;

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

/** JEI page for one hammer rule: block in, result out (with the 4–6 range). */
public final class HammerRecipeCategory extends AbstractRecipeCategory<HammerRecipe> {

    private static final int WIDTH = 76;
    private static final int HEIGHT = 26;
    private static final int SLOT_Y = 4;
    private static final int INPUT_X = 4;
    private static final int ARROW_X = 26;
    private static final int OUTPUT_X = 54;

    private final IGuiHelper guiHelper;

    public HammerRecipeCategory(final IGuiHelper guiHelper) {
        super(HammerJeiPlugin.TYPE,
                Component.translatable("jei.exnihilosatellitium.hammer"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.IRON_HAMMER.get())),
                WIDTH,
                HEIGHT);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final HammerRecipe recipe,
                          final IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.input().asItem()))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.hammer.hint")));

        builder.addOutputSlot(OUTPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.result(),
                        recipe.single() ? Math.max(1, recipe.min()) : 1))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(recipe.single()
                        ? Component.translatable("jei.exnihilosatellitium.hammer.count_single",
                                recipe.min())
                        : Component.translatable("jei.exnihilosatellitium.hammer.count",
                                recipe.min(), recipe.max())));
    }

    @Override
    public void draw(final HammerRecipe recipe, final IRecipeSlotsView slotsView,
                     final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final IDrawable arrow = this.guiHelper.getRecipeArrow();
        arrow.draw(graphics, ARROW_X, 6);
    }
}
