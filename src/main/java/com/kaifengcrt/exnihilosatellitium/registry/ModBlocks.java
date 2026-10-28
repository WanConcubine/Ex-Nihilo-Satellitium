package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    public static final RegistryObject<Block> STONE_BARREL =
            BLOCKS.register("stone_barrel",
                    () -> new BarrelBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));

    /** Registration order doubles as creative-tab order. */
    public static final List<RegistryObject<Block>> BARRELS = List.of(
            BARREL,
            SPRUCE_BARREL,
            BIRCH_BARREL,
            JUNGLE_BARREL,
            ACACIA_BARREL,
            DARK_OAK_BARREL,
            STONE_BARREL);

    private ModBlocks() {
    }

    private static RegistryObject<Block> registerWoodBarrel(final String name, final Block planks) {
        return BLOCKS.register(name, () -> new BarrelBlock(BlockBehaviour.Properties.copy(planks)));
    }

    public static void register(final IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
