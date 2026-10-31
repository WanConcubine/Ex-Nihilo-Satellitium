package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.InfestedLeavesBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Infested leaves tooltip: original leaf name and infestation progress. */
public enum InfestedLeavesComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "infested_leaves");

    @Override
    public void appendTooltip(final ITooltip tooltip, final BlockAccessor accessor, final IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof InfestedLeavesBlockEntity leaves)) {
            return;
        }
        tooltip.add(Component.translatable("jade.exnihilosatellitium.infested_leaves.mimic",
                leaves.getMimic().getBlock().getName()));
        final int percent = Math.round(leaves.getProgress() * 100.0F);
        if (leaves.getProgress() >= 1.0F) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.infested_leaves.mature"));
        } else {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.infested_leaves.progress", percent));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
