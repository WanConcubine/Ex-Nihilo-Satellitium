package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Crimson / warped nylium spores (绯红 / 诡异菌岩孢子): right-click netherrack to
 * grow the matching nylium. This is the 1.16+ skyblock entry point for nether
 * trees, the same idea as Sequentia's nylium spores: once nylium exists, vanilla
 * bone meal grows fungus, and bone meal on the fungus grows a huge fungus.
 *
 * <p>The spores come from sieving crushed netherrack, so the whole chain stays
 * inside the sieve: netherrack -&gt; hammer -&gt; sift -&gt; spores -&gt; nylium.</p>
 */
public class NyliumSporesItem extends Item {

    private final Block trigger;
    private final Block result;
    private final ParticleOptions particles;

    public NyliumSporesItem(final Properties properties, final Block trigger,
                            final Block result, final ParticleOptions particles) {
        super(properties);
        this.trigger = trigger;
        this.result = result;
        this.particles = particles;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(trigger)) {
            return InteractionResult.PASS;
        }
        final Player player = context.getPlayer();
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, result.defaultBlockState());
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(particles,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        12, 0.4D, 0.2D, 0.4D, 0.0D);
            }
            level.playSound(null, pos, SoundEvents.NETHERRACK_PLACE, SoundSource.BLOCKS, 0.8F, 0.9F);
            if (player == null || !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
