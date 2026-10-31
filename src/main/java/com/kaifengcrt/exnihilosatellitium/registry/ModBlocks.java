package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.block.BarrelBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.CrucibleBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.UnfiredCrucibleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
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

    public static final DeferredHolder<Block, Block> SIEVE =
            BLOCKS.register("sieve", () -> new SieveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)));

    /** Generic dust block: hammered from sand, then sifted for powdered resources. */
    public static final DeferredHolder<Block, Block> DUST =
            BLOCKS.register("dust", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)));

    /** M4: unfired porcelain crucible (smelted into the working crucible). */
    public static final DeferredHolder<Block, Block> UNFIRED_CRUCIBLE =
            BLOCKS.register("unfired_crucible", () -> new UnfiredCrucibleBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY).noOcclusion()));

    /** M4: fired crucible with a fluid tank and a heat-driven melt queue. */
    public static final DeferredHolder<Block, Block> CRUCIBLE =
            BLOCKS.register("crucible", () -> new CrucibleBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).noOcclusion()));

    /** M6: witch water source block (fluid rendering comes from the FluidType). */
    public static final DeferredHolder<Block, LiquidBlock> WITCH_WATER =
            BLOCKS.register("witch_water", () -> new LiquidBlock(ModFluids.WITCH_WATER.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));

    /** M6: wooden crucibles, melt leaves/saplings into water without a heat source. */
    public static final DeferredHolder<Block, Block> WOODEN_CRUCIBLE =
            registerWoodenCrucible("wooden_crucible", Blocks.OAK_PLANKS);
    public static final DeferredHolder<Block, Block> SPRUCE_WOODEN_CRUCIBLE =
            registerWoodenCrucible("spruce_wooden_crucible", Blocks.SPRUCE_PLANKS);
    public static final DeferredHolder<Block, Block> BIRCH_WOODEN_CRUCIBLE =
            registerWoodenCrucible("birch_wooden_crucible", Blocks.BIRCH_PLANKS);
    public static final DeferredHolder<Block, Block> JUNGLE_WOODEN_CRUCIBLE =
            registerWoodenCrucible("jungle_wooden_crucible", Blocks.JUNGLE_PLANKS);
    public static final DeferredHolder<Block, Block> ACACIA_WOODEN_CRUCIBLE =
            registerWoodenCrucible("acacia_wooden_crucible", Blocks.ACACIA_PLANKS);
    public static final DeferredHolder<Block, Block> DARK_OAK_WOODEN_CRUCIBLE =
            registerWoodenCrucible("dark_oak_wooden_crucible", Blocks.DARK_OAK_PLANKS);
    public static final DeferredHolder<Block, Block> MANGROVE_WOODEN_CRUCIBLE =
            registerWoodenCrucible("mangrove_wooden_crucible", Blocks.MANGROVE_PLANKS);
    public static final DeferredHolder<Block, Block> CHERRY_WOODEN_CRUCIBLE =
            registerWoodenCrucible("cherry_wooden_crucible", Blocks.CHERRY_PLANKS);
    public static final DeferredHolder<Block, Block> BAMBOO_WOODEN_CRUCIBLE =
            registerWoodenCrucible("bamboo_wooden_crucible", Blocks.BAMBOO_PLANKS);
    public static final DeferredHolder<Block, Block> CRIMSON_WOODEN_CRUCIBLE =
            registerWoodenCrucible("crimson_wooden_crucible", Blocks.CRIMSON_PLANKS);
    public static final DeferredHolder<Block, Block> WARPED_WOODEN_CRUCIBLE =
            registerWoodenCrucible("warped_wooden_crucible", Blocks.WARPED_PLANKS);

    public static final List<DeferredHolder<Block, Block>> WOODEN_CRUCIBLES = List.of(
            WOODEN_CRUCIBLE,
            SPRUCE_WOODEN_CRUCIBLE,
            BIRCH_WOODEN_CRUCIBLE,
            JUNGLE_WOODEN_CRUCIBLE,
            ACACIA_WOODEN_CRUCIBLE,
            DARK_OAK_WOODEN_CRUCIBLE,
            MANGROVE_WOODEN_CRUCIBLE,
            CHERRY_WOODEN_CRUCIBLE,
            BAMBOO_WOODEN_CRUCIBLE,
            CRIMSON_WOODEN_CRUCIBLE,
            WARPED_WOODEN_CRUCIBLE);

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

    private static DeferredHolder<Block, Block> registerWoodenCrucible(final String name, final Block planks) {
        return BLOCKS.register(name, () -> new CrucibleBlock(
                BlockBehaviour.Properties.ofFullCopy(planks).noOcclusion()));
    }

    private static DeferredHolder<Block, Block> registerWoodBarrel(final String name, final Block planks) {
        return BLOCKS.register(name, () -> new BarrelBlock(BlockBehaviour.Properties.ofFullCopy(planks)));
    }

    public static void register(final IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
