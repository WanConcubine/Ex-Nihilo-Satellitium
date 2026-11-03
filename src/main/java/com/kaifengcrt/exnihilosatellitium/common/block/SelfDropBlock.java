package com.kaifengcrt.exnihilosatellitium.common.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

/**
 * A block that always drops itself without a loot table.
 *
 * <p>Mod-gated gem ore blocks only exist while Mekanism/Thermal is installed, so
 * shipping a loot-table JSON for them would fail to parse in an unmodded pack
 * (the referenced item does not exist). The code drop keeps the block usable when
 * the mod is present and leaves nothing behind when it is not.</p>
 */
public class SelfDropBlock extends Block {

    public SelfDropBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
