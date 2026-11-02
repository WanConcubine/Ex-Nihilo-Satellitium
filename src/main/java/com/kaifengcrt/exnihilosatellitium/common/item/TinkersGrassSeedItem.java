package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Tinkers-only grass seed: right-click a vanilla grass block to turn it into a
 * Tinkers slime grass block (the "vanilla soil" variant).
 *
 * <p>The target block is looked up by id at use time, so this class never links
 * against Tinkers classes; the item itself is only registered when
 * {@code tconstruct} is loaded.</p>
 */
public class TinkersGrassSeedItem extends Item {

    private final String targetBlockId;

    public TinkersGrassSeedItem(final Properties properties, final String targetBlockId) {
        super(properties);
        this.targetBlockId = targetBlockId;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
            return InteractionResult.PASS;
        }
        final ResourceLocation id = ResourceLocation.tryParse(this.targetBlockId);
        final Block target = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        if (target == null) {
            return InteractionResult.PASS;
        }
        final Player player = context.getPlayer();
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, target.defaultBlockState());
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        8, 0.35D, 0.2D, 0.35D, 0.0D);
            }
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            if (player == null || !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
