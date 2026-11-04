package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import com.kaifengcrt.exnihilosatellitium.common.item.CrookItem;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.HammerRolls;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Hammer/crook effects that hook into the normal block-break path.
 *
 * <p>The hammer has no vanilla {@code TOOL} data, so vanilla would treat it as
 * the wrong tool for stone and skip the drop path. {@link PlayerEvent.HarvestCheck}
 * and {@link PlayerEvent.BreakSpeed} make hammer-table blocks harvestable at a
 * usable speed.</p>
 *
 * <p>1.20.1 Forge has no drop-replacement event: the hammer handles
 * {@link BlockEvent.BreakEvent} itself (cancel, remove the block, spawn the
 * crush drops and wear the tool). The crook keeps the vanilla break (and thus
 * the vanilla leaf drops) and only adds its extra rolls.</p>
 */
@Mod.EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    @SubscribeEvent
    public static void onBreak(final BlockEvent.BreakEvent event) {
        final Player player = event.getPlayer();
        if (player.level().isClientSide || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        final ItemStack tool = player.getMainHandItem();
        final BlockPos pos = event.getPos();
        final BlockState state = event.getState();

        if (tool.getItem() instanceof HammerItem) {
            if (HammerRecipes.ruleFor(state) == null) {
                return;
            }
            // No drop-replacement event on 1.20.1: take the break over completely.
            event.setCanceled(true);
            level.removeBlock(pos, false);
            level.levelEvent(2001, pos, Block.getId(state));
            for (final ItemStack result : HammerRolls.roll(state, 0, level.random)) {
                Block.popResource(level, pos, result);
            }
            if (!player.isCreative()) {
                tool.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
            return;
        }

        if (tool.getItem() instanceof CrookItem crook) {
            final var drops = CrookDrops.dropsFor(state.getBlock());
            if (drops.isEmpty()) {
                return;
            }
            // Vanilla keeps its own leaf drops; the crook only adds these rolls.
            for (final CrookDrops.Drop drop : drops) {
                final float chance = Math.min(1.0F, drop.chance() * crook.chanceMultiplier());
                if (level.random.nextFloat() < chance) {
                    Block.popResource(level, pos, new ItemStack(drop.item()));
                }
            }
            // Like the hammer: wear the stack actually in hand. BreakEvent fires
            // before the block is removed, so this runs exactly once per break.
            if (!player.isCreative()) {
                tool.hurtAndBreak(1, player,
                        broken -> broken.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
        }
    }
}
