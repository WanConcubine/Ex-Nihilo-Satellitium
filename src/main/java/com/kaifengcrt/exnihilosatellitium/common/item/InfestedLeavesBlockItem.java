package com.kaifengcrt.exnihilosatellitium.common.item;

import com.kaifengcrt.exnihilosatellitium.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Item form of the infested leaves. Carrying the {@code infestation} component
 * decides whether the placed block starts at 0 % or fully mature, so the
 * creative tab can offer both "infesting" and "infested" entries of one item.
 */
public class InfestedLeavesBlockItem extends BlockItem {

    public InfestedLeavesBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    private static boolean mature(final ItemStack stack) {
        final Float progress = stack.get(ModDataComponents.INFESTATION.get());
        return progress != null && progress >= 1.0F;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return mature(stack)
                ? Component.translatable("block.exnihilosatellitium.infested_leaves.mature")
                : super.getName(stack);
    }
}
