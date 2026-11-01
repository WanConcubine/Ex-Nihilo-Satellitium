package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.item.CrookItem;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Item registry. Placeholder content lives here until the first gameplay
 * milestone introduces hammers, crooks, meshes and ore pieces.
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExNihiloSatellitium.MOD_ID);

    public static final RegistryObject<Item> PORCELAIN =
            ITEMS.register("porcelain", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BARREL =
            registerBarrelItem("barrel", ModBlocks.BARREL);
    public static final RegistryObject<Item> SPRUCE_BARREL =
            registerBarrelItem("spruce_barrel", ModBlocks.SPRUCE_BARREL);
    public static final RegistryObject<Item> BIRCH_BARREL =
            registerBarrelItem("birch_barrel", ModBlocks.BIRCH_BARREL);
    public static final RegistryObject<Item> JUNGLE_BARREL =
            registerBarrelItem("jungle_barrel", ModBlocks.JUNGLE_BARREL);
    public static final RegistryObject<Item> ACACIA_BARREL =
            registerBarrelItem("acacia_barrel", ModBlocks.ACACIA_BARREL);
    public static final RegistryObject<Item> DARK_OAK_BARREL =
            registerBarrelItem("dark_oak_barrel", ModBlocks.DARK_OAK_BARREL);
    public static final RegistryObject<Item> STONE_BARREL =
            registerBarrelItem("stone_barrel", ModBlocks.STONE_BARREL);

    public static final List<RegistryObject<Item>> BARREL_ITEMS = List.of(
            BARREL,
            SPRUCE_BARREL,
            BIRCH_BARREL,
            JUNGLE_BARREL,
            ACACIA_BARREL,
            DARK_OAK_BARREL,
            STONE_BARREL);

    public static final RegistryObject<Item> SIEVE =
            ITEMS.register("sieve", () -> new BlockItem(ModBlocks.SIEVE.get(), new Item.Properties()));

    public static final RegistryObject<Item> WOOD_MESH =
            ITEMS.register("wood_mesh", () -> new MeshItem(
                    new Item.Properties().stacksTo(1).durability(10), 1));
    public static final RegistryObject<Item> SILK_MESH =
            ITEMS.register("silk_mesh", () -> new MeshItem(
                    new Item.Properties().stacksTo(1).durability(64), 2));
    public static final RegistryObject<Item> IRON_MESH =
            ITEMS.register("iron_mesh", () -> new MeshItem(
                    new Item.Properties().stacksTo(1).durability(256), 3));
    public static final List<RegistryObject<Item>> MESHES = List.of(WOOD_MESH, SILK_MESH, IRON_MESH);

    public static final RegistryObject<Item> DUST =
            ITEMS.register("dust", () -> new BlockItem(ModBlocks.DUST.get(), new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_HAMMER =
            ITEMS.register("wooden_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(64), 1));
    public static final RegistryObject<Item> STONE_HAMMER =
            ITEMS.register("stone_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(128), 2));
    public static final RegistryObject<Item> IRON_HAMMER =
            ITEMS.register("iron_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(256), 3));
    public static final RegistryObject<Item> GOLDEN_HAMMER =
            ITEMS.register("golden_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(32), 4));
    public static final RegistryObject<Item> DIAMOND_HAMMER =
            ITEMS.register("diamond_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(1561), 5));
    public static final RegistryObject<Item> NETHERITE_HAMMER =
            ITEMS.register("netherite_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(2031), 6));
    public static final List<RegistryObject<Item>> HAMMERS = List.of(
            WOODEN_HAMMER, STONE_HAMMER, IRON_HAMMER, GOLDEN_HAMMER, DIAMOND_HAMMER, NETHERITE_HAMMER);

    public static final RegistryObject<Item> WOODEN_CROOK =
            ITEMS.register("wooden_crook", () -> new CrookItem(
                    new Item.Properties().stacksTo(1).durability(64), 1));
    public static final RegistryObject<Item> BONE_CROOK =
            ITEMS.register("bone_crook", () -> new CrookItem(
                    new Item.Properties().stacksTo(1).durability(128), 2));
    public static final List<RegistryObject<Item>> CROOKS = List.of(WOODEN_CROOK, BONE_CROOK);

    private ModItems() {
    }

    private static RegistryObject<Item> registerBarrelItem(final String name,
                                                           final RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
