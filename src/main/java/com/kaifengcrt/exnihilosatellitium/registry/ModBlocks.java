package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.block.BarrelBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.CrucibleBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.EndCakeBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.InfestedLeavesBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.UnfiredCrucibleBlock;
import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.block.MachineBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
            DeferredRegister.create(ForgeRegistries.BLOCKS, ExNihiloSatellitium.MOD_ID);

    /** Oak keeps the plain {@code barrel} id so existing saves and recipes resolve. */
    public static final RegistryObject<Block> BARREL =
            registerWoodBarrel("barrel", Blocks.OAK_PLANKS);
    public static final RegistryObject<Block> SPRUCE_BARREL =
            registerWoodBarrel("spruce_barrel", Blocks.SPRUCE_PLANKS);
    public static final RegistryObject<Block> BIRCH_BARREL =
            registerWoodBarrel("birch_barrel", Blocks.BIRCH_PLANKS);
    public static final RegistryObject<Block> JUNGLE_BARREL =
            registerWoodBarrel("jungle_barrel", Blocks.JUNGLE_PLANKS);
    public static final RegistryObject<Block> ACACIA_BARREL =
            registerWoodBarrel("acacia_barrel", Blocks.ACACIA_PLANKS);
    public static final RegistryObject<Block> DARK_OAK_BARREL =
            registerWoodBarrel("dark_oak_barrel", Blocks.DARK_OAK_PLANKS);
    public static final RegistryObject<Block> MANGROVE_BARREL =
            registerWoodBarrel("mangrove_barrel", Blocks.MANGROVE_PLANKS);
    public static final RegistryObject<Block> CHERRY_BARREL =
            registerWoodBarrel("cherry_barrel", Blocks.CHERRY_PLANKS);
    public static final RegistryObject<Block> BAMBOO_BARREL =
            registerWoodBarrel("bamboo_barrel", Blocks.BAMBOO_PLANKS);
    public static final RegistryObject<Block> CRIMSON_BARREL =
            registerWoodBarrel("crimson_barrel", Blocks.CRIMSON_PLANKS);
    public static final RegistryObject<Block> WARPED_BARREL =
            registerWoodBarrel("warped_barrel", Blocks.WARPED_PLANKS);
    public static final RegistryObject<Block> STONE_BARREL =
            BLOCKS.register("stone_barrel",
                    () -> new BarrelBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));

    /** M4: unfired porcelain crucible (smelted into the working crucible). */
    public static final RegistryObject<Block> UNFIRED_CRUCIBLE =
            BLOCKS.register("unfired_crucible", () -> new UnfiredCrucibleBlock(
                    BlockBehaviour.Properties.copy(Blocks.CLAY).noOcclusion()));

    /** M4: fired crucible with a fluid tank and a heat-driven melt queue. */
    public static final RegistryObject<Block> CRUCIBLE =
            BLOCKS.register("crucible", () -> new CrucibleBlock(
                    BlockBehaviour.Properties.copy(Blocks.QUARTZ_BLOCK).noOcclusion()));

    /** M6: wooden crucibles, melt leaves/saplings into water without a heat source. */
    public static final RegistryObject<Block> WOODEN_CRUCIBLE =
            registerWoodenCrucible("wooden_crucible", Blocks.OAK_PLANKS);
    public static final RegistryObject<Block> SPRUCE_WOODEN_CRUCIBLE =
            registerWoodenCrucible("spruce_wooden_crucible", Blocks.SPRUCE_PLANKS);
    public static final RegistryObject<Block> BIRCH_WOODEN_CRUCIBLE =
            registerWoodenCrucible("birch_wooden_crucible", Blocks.BIRCH_PLANKS);
    public static final RegistryObject<Block> JUNGLE_WOODEN_CRUCIBLE =
            registerWoodenCrucible("jungle_wooden_crucible", Blocks.JUNGLE_PLANKS);
    public static final RegistryObject<Block> ACACIA_WOODEN_CRUCIBLE =
            registerWoodenCrucible("acacia_wooden_crucible", Blocks.ACACIA_PLANKS);
    public static final RegistryObject<Block> DARK_OAK_WOODEN_CRUCIBLE =
            registerWoodenCrucible("dark_oak_wooden_crucible", Blocks.DARK_OAK_PLANKS);
    public static final RegistryObject<Block> MANGROVE_WOODEN_CRUCIBLE =
            registerWoodenCrucible("mangrove_wooden_crucible", Blocks.MANGROVE_PLANKS);
    public static final RegistryObject<Block> CHERRY_WOODEN_CRUCIBLE =
            registerWoodenCrucible("cherry_wooden_crucible", Blocks.CHERRY_PLANKS);
    public static final RegistryObject<Block> BAMBOO_WOODEN_CRUCIBLE =
            registerWoodenCrucible("bamboo_wooden_crucible", Blocks.BAMBOO_PLANKS);
    public static final RegistryObject<Block> CRIMSON_WOODEN_CRUCIBLE =
            registerWoodenCrucible("crimson_wooden_crucible", Blocks.CRIMSON_PLANKS);
    public static final RegistryObject<Block> WARPED_WOODEN_CRUCIBLE =
            registerWoodenCrucible("warped_wooden_crucible", Blocks.WARPED_PLANKS);

    public static final List<RegistryObject<Block>> WOODEN_CRUCIBLES = List.of(
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

    /** M3: the dust block (hammer sand -> dust; sifting dust gives redstone etc.). */
    public static final RegistryObject<Block> DUST =
            BLOCKS.register("dust", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND)));

    /** The End route: a cake that teleports the eater to the End. */
    public static final RegistryObject<Block> END_CAKE =
            BLOCKS.register("end_cake", () -> new EndCakeBlock(
                    BlockBehaviour.Properties.copy(Blocks.CAKE)));

    /** M6: hammered netherrack / end stone, sifted as blocks for nether/end loot. */
    public static final RegistryObject<Block> CRUSHED_NETHERRACK =
            BLOCKS.register("crushed_netherrack", () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.NETHERRACK)));
    public static final RegistryObject<Block> CRUSHED_END_STONE =
            BLOCKS.register("crushed_end_stone", () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.END_STONE)));

    /** M6: infested leaves (silkworm); the block entity renders the original leaves. */
    public static final RegistryObject<Block> INFESTED_LEAVES =
            BLOCKS.register("infested_leaves", () -> new InfestedLeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).noOcclusion().randomTicks()));

    /** M6: witch water source block (fluid rendering comes from the FluidType). */
    public static final RegistryObject<LiquidBlock> WITCH_WATER =
            BLOCKS.register("witch_water", () -> new LiquidBlock(ModFluids.WITCH_WATER,
                    BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static final RegistryObject<Block> SIEVE =
            BLOCKS.register("sieve", () -> new SieveBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)));

    // ------------------------------------------------------------- M10 automation
    public static final RegistryObject<Block> ELECTRIC_SIEVE =
            BLOCKS.register("electric_sieve", () -> new MachineBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK),
                    MachineKind.SIEVE, true));
    public static final RegistryObject<Block> ELECTRIC_HAMMER =
            BLOCKS.register("electric_hammer", () -> new MachineBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK),
                    MachineKind.HAMMER, true));
    public static final RegistryObject<Block> FUEL_SIEVE =
            BLOCKS.register("fuel_sieve", () -> new MachineBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3.5F, 6.0F),
                    MachineKind.SIEVE, false));
    public static final RegistryObject<Block> FUEL_HAMMER =
            BLOCKS.register("fuel_hammer", () -> new MachineBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3.5F, 6.0F),
                    MachineKind.HAMMER, false));

    /** Registration order doubles as creative-tab order. */
    public static final List<RegistryObject<Block>> BARRELS = List.of(
            BARREL,
            SPRUCE_BARREL,
            BIRCH_BARREL,
            JUNGLE_BARREL,
            ACACIA_BARREL,
            DARK_OAK_BARREL,
            MANGROVE_BARREL,
            CHERRY_BARREL,
            BAMBOO_BARREL,
            CRIMSON_BARREL,
            WARPED_BARREL,
            STONE_BARREL);

    private ModBlocks() {
    }

    private static RegistryObject<Block> registerWoodBarrel(final String name, final Block planks) {
        return BLOCKS.register(name, () -> new BarrelBlock(BlockBehaviour.Properties.copy(planks)));
    }

    private static RegistryObject<Block> registerWoodenCrucible(final String name, final Block planks) {
        return BLOCKS.register(name, () -> new CrucibleBlock(
                BlockBehaviour.Properties.copy(planks).noOcclusion()));
    }

    public static void register(final IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
