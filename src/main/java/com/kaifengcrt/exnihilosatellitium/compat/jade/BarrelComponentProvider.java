package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
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

        final ItemStack solid = barrel.getSolid();
        if (fluid > 0 && !solid.isEmpty()) {
            final BarrelRecipes.Recipe rule = BarrelRecipes.ruleForItem(barrel.getTank().getFluid().getFluid(), solid);
            if (rule != null) {
                tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.transform",
                        solid.getHoverName(), resultName(rule)));
            }
        }

        if (barrel.isCooking()) {
            tooltip.add(Component.translatable(barrel.isConvertingFluid()
                    ? "jade.exnihilosatellitium.barrel.converting"
                    : "jade.exnihilosatellitium.barrel.cooking", barrel.getCookSeconds()));
            return;
        }

        if (barrel.isDirtReady()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.ready"));
            return;
        }

        if (barrel.hasSolid()) {
            tooltip.add(Component.translatable("jade.exnihilosatellitium.barrel.solid",
                    barrel.getSolid().getHoverName()));
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

    /** Result of the current fluid + solid reaction, for the tooltip line. */
    private static Component resultName(final BarrelRecipes.Recipe rule) {
        if (rule.resultFluid() != null) {
            return new FluidStack(rule.resultFluid(), 1).getHoverName();
        }
        if (rule.entity() != null) {
            return BuiltInRegistries.ENTITY_TYPE.get(rule.entity()).getDescription();
        }
        return rule.result().getHoverName();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
