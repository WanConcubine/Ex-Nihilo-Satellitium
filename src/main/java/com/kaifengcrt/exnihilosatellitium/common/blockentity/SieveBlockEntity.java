package com.kaifengcrt.exnihilosatellitium.common.blockentity;

import com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Sieve state: the installed mesh, the material being sifted, and a continuous
 * progress bar (like Omnia's work / workMax) instead of one click = one result.
 */
public class SieveBlockEntity extends BlockEntity {

    /** Ticks of work needed for one item; also the progress bar maximum. */
    public static final int WORK_MAX = 100;
    /** Work added per tick while a material is loaded. */
    public static final int WORK_PER_TICK = 2;
    public static final int MAX_CONTENTS = 16;

    private ItemStack mesh = ItemStack.EMPTY;
    private ItemStack contents = ItemStack.EMPTY;
    private int work;

    public SieveBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.SIEVE.get(), pos, state);
    }

    public ItemStack getMesh() {
        return this.mesh;
    }

    public boolean hasMesh() {
        return !this.mesh.isEmpty();
    }

    public void setMesh(final ItemStack stack) {
        this.mesh = stack;
        this.setChanged();
        this.sync();
        this.updateBlockMeshState();
    }

    public ItemStack getContents() {
        return this.contents;
    }

    public int getWork() {
        return this.work;
    }

    public float progress() {
        return Math.min(1.0F, this.work / (float) WORK_MAX);
    }

    /** Loads a held stack into the sieve; returns false when it does not fit. */
    public boolean insert(final ItemStack stack) {
        if (SieveRegistry.dropsFor(stack).isEmpty()) {
            return false;
        }
        if (!this.contents.isEmpty() && !ItemStack.isSameItem(this.contents, stack)) {
            return false;
        }
        final int room = MAX_CONTENTS - (this.contents.isEmpty() ? 0 : this.contents.getCount());
        if (room <= 0) {
            return false;
        }
        final int moved = Math.min(room, stack.getCount());
        if (this.contents.isEmpty()) {
            this.contents = stack.copyWithCount(moved);
        } else {
            this.contents.grow(moved);
        }
        stack.shrink(moved);
        this.setChanged();
        this.sync();
        return true;
    }

    /** Empties the contents back to the player (sneak + empty hand). */
    public void ejectContents(final Player player) {
        if (!this.contents.isEmpty()) {
            player.getInventory().placeItemBackInInventory(this.contents.copy());
            this.contents = ItemStack.EMPTY;
            this.work = 0;
            this.setChanged();
            this.sync();
        }
    }

    public void dropMesh(final Player player) {
        if (this.hasMesh()) {
            player.getInventory().placeItemBackInInventory(this.mesh.copy());
            this.mesh = ItemStack.EMPTY;
            this.setChanged();
            this.sync();
            this.updateBlockMeshState();
        }
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final SieveBlockEntity sieve) {
        if (!sieve.hasMesh() || sieve.contents.isEmpty()) {
            if (sieve.work != 0) {
                sieve.work = 0;
                sieve.sync();
            }
            return;
        }

        sieve.work += WORK_PER_TICK;
        if (sieve.work < WORK_MAX) {
            if (sieve.work % 10 == 0) {
                sieve.sync();
            }
            return;
        }

        sieve.completeSift(level, pos);
    }

    private void completeSift(final Level level, final BlockPos pos) {
        final ItemStack input = this.contents;
        final List<SieveDrop> drops = SieveRegistry.dropsFor(input);
        this.work = 0;

        for (final SieveDrop drop : drops) {
            if (level.random.nextFloat() < drop.chance()) {
                final ItemEntity entity = new ItemEntity(level,
                        pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        new ItemStack(drop.item()));
                entity.setDeltaMovement(0.0D, 0.1D, 0.0D);
                level.addFreshEntity(entity);
            }
        }
        level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS,
                0.4F, 0.9F + level.random.nextFloat() * 0.2F);

        this.contents.shrink(1);
        if (this.contents.isEmpty()) {
            this.contents = ItemStack.EMPTY;
        }
        this.damageMesh(level, pos);
        this.setChanged();
        this.sync();
    }

    /** One completed sift costs one durability; a broken mesh disappears. */
    private void damageMesh(final Level level, final BlockPos pos) {
        if (!this.hasMesh()) {
            return;
        }
        final ItemStack current = this.mesh;
        final int damage = current.getDamageValue() + 1;
        if (damage >= current.getMaxDamage()) {
            this.mesh = ItemStack.EMPTY;
            level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            this.updateBlockMeshState();
        } else {
            current.setDamageValue(damage);
        }
    }

    public void updateBlockMeshState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        final int tier = this.mesh.getItem() instanceof MeshItem meshItem ? meshItem.tier() : 0;
        final BlockState state = this.getBlockState();
        if (state.hasProperty(SieveBlock.MESH) && state.getValue(SieveBlock.MESH) != tier) {
            this.level.setBlock(this.worldPosition, state.setValue(SieveBlock.MESH, tier), Block.UPDATE_ALL);
        }
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.hasMesh()) {
            tag.put("Mesh", this.mesh.save(registries));
        }
        if (!this.contents.isEmpty()) {
            tag.put("Contents", this.contents.save(registries));
        }
        tag.putInt("Work", this.work);
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mesh = tag.contains("Mesh") ? ItemStack.parseOptional(registries, tag.getCompound("Mesh")) : ItemStack.EMPTY;
        this.contents = tag.contains("Contents") ? ItemStack.parseOptional(registries, tag.getCompound("Contents")) : ItemStack.EMPTY;
        this.work = tag.getInt("Work");
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Without this the client never receives contents/work and nothing renders.
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
