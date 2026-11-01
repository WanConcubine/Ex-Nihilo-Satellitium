package com.kaifengcrt.exnihilosatellitium.platform.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * The machine entity's only door to Forge item capabilities.
 *
 * <p>{@code MachineBlockEntity} pushes output through this facade instead of
 * touching capabilities / {@code ItemHandlerHelper} itself, so the core state
 * machine stays free of platform imports.</p>
 */
public final class MachineHooks {

    private MachineHooks() {
    }

    /**
     * Inserts into the inventory behind one face of the machine.
     *
     * @return the leftover that the neighbour refused; equal to {@code stack}
     *         when there is no inventory there
     */
    public static ItemStack insertIntoNeighbour(final Level level, final BlockPos pos,
                                                final Direction face, final ItemStack stack) {
        final net.minecraft.world.level.block.entity.BlockEntity neighbour =
                level.getBlockEntity(pos.relative(face));
        final IItemHandler target = neighbour == null ? null
                : neighbour.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite())
                        .orElse(null);
        if (target == null) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(target, stack, false);
    }
}
