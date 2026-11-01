package com.kaifengcrt.exnihilosatellitium.common.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Speed / fortune upgrade. The tier is baked into the item (I = 1, II = 2,
 * III = 3); the machine only looks at the best tier it finds, so four copies of
 * speed I are the same as one speed I.
 */
public class UpgradeItem extends Item {

    private final UpgradeType type;
    private final int tier;

    public UpgradeItem(final Properties properties, final UpgradeType type, final int tier) {
        super(properties);
        this.type = type;
        this.tier = tier;
    }

    public UpgradeType type() {
        return this.type;
    }

    /** 1..3. */
    public int tier() {
        return this.tier;
    }

    /** Speed multiplier applied to the base operation time. */
    public static double speedMultiplier(final int tier) {
        return switch (tier) {
            case 1 -> 1.5D;
            case 2 -> 2.5D;
            case 3 -> 4.0D;
            default -> 1.0D;
        };
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context,
                                final List<Component> tooltip, final TooltipFlag flag) {
        if (this.type == UpgradeType.SPEED) {
            tooltip.add(Component.translatable("tooltip.exnihilosatellitium.speed_upgrade",
                    String.format(java.util.Locale.ROOT, "%.1f", speedMultiplier(this.tier)))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.exnihilosatellitium.speed_upgrade.stack")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.exnihilosatellitium.fortune_upgrade", this.tier)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.exnihilosatellitium.fortune_upgrade.stack")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
