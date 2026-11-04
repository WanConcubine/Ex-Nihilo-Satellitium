package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.Level;

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
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player player,
                                                  final LivingEntity target, final InteractionHand hand) {
        if (!(target instanceof Villager villager)
                || villager.getVillagerData().getProfession() != VillagerProfession.NITWIT) {
            return InteractionResult.PASS;
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
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
