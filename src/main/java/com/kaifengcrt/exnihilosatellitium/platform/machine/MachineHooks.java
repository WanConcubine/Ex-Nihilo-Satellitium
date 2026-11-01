package com.kaifengcrt.exnihilosatellitium.platform.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * The machine entity's only door to NeoForge item capabilities.
 *
 * <p>{@code MachineBlockEntity} pushes output through this facade instead of
 * touching {@code Capabilities} / {@code ItemHandlerHelper} itself, so the core
 * state machine stays free of platform imports. 1.20.1 only rewrites this file
 * (Forge capabilities + {@code LazyOptional}).</p>
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
        final IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK,
                pos.relative(face), face.getOpposite());
        if (target == null) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(target, stack, false);
    }
}
