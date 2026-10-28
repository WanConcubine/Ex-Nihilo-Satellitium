package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Tooltip lines for the barrel. Reads the already-synced block entity; no
 * extra network traffic.
 */
public enum BarrelComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "barrel");

    @Override
    public void appendTooltip(final ITooltip tooltip, final BlockAccessor accessor,
                              final IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof BarrelBlockEntity barrel)) {
            return;
        }

        if (barrel.isDirtReady()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.ready"));
            return;
        }

        if (barrel.getCompostProgress() > 0) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.compost",
                    barrel.getCompostProgress(), BarrelBlockEntity.COMPOST_MAX));
            if (barrel.getCompostTicks() > 0) {
                final int seconds = (barrel.getCompostTicks() + 19) / 20;
                tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.timer", seconds));
            }
        }

        final FluidStack fluid = barrel.getTank().getFluid();
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.fluid",
                    fluid.getDisplayName(), fluid.getAmount(), barrel.getTank().getCapacity()));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
