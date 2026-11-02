package com.kaifengcrt.exnihilosatellitium.compat.jei;

import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        // A sift table keeps growing as mod integrations add drops, so never
        // truncate at 21: distribute them round-robin over the 7x3 grid. A slot
        // that holds several drops is cycled by JEI, and the chance tooltip
        // follows whatever is currently displayed.
        final List<SieveDrop> drops = recipe.drops();
        final int slots = COLUMNS * ROWS;
        for (int slotIndex = 0; slotIndex < Math.min(slots, drops.size()); slotIndex++) {
            final int x = OUTPUT_X + (slotIndex % COLUMNS) * SLOT;
            final int y = OUTPUT_Y + (slotIndex / COLUMNS) * SLOT;
            final IRecipeSlotBuilder slot = builder.addOutputSlot(x, y)
                    // Same vanilla crafting-table slot as the inputs; do not mix
                    // JEI's special output-slot background into the grid.
                    .setStandardSlotBackground();

            final Map<Item, Float> chances = new HashMap<>();
            final Map<Item, SieveDrop> byItem = new HashMap<>();
            for (int dropIndex = slotIndex; dropIndex < drops.size(); dropIndex += slots) {
                final SieveDrop drop = drops.get(dropIndex);
                slot.addItemStack(new ItemStack(drop.item()));
                chances.put(drop.item(), drop.chance());
                byItem.put(drop.item(), drop);
            }
            slot.addRichTooltipCallback((view, tooltip) ->
                    view.getDisplayedItemStack().ifPresent(stack -> {
                        final Float chance = chances.get(stack.getItem());
                        if (chance != null) {
                            tooltip.add(Component.translatable(
                                    "jei.exnihilosatellitium.sieve.chance", percent(chance)));
                        }
                        final SieveDrop drop = byItem.get(stack.getItem());
                        if (drop != null && drop.max() > 1) {
                            tooltip.add(Component.translatable(
                                    "jei.exnihilosatellitium.sieve.count",
                                    drop.min(), drop.max()));
                        }
                    }));
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
