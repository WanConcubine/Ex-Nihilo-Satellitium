package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
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

/** JEI page for one crook leaf table: leaves in, extra drops out. */
public final class CrookRecipeCategory extends AbstractRecipeCategory<CrookRecipe> {

    private static final int WIDTH = 104;
    private static final int HEIGHT = 26;
    private static final int SLOT_Y = 4;
    private static final int INPUT_X = 4;
    private static final int ARROW_X = 26;
    private static final int FIRST_OUTPUT_X = 46;
    private static final int OUTPUT_STEP = 18;

    private final IGuiHelper guiHelper;

    public CrookRecipeCategory(final IGuiHelper guiHelper) {
        super(CrookJeiPlugin.TYPE,
                Component.translatable("jei.exnihilosatellitium.crook"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.WOODEN_CROOK.get())),
                WIDTH,
                HEIGHT);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final CrookRecipe recipe,
                          final IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.leaves().asItem()))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.crook.hint")));

        for (int index = 0; index < recipe.drops().size(); index++) {
            final CrookDrops.Drop drop = recipe.drops().get(index);
            final int percent = Math.round(drop.chance() * 100.0F);
            builder.addOutputSlot(FIRST_OUTPUT_X + index * OUTPUT_STEP, SLOT_Y)
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(drop.item()))
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.translatable(
                                "jei.exnihilosatellitium.crook.chance", percent));
                        tooltip.add(Component.translatable(
                                "jei.exnihilosatellitium.crook.bone"));
                    });
        }
    }

    @Override
    public void draw(final CrookRecipe recipe, final IRecipeSlotsView slotsView,
                     final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final IDrawable arrow = this.guiHelper.getRecipeArrow();
        arrow.draw(graphics, ARROW_X, 6);
    }
}
