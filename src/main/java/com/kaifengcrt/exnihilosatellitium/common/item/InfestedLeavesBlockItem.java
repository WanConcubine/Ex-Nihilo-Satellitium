package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Item form of the infested leaves.
 *
 * <p>1.20.1 has no data components, so the infestation progress rides in the
 * vanilla {@code BlockEntityTag} that {@link BlockItem} already copies into the
 * block entity on placement. The creative tab offers two stacks of the same
 * item: a fresh one (0 %) and a mature one ({@code Progress = 1.0}).</p>
 */
public class InfestedLeavesBlockItem extends BlockItem {

    public InfestedLeavesBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    /** A stack whose block entity starts at the given infestation progress. */
    public static ItemStack withProgress(final ItemStack stack, final float progress) {
        final CompoundTag entityTag = stack.getOrCreateTagElement(BLOCK_ENTITY_TAG);
        entityTag.putFloat("Progress", progress);
        return stack;
    }

    private static boolean mature(final ItemStack stack) {
        final CompoundTag entityTag = stack.getTagElement(BLOCK_ENTITY_TAG);
        return entityTag != null && entityTag.getFloat("Progress") >= 1.0F;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return mature(stack)
                ? Component.translatable("block.exnihilosatellitium.infested_leaves.mature")
                : super.getName(stack);
    }
}
