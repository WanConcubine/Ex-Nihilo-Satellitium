package com.kaifengcrt.exnihiloimitatio.registry;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
import com.kaifengcrt.exnihiloimitatio.common.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Block registry.
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ExNihiloImitatio.MOD_ID);

    public static final RegistryObject<Block> BARREL =
            BLOCKS.register("barrel", () -> new BarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    private ModBlocks() {
    }

    public static void register(final IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
