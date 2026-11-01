package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * JEI page for a barrel transformation: base fluid + additive -> block.
 *
 * <p>All three slots use JEI's native slot background. Fluid slots also use
 * JEI's fluid renderer, so the tank contents are drawn like in a fluid
 * recipe instead of as a bucket item.</p>
 */
public final class BarrelRecipeCategory extends AbstractRecipeCategory<BarrelRecipes.Recipe> {

    private static final int WIDTH = 116;
    private static final int HEIGHT = 26;
    private static final int SLOT_Y = 4;
    private static final int BASE_X = 4;
    private static final int PLUS_X = 26;
    private static final int ADDED_X = 42;
    private static final int ARROW_X = 64;
    private static final int RESULT_X = 90;

    private final IGuiHelper guiHelper;

    public BarrelRecipeCategory(final IGuiHelper guiHelper) {
        super(BarrelJeiPlugin.TRANSFORM_TYPE,
                Component.translatable("jei.exnihilosatellitium.barrel"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.BARREL.get())),
                WIDTH,
                HEIGHT);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final BarrelRecipes.Recipe recipe,
                          final IFocusGroup focuses) {
        final Fluid base = recipe.base();
        builder.addInputSlot(BASE_X, SLOT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(BarrelBlockEntity.CAPACITY, false, 16, 16)
                .addFluidStack(base, BarrelBlockEntity.CAPACITY)
                .addRichTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable("jei.exnihilosatellitium.barrel.base"));
                    if (base == Fluids.LAVA) {
                        tooltip.add(Component.translatable("jei.exnihilosatellitium.barrel.base_lava"));
                    }
                });

        if (recipe.isFluidReaction()) {
            builder.addInputSlot(ADDED_X, SLOT_Y)
                    .setStandardSlotBackground()
                    .setFluidRenderer(BarrelBlockEntity.CAPACITY, false, 16, 16)
                    .addFluidStack(recipe.addedFluid(), BarrelBlockEntity.CAPACITY)
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                            Component.translatable("jei.exnihilosatellitium.barrel.added_fluid")));
        } else {
            builder.addInputSlot(ADDED_X, SLOT_Y)
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(recipe.addedItem()))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                            Component.translatable("jei.exnihilosatellitium.barrel.added_item")));
        }

        builder.addOutputSlot(RESULT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(recipe.result())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.barrel.result")));
    }

    @Override
    public void draw(final BarrelRecipes.Recipe recipe, final IRecipeSlotsView slotsView,
                     final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final IDrawable plus = this.guiHelper.getRecipePlusSign();
        plus.draw(graphics, PLUS_X, 7);
        this.guiHelper.getRecipeArrow().draw(graphics, ARROW_X, 6);
    }
}
