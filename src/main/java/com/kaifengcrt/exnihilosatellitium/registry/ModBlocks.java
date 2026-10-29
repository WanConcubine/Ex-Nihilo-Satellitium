package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

/**
 * Block registry.
 *
 * <p>All barrels share one block class and one block entity type. Wood variants
 * only differ in block properties and model textures; the stone barrel copies
 * stone properties so it needs a pickaxe and sounds like stone.</p>
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ExNihiloSatellitium.MOD_ID);

    /** Oak keeps the plain {@code barrel} id so existing saves and recipes resolve. */
    public static final DeferredHolder<Block, Block> BARREL =
            registerWoodBarrel("barrel", Blocks.OAK_PLANKS);
    public static final DeferredHolder<Block, Block> SPRUCE_BARREL =
            registerWoodBarrel("spruce_barrel", Blocks.SPRUCE_PLANKS);
    public static final DeferredHolder<Block, Block> BIRCH_BARREL =
            registerWoodBarrel("birch_barrel", Blocks.BIRCH_PLANKS);
    public static final DeferredHolder<Block, Block> JUNGLE_BARREL =
            registerWoodBarrel("jungle_barrel", Blocks.JUNGLE_PLANKS);
    public static final DeferredHolder<Block, Block> ACACIA_BARREL =
            registerWoodBarrel("acacia_barrel", Blocks.ACACIA_PLANKS);
    public static final DeferredHolder<Block, Block> DARK_OAK_BARREL =
            registerWoodBarrel("dark_oak_barrel", Blocks.DARK_OAK_PLANKS);
    public static final DeferredHolder<Block, Block> STONE_BARREL =
            BLOCKS.register("stone_barrel",
                    () -> new BarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    /** Registration order doubles as creative-tab order. */
    public static final List<DeferredHolder<Block, Block>> BARRELS = List.of(
            BARREL,
            SPRUCE_BARREL,
            BIRCH_BARREL,
            JUNGLE_BARREL,
            ACACIA_BARREL,
            DARK_OAK_BARREL,
            STONE_BARREL);

    private ModBlocks() {
    }

    private static DeferredHolder<Block, Block> registerWoodBarrel(final String name, final Block planks) {
        return BLOCKS.register(name, () -> new BarrelBlock(BlockBehaviour.Properties.ofFullCopy(planks)));
    }

    public static void register(final IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
