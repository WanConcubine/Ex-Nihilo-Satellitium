package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import com.kaifengcrt.exnihilosatellitium.common.item.CrookItem;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Hammer/crook effects that hook into the normal block-break drop path.
 *
 * <p>The hammer is a plain {@link net.minecraft.world.item.Item} without a
 * {@code DataComponents.TOOL} component, so vanilla would treat it as the wrong
 * tool for stone and skip the drop path entirely. The two {@link PlayerEvent}
 * hooks below mark hammer-table blocks as harvestable and give the hammer a
 * reasonable mining speed there; the actual drop replacement still happens in
 * {@link #onBlockDrops(BlockDropsEvent)}.</p>
 */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID)
public final class ToolEvents {

    /** Classic hammer was 0.75 x an iron tool's efficiency on smashable blocks. */
    private static final float HAMMER_SPEED = 6.0F;

    private ToolEvents() {
    }

    @SubscribeEvent
    public static void onHarvestCheck(final PlayerEvent.HarvestCheck event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof HammerItem)) {
            return;
        }
        if (HammerRecipes.ruleFor(event.getTargetBlock()) != null) {
            event.setCanHarvest(true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(final PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity().getMainHandItem().getItem() instanceof HammerItem)) {
            return;
        }
        if (HammerRecipes.ruleFor(event.getState()) == null) {
            return;
        }
        event.setNewSpeed(Math.max(event.getNewSpeed(), HAMMER_SPEED));
    }

    /**
     * Creative mode never reaches {@link BlockDropsEvent}, so drop the hammer
     * result here instead. Classic Ex Nihilo also gave the result in creative,
     * which keeps the tool testable in a creative test world.
     */
    @SubscribeEvent
    public static void onCreativeBreak(final BlockEvent.BreakEvent event) {
        final Player player = event.getPlayer();
        if (!player.isCreative() || !(player.getMainHandItem().getItem() instanceof HammerItem)) {
            return;
        }
        final HammerRecipes.Rule rule = HammerRecipes.ruleFor(event.getState());
        if (rule == null || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Block.popResource(level, event.getPos(), rule.roll(level.random));
    }

    @SubscribeEvent
    public static void onBlockDrops(final BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }
        final ItemStack tool = event.getTool();
        if (tool.getItem() instanceof HammerItem) {
            final HammerRecipes.Rule rule = HammerRecipes.ruleFor(event.getState());
            if (rule == null) {
                return;
            }
            event.getDrops().clear();
            event.getDrops().add(drop(event.getLevel(), event.getPos(),
                    rule.roll(event.getLevel().random)));
            wearMainHand(player, tool);
            return;
        }
        if (tool.getItem() instanceof CrookItem crook) {
            final var drops = CrookDrops.dropsFor(event.getState().getBlock());
            if (drops.isEmpty()) {
                return;
            }
            for (final CrookDrops.Drop drop : drops) {
                final float chance = Math.min(1.0F, drop.chance() * crook.chanceMultiplier());
                if (event.getLevel().random.nextFloat() < chance) {
                    event.getDrops().add(drop(event.getLevel(), event.getPos(), new ItemStack(drop.item())));
                }
            }
            wearMainHand(player, tool);
        }
    }

    /**
     * Wear the tool the player is actually holding.
     *
     * <p>{@link BlockDropsEvent#getTool()} is a <em>copy</em> taken by
     * {@code ServerPlayerGameMode}; damaging it would be lost. The real stack is
     * the player's main hand. The item check guards against the hand changing
     * between the break and this event.</p>
     */
    private static void wearMainHand(final Player player, final ItemStack eventTool) {
        if (player.isCreative()) {
            return;
        }
        final ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() == eventTool.getItem()) {
            held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    private static ItemEntity drop(final ServerLevel level, final net.minecraft.core.BlockPos pos,
                                   final ItemStack stack) {
        return new ItemEntity(level,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
    }
}
