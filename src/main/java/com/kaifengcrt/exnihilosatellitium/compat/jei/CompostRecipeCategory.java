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
import net.minecraft.world.item.Items;

/**
 * JEI page for barrel composting: any compostable item -> dirt after 1000 points.
 *
 * <p>One page per {@code compost/*.json} entry; tag entries expand to their
 * item list. The tooltip carries the entry's point value.</p>
 */
public final class CompostRecipeCategory extends AbstractRecipeCategory<CompostRecipe> {

    private static final int WIDTH = 74;
    private static final int HEIGHT = 26;
    private static final int SLOT_Y = 4;
    private static final int INPUT_X = 4;
    private static final int ARROW_X = 26;
    private static final int OUTPUT_X = 54;

    private final IGuiHelper guiHelper;

    public CompostRecipeCategory(final IGuiHelper guiHelper) {
        super(BarrelJeiPlugin.COMPOST_TYPE,
                Component.translatable("jei.exnihilosatellitium.barrel_compost"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.BARREL.get())),
                WIDTH,
                HEIGHT);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final CompostRecipe recipe,
                          final IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.barrel_compost.value", recipe.value())));

        builder.addOutputSlot(OUTPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(Items.DIRT))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.barrel_compost.hint")));
    }

    @Override
    public void draw(final CompostRecipe recipe, final IRecipeSlotsView slotsView,
                     final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final IDrawable arrow = this.guiHelper.getRecipeArrow();
        arrow.draw(graphics, ARROW_X, 6);
    }
}
