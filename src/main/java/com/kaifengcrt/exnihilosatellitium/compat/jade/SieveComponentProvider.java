package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.SieveBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Sieve tooltip: installed mesh (name + durability) and sifting progress. */
public enum SieveComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "sieve");

    @Override
    public void appendTooltip(final ITooltip tooltip, final BlockAccessor accessor, final IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof SieveBlockEntity sieve)) {
            return;
        }

        final ItemStack mesh = sieve.getMesh();
        if (mesh.isEmpty()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.sieve.no_mesh"));
            return;
        }

        final int left = mesh.getMaxDamage() - mesh.getDamageValue();
        tooltip.add(Component.translatable("jade.exnihilosatellitium.sieve.mesh",
                mesh.getHoverName(), left, mesh.getMaxDamage()));

        if (!sieve.getContents().isEmpty()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.sieve.progress",
                    Math.round(sieve.progress() * 100.0F)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
