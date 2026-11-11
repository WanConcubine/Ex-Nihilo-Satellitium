package com.kaifengcrt.exnihilosatellitium.compat.tconstruct;

import com.kaifengcrt.exnihilosatellitium.common.item.HammerTool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Tinkers' Construct modifiable hammer that also feeds the Ex Nihilo crush
 * table.
 *
 * <p>Extending {@link ModifiableItem} keeps every TiC behaviour: material
 * stats, material traits, modifier slots, durability, Tinker Station building
 * and the modifier GUI. Implementing {@link HammerTool} is the only bridge to
 * our side: the shared {@code ToolEvents} hooks then replace the drops of any
 * hammer-table block with the same rolls the plain {@link
 * com.kaifengcrt.exnihilosatellitium.common.item.HammerItem} uses.</p>
 *
 * <p>On 1.20.1 the hammer takes the break over by cancelling
 * {@code BlockEvent.BreakEvent}, so TiC's own {@code ToolHarvestLogic} never
 * runs and cannot charge durability. {@link #wear} pays that point through
 * TiC's damage hooks instead (reinforced / overslime / multipliers / break
 * animation all still apply).</p>
 */
public final class ExNihiloHammerItem extends ModifiableItem implements HammerTool {

    public ExNihiloHammerItem(final Item.Properties properties, final ToolDefinition definition) {
        super(properties, definition);
    }

    /**
     * Plain hammer tier reported to the shared hooks. The TiC head material's
     * mining tier is what actually gates harvesting in game; this value only
     * keeps the {@link HammerTool} contract (JEI / catalyst metadata).
     */
    @Override
    public int tier() {
        return 5;
    }

    /**
     * Use TiC's material mining speed instead of the plain 6.0 hammer floor.
     *
     * <p>Returning a real value matters for hammer-table blocks that are tagged
     * {@code mineable/shovel} (gravel, sand, dust): the tool is not "effective"
     * on those, so vanilla would fall back to bare-hand speed. The event hook
     * raises the break speed to the head material's stat instead.</p>
     */
    @Override
    public float breakSpeed(final ItemStack stack) {
        return Math.max(1.0F, ToolStack.from(stack).getStats().get(ToolStats.MINING_SPEED));
    }

    /** One {@code exnihilosatellitium:chain} level takes 16 more connected blocks. */
    @Override
    public int chainLimit(final ItemStack stack) {
        return ToolStack.from(stack).getModifiers().getLevel(TinkersTools.CHAIN.getId())
                * TinkersTools.CHAIN_BLOCKS_PER_LEVEL;
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level level,
                                final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(
                        "item.exnihilosatellitium.ex_nihilo_hammer.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    /** One crush = one TiC durability point, paid through TiC's damage hooks. */
    @Override
    public void wear(final Player player, final ItemStack stack) {
        ToolDamageUtil.damageAnimated(ToolStack.from(stack), 1, player, EquipmentSlot.MAINHAND);
    }
}
