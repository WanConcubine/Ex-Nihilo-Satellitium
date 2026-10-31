package com.kaifengcrt.exnihilosatellitium.common.blockentity;

import com.kaifengcrt.exnihilosatellitium.common.block.InfestedLeavesBlock;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Infested leaves (M6): remembers the leaf block it replaced, crawls towards
 * white over {@link #PROGRESS_PER_TICK}, spreads to neighbouring leaves once it
 * is past {@link #SPREAD_THRESHOLD}, and drops string when broken.
 */
public class InfestedLeavesBlockEntity extends BlockEntity {

    /** 0.0005 per tick: 2000 ticks (100 s) to fully infest, like the original. */
    public static final float PROGRESS_PER_TICK = 0.0005F;
    /** Spreading only starts after 60 % progress, then every 100 ticks. */
    public static final float SPREAD_THRESHOLD = 0.6F;
    private static final int SPREAD_INTERVAL = 100;

    private BlockState mimic = Blocks.OAK_LEAVES.defaultBlockState();
    private float progress;
    private int spreadTimer;

    public InfestedLeavesBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.INFESTED_LEAVES.get(), pos, state);
    }

    public BlockState getMimic() {
        return this.mimic;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(final float progress) {
        this.progress = Math.max(0.0F, Math.min(1.0F, progress));
        this.setChanged();
        this.sync();
    }

    /** Called by the silkworm and by the spread logic. */
    public void setMimic(final BlockState state) {
        this.mimic = state;
        this.setChanged();
        this.sync();
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final InfestedLeavesBlockEntity leaves) {
        if (leaves.progress < 1.0F) {
            leaves.progress = Math.min(1.0F, leaves.progress + PROGRESS_PER_TICK);
        }
        if (leaves.progress > SPREAD_THRESHOLD && ++leaves.spreadTimer >= SPREAD_INTERVAL) {
            leaves.spreadTimer = level.random.nextInt(10);
            leaves.spread(level, pos);
        }
        // Occasional white motes so an infested patch is readable in world.
        if (leaves.progress > 0.05F && level.random.nextInt(24) == 0
                && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    pos.getX() + level.random.nextDouble(), pos.getY() + level.random.nextDouble(),
                    pos.getZ() + level.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (level.getGameTime() % 40 == 0) {
            leaves.setChanged();
            leaves.sync();
        }
    }

    private void spread(final Level level, final BlockPos pos) {
        // Collect the plain leaves around this block and infest one of them: a
        // random 3x3x3 pick mostly lands on air and makes the patch crawl.
        final java.util.List<BlockPos> targets = new java.util.ArrayList<>(8);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    final BlockPos candidate = pos.offset(dx, dy, dz);
                    final BlockState candidateState = level.getBlockState(candidate);
                    if (candidateState.is(BlockTags.LEAVES)
                            && !(candidateState.getBlock() instanceof InfestedLeavesBlock)) {
                        targets.add(candidate);
                    }
                }
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        final BlockPos target = targets.get(level.random.nextInt(targets.size()));
        final BlockState targetState = level.getBlockState(target);
        level.setBlock(target, ModBlocks.INFESTED_LEAVES.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(target) instanceof InfestedLeavesBlockEntity infested) {
            infested.setMimic(targetState);
        }
        level.playSound(null, target, SoundEvents.CAVE_VINES_PLACE, SoundSource.BLOCKS, 0.4F, 1.4F);
    }

    /** String drop chance grows with progress; called from the block's destroy hook. */
    public void dropString(final Level level, final BlockPos pos) {
        if (level.random.nextFloat() < this.progress) {
            Block.popResource(level, pos, new ItemStack(Items.STRING));
        }
        if (level.random.nextFloat() < this.progress * 0.25F) {
            Block.popResource(level, pos, new ItemStack(Items.STRING));
        }
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Mimic", NbtUtils.writeBlockState(this.mimic));
        tag.putFloat("Progress", this.progress);
        tag.putInt("SpreadTimer", this.spreadTimer);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Mimic")) {
            this.mimic = NbtUtils.readBlockState(
                    registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                    tag.getCompound("Mimic"));
        }
        this.progress = tag.getFloat("Progress");
        this.spreadTimer = tag.getInt("SpreadTimer");
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
