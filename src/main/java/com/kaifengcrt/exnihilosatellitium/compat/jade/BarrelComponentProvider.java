package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Barrel tooltip: fluid amount, compost percentage, and the remaining time
 * once the compost bar is full.
 *
 * <p>Values come from the block entity's synced update tag, so the client sees
 * the same numbers the server has.</p>
 */
public enum BarrelComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "barrel");

    @Override
    public void appendTooltip(final ITooltip tooltip, final BlockAccessor accessor, final IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof BarrelBlockEntity barrel)) {
            return;
        }

        final int fluid = barrel.getTank().getFluidAmount();
        if (fluid > 0) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.fluid",
                    barrel.getTank().getFluid().getHoverName(), fluid, BarrelBlockEntity.CAPACITY));
        }

        if (barrel.isDirtReady()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.ready"));
            return;
        }

        final int progress = barrel.getCompostProgress();
        if (progress > 0) {
            final int percent = Math.min(100, progress * 100 / BarrelBlockEntity.COMPOST_MAX);
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.compost",
                    percent, progress, BarrelBlockEntity.COMPOST_MAX));
        }

        final int ticks = barrel.getCompostTicks();
        if (progress >= BarrelBlockEntity.COMPOST_MAX && ticks > 0) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.timer",
                    (ticks + 19) / 20));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
