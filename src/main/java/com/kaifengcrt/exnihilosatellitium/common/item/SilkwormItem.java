package com.kaifengcrt.exnihilosatellitium.common.item;

import com.kaifengcrt.exnihilosatellitium.common.block.InfestedLeavesBlock;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.InfestedLeavesBlockEntity;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;

/**
 * Silkworm (M6): right-click a leaf block to infest it. The infested block
 * remembers the original leaves and spreads once it is mature.
 */
public class SilkwormItem extends Item {

    public SilkwormItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.LEAVES) || state.getBlock() instanceof InfestedLeavesBlock) {
            return InteractionResult.PASS;
        }
        final Player player = context.getPlayer();
        if (!level.isClientSide) {
            level.setBlock(pos, ModBlocks.INFESTED_LEAVES.get().defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof InfestedLeavesBlockEntity leaves) {
                leaves.setMimic(state);
            }
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PLACE, SoundSource.BLOCKS, 0.8F, 1.2F);
            if (player == null || !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
