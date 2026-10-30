package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.CrucibleBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Crucible tooltip: fluid, the block being melted, and the heat-source speed. */
public enum CrucibleComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "crucible");

    @Override
    public void appendTooltip(final ITooltip tooltip, final BlockAccessor accessor, final IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof CrucibleBlockEntity crucible)) {
            return;
        }

        // Jade's built-in fluid handler already shows the tank; only add what it
        // cannot know about (the melt queue and the heat-source speed).
        final int pending = crucible.getPending();
        if (pending > 0) {
            final ItemStack stack = crucible.getPendingStack();
            tooltip.add(Component.translatable("jade.exnihilosatellitium.crucible.melting",
                    stack.isEmpty()
                            ? Component.translatable("jade.exnihilosatellitium.crucible.unknown")
                            : stack.getHoverName(),
                    pending));
        }

        if (crucible.getTank().getFluid().isEmpty() && pending <= 0) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.crucible.empty"));
        }

        final float speed = CrucibleHeat.speedForClient(
                accessor.getLevel().getBlockState(accessor.getPosition().below()));
        if (speed > 0.0F) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.crucible.heat", speed));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
