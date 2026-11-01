package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JEI category for the sieve.
 *
 * <p>Layout follows the classic Ex Nihilo pages: the material goes top-left,
 * the mesh bottom-left, and the drops fill a 7-column grid on the right. Every
 * slot uses JEI's native slot background, so the item is always centred inside
 * the frame; no custom background grid is drawn.</p>
 */
public final class SieveRecipeCategory extends AbstractRecipeCategory<SieveRecipe> {

    // Two sieve recipes (gravel + sand) stack on one page at this height.
    private static final int WIDTH = 166;
    // JEI stacks two tiny sieve recipes on one 166-tall page; each recipe gets
    // half of it, so the content fills the page instead of leaving grey space.
    private static final int HEIGHT = 60;
    private static final int SLOT = 18;
    private static final int COLUMNS = 7;
    private static final int ROWS = 3;
    private static final int INPUT_X = 6;
    private static final int INPUT_Y = 3;
    private static final int MESH_Y = 39;
    private static final int OUTPUT_X = 34;
    private static final int OUTPUT_Y = 3;
    private static final Set<Object> OVERFLOW_WARNED = ConcurrentHashMap.newKeySet();

    public SieveRecipeCategory(final IGuiHelper guiHelper) {
        super(SieveJeiPlugin.RECIPE_TYPE,
                Component.translatable("jei.exnihilosatellitium.sieve"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.SIEVE.get())),
                WIDTH,
                HEIGHT);
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final SieveRecipe recipe,
                          final IFocusGroup focuses) {
        if (recipe.drops().size() > COLUMNS * ROWS
                && OVERFLOW_WARNED.add(recipe.input().getItem())) {
            ExNihiloSatellitium.LOGGER.warn("[jei] sieve input {} has {} drops; only {} fit on one page",
                    recipe.input().getHoverName().getString(), recipe.drops().size(), COLUMNS * ROWS);
        }
        builder.addInputSlot(INPUT_X, INPUT_Y)
                .setStandardSlotBackground()
                .addItemStack(recipe.input())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.sieve.hint")));

        builder.addInputSlot(INPUT_X, MESH_Y)
                .setStandardSlotBackground()
                .addItemStacks(meshStacks())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.exnihilosatellitium.sieve.mesh_hint")));

        int index = 0;
        for (final SieveDrop drop : recipe.drops()) {
            if (index >= COLUMNS * ROWS) {
                break;
            }
            final int x = OUTPUT_X + (index % COLUMNS) * SLOT;
            final int y = OUTPUT_Y + (index / COLUMNS) * SLOT;
            final IRecipeSlotBuilder slot = builder.addOutputSlot(x, y)
                    // Same vanilla crafting-table slot as the inputs; do not mix
                    // JEI's special output-slot background into the grid.
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(drop.item()));
            slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(
                    Component.translatable("jei.exnihilosatellitium.sieve.chance", percent(drop.chance()))));
            index++;
        }
    }

    private static List<ItemStack> meshStacks() {
        return ModItems.MESHES.stream().map(holder -> new ItemStack(holder.get())).toList();
    }

    /** 0.05556 -> "5.56%", 0.2 -> "20%"; two decimals only when needed. */
    static String percent(final float chance) {
        final double value = chance * 100.0D;
        if (Math.abs(value - Math.rint(value)) < 0.005D) {
            return String.format(Locale.ROOT, "%.0f%%", value);
        }
        return String.format(Locale.ROOT, "%.2f%%", value);
    }
}
