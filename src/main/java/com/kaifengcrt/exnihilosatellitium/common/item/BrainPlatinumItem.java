package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * "Brain Platinum" (脑白金): right-click a nitwit villager to turn it into an
 * ordinary unemployed villager.
 *
 * <p>Only the nitwit profession changes; the entity keeps its type and bed/home
 * links. The item is consumed on success and left untouched on any other
 * target.</p>
 */
public final class BrainPlatinumItem extends Item {

    public BrainPlatinumItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context,
                                final List<Component> tooltip, final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.exnihilosatellitium.brain_platinum.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player player,
                                                  final LivingEntity target, final InteractionHand hand) {
        return tryConvert(player, target, stack)
                ? InteractionResult.sidedSuccess(player.level().isClientSide)
                : InteractionResult.PASS;
    }

    /**
     * Converts a nitwit villager into an ordinary unemployed villager.
     *
     * <p>Called from both {@link #interactLivingEntity} and the
     * {@code PlayerInteractEvent.EntityInteract} handler: vanilla opens the
     * trade screen on a normal right-click, so the item method only runs while
     * sneaking. The event runs before the villager interaction and can cancel
     * it, which keeps the item usable without Shift.</p>
     *
     * @return true when the target is a nitwit (whether or not this is the
     *         logical server); the caller decides whether to cancel the
     *         vanilla interaction.
     */
    public static boolean tryConvert(final Player player, final LivingEntity target,
                                     final ItemStack stack) {
        if (!(target instanceof Villager villager)
                || villager.getVillagerData().getProfession() != VillagerProfession.NITWIT) {
            return false;
        }

        final Level level = player.level();
        if (!level.isClientSide) {
            villager.setVillagerData(villager.getVillagerData()
                    .setProfession(VillagerProfession.NONE)
                    .setLevel(1));
            villager.setVillagerXp(0);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_YES,
                    SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        villager.getX(), villager.getY() + 1.0D, villager.getZ(),
                        8, 0.4D, 0.5D, 0.4D, 0.0D);
            }
        }
        return true;
    }
}
