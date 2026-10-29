package com.kaifengcrt.exnihilosatellitium.common.blockentity;

import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
 * Sieve state: the installed mesh (with its own durability) and the sifting
 * action. The sift table itself is data-driven; see {@link SieveRegistry}.
 */
public class SieveBlockEntity extends BlockEntity {

    private ItemStack mesh = ItemStack.EMPTY;

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
    }

    /** @return true when the sift produced at least one drop. */
    public boolean sift(final Player player, final ItemStack input) {
        final List<SieveDrop> drops = SieveRegistry.dropsFor(input);
        if (drops.isEmpty()) {
            return false;
        }
        if (!player.isCreative()) {
            input.shrink(1);
        }

        final Level level = this.level;
        if (level == null) {
            return false;
        }

        boolean produced = false;
        for (final SieveDrop drop : drops) {
            if (level.random.nextFloat() < drop.chance()) {
                produced = true;
                final ItemStack out = new ItemStack(drop.item());
                final ItemEntity entity = new ItemEntity(level,
                        this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.75D,
                        this.worldPosition.getZ() + 0.5D, out);
                entity.setDeltaMovement(0.0D, 0.15D, 0.0D);
                level.addFreshEntity(entity);
            }
        }

        this.damageMesh(player);
        this.setChanged();
        this.sync();
        return produced;
    }

    /** One sift costs one durability; a broken mesh disappears. */
    private void damageMesh(final Player player) {
        if (player.isCreative() || !this.hasMesh()) {
            return;
        }
        final ItemStack current = this.mesh;
        final int damage = current.getDamageValue() + 1;
        if (damage >= current.getMaxDamage()) {
            this.mesh = ItemStack.EMPTY;
            if (this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_BREAK,
                        SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            this.updateBlockMeshState();
        } else {
            current.setDamageValue(damage);
        }
    }

    public void updateBlockMeshState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        final int tier = this.hasMesh() ? this.mesh.getItem() instanceof com.kaifengcrt.exnihilosatellitium.common.item.MeshItem meshItem
                ? meshItem.tier() : 1 : 0;
        final BlockState state = this.getBlockState();
        if (state.hasProperty(com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock.MESH)
                && state.getValue(com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock.MESH) != tier) {
            this.level.setBlock(this.worldPosition, state.setValue(
                    com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock.MESH, tier), Block.UPDATE_ALL);
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
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mesh = tag.contains("Mesh")
                ? ItemStack.parseOptional(registries, tag.getCompound("Mesh"))
                : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
