package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Ancient Spores (古代孢子, M6): the in-world route to mycelium, and the mycelium
 * route to witch water. Right-click dirt to turn it into mycelium; right-click a
 * cow to turn it into a mooshroom (which drops mushrooms and can be milked).
 *
 * <p>Ancient Spores come from sieving sand, like the original 1/128 entry.</p>
 */
public class AncientSporesItem extends Item {

    public AncientSporesItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.DIRT)) {
            return InteractionResult.PASS;
        }
        final Player player = context.getPlayer();
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, Blocks.MYCELIUM.defaultBlockState());
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.MYCELIUM,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        12, 0.4D, 0.2D, 0.4D, 0.0D);
            }
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 0.7F, 0.6F);
            if (player == null || !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player player,
                                                  final LivingEntity target, final InteractionHand hand) {
        if (!(target instanceof Cow cow) || target instanceof MushroomCow || !target.isAlive()) {
            return InteractionResult.PASS;
        }
        final Level level = target.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        final MushroomCow mooshroom = net.minecraft.world.entity.EntityType.MOOSHROOM.create(level);
        if (mooshroom == null) {
            return InteractionResult.PASS;
        }
        mooshroom.moveTo(cow.getX(), cow.getY(), cow.getZ(), cow.getYRot(), cow.getXRot());
        mooshroom.setHealth(cow.getHealth());
        mooshroom.setCustomName(cow.getCustomName());
        mooshroom.setBaby(cow.isBaby());
        cow.discard();
        level.addFreshEntity(mooshroom);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    cow.getX(), cow.getY() + cow.getBbHeight() * 0.5D, cow.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, cow.blockPosition(), SoundEvents.MOOSHROOM_CONVERT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
