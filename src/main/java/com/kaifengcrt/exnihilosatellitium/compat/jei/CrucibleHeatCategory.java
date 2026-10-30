package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** JEI page listing the blocks that heat a crucible and their mB/tick. */
public final class CrucibleHeatCategory extends AbstractRecipeCategory<CrucibleHeat.HeatSource> {

    private static final int WIDTH = 92;
    private static final int HEIGHT = 26;
    private static final int SLOT_Y = 4;
    private static final int INPUT_X = 4;
    private static final int TEXT_X = 28;
    private static final int TEXT_Y = 9;

    public CrucibleHeatCategory(final IGuiHelper guiHelper) {
        super(CrucibleJeiPlugin.HEAT_TYPE,
                Component.translatable("jei.exnihilosatellitium.crucible_heat"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.CAMPFIRE)),
                WIDTH,
                HEIGHT);
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final CrucibleHeat.HeatSource recipe,
                          final IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(recipe.icon())
                .addRichTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable(
                            "jei.exnihilosatellitium.crucible_heat.speed", recipe.speed()));
                    if (!recipe.properties().isEmpty()) {
                        tooltip.add(Component.translatable(
                                "jei.exnihilosatellitium.crucible_heat.state",
                                String.join(", ", recipe.properties().keySet())));
                    }
                });
    }

    @Override
    public void draw(final CrucibleHeat.HeatSource recipe, final IRecipeSlotsView slotsView,
                     final GuiGraphics graphics, final double mouseX, final double mouseY) {
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.exnihilosatellitium.crucible_heat.value", recipe.speed()),
                TEXT_X, TEXT_Y, 0xFF404040, false);
    }
}
