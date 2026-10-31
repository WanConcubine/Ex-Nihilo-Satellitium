package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.item.CrookItem;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.common.item.PorcelainBucketItem;
import com.kaifengcrt.exnihilosatellitium.common.item.AncientSporesItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

/** Item registry: meshes, barrels, sieve and the plain porcelain item. */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ExNihiloSatellitium.MOD_ID);

    public static final DeferredHolder<Item, Item> PORCELAIN =
            ITEMS.register("porcelain", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> WOODEN_CRUCIBLE =
            registerCrucibleItem("wooden_crucible", ModBlocks.WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> SPRUCE_WOODEN_CRUCIBLE =
            registerCrucibleItem("spruce_wooden_crucible", ModBlocks.SPRUCE_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> BIRCH_WOODEN_CRUCIBLE =
            registerCrucibleItem("birch_wooden_crucible", ModBlocks.BIRCH_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> JUNGLE_WOODEN_CRUCIBLE =
            registerCrucibleItem("jungle_wooden_crucible", ModBlocks.JUNGLE_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> ACACIA_WOODEN_CRUCIBLE =
            registerCrucibleItem("acacia_wooden_crucible", ModBlocks.ACACIA_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> DARK_OAK_WOODEN_CRUCIBLE =
            registerCrucibleItem("dark_oak_wooden_crucible", ModBlocks.DARK_OAK_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> MANGROVE_WOODEN_CRUCIBLE =
            registerCrucibleItem("mangrove_wooden_crucible", ModBlocks.MANGROVE_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> CHERRY_WOODEN_CRUCIBLE =
            registerCrucibleItem("cherry_wooden_crucible", ModBlocks.CHERRY_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> BAMBOO_WOODEN_CRUCIBLE =
            registerCrucibleItem("bamboo_wooden_crucible", ModBlocks.BAMBOO_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> CRIMSON_WOODEN_CRUCIBLE =
            registerCrucibleItem("crimson_wooden_crucible", ModBlocks.CRIMSON_WOODEN_CRUCIBLE);
    public static final DeferredHolder<Item, Item> WARPED_WOODEN_CRUCIBLE =
            registerCrucibleItem("warped_wooden_crucible", ModBlocks.WARPED_WOODEN_CRUCIBLE);

    public static final List<DeferredHolder<Item, Item>> WOODEN_CRUCIBLES = List.of(
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

    /** M6: unfired porcelain bucket, smelted into the working bucket. */
    public static final DeferredHolder<Item, Item> UNFIRED_PORCELAIN_BUCKET =
            ITEMS.register("unfired_porcelain_bucket", () -> new Item(new Item.Properties().stacksTo(1)));

    /** M6: universal 1000 mB bucket, accepts lava and witch water. */
    public static final DeferredHolder<Item, Item> PORCELAIN_BUCKET =
            ITEMS.register("porcelain_bucket", () -> new PorcelainBucketItem(
                    new Item.Properties().stacksTo(1)));

    /** M6: ancient spores turn dirt into mycelium (the witch water chain start). */
    public static final DeferredHolder<Item, Item> ANCIENT_SPORES =
            ITEMS.register("ancient_spores", () -> new AncientSporesItem(new Item.Properties()));

    /** M6: witch water bucket (fluid id lives in {@link ModFluids}). */
    public static final DeferredHolder<Item, Item> WITCH_WATER_BUCKET =
            ITEMS.register("witch_water_bucket", () -> new BucketItem(ModFluids.WITCH_WATER.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredHolder<Item, Item> BARREL =
            registerBarrelItem("barrel", ModBlocks.BARREL);
    public static final DeferredHolder<Item, Item> SPRUCE_BARREL =
            registerBarrelItem("spruce_barrel", ModBlocks.SPRUCE_BARREL);
    public static final DeferredHolder<Item, Item> BIRCH_BARREL =
            registerBarrelItem("birch_barrel", ModBlocks.BIRCH_BARREL);
    public static final DeferredHolder<Item, Item> JUNGLE_BARREL =
            registerBarrelItem("jungle_barrel", ModBlocks.JUNGLE_BARREL);
    public static final DeferredHolder<Item, Item> ACACIA_BARREL =
            registerBarrelItem("acacia_barrel", ModBlocks.ACACIA_BARREL);
    public static final DeferredHolder<Item, Item> DARK_OAK_BARREL =
            registerBarrelItem("dark_oak_barrel", ModBlocks.DARK_OAK_BARREL);
    public static final DeferredHolder<Item, Item> STONE_BARREL =
            registerBarrelItem("stone_barrel", ModBlocks.STONE_BARREL);

    public static final DeferredHolder<Item, Item> SIEVE =
            ITEMS.register("sieve", () -> new BlockItem(ModBlocks.SIEVE.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> DUST =
            ITEMS.register("dust", () -> new BlockItem(ModBlocks.DUST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> UNFIRED_CRUCIBLE =
            ITEMS.register("unfired_crucible", () -> new BlockItem(ModBlocks.UNFIRED_CRUCIBLE.get(),
                    new Item.Properties()));
    public static final DeferredHolder<Item, Item> CRUCIBLE =
            ITEMS.register("crucible", () -> new BlockItem(ModBlocks.CRUCIBLE.get(),
                    new Item.Properties()));

    public static final DeferredHolder<Item, Item> WOOD_MESH =
            ITEMS.register("wood_mesh", () -> new MeshItem(new Item.Properties().stacksTo(1).durability(10), 1));
    public static final DeferredHolder<Item, Item> SILK_MESH =
            ITEMS.register("silk_mesh", () -> new MeshItem(new Item.Properties().stacksTo(1).durability(64), 2));
    public static final DeferredHolder<Item, Item> IRON_MESH =
            ITEMS.register("iron_mesh", () -> new MeshItem(new Item.Properties().stacksTo(1).durability(256), 3));

    public static final List<DeferredHolder<Item, Item>> MESHES = List.of(WOOD_MESH, SILK_MESH, IRON_MESH);

    public static final DeferredHolder<Item, Item> WOODEN_HAMMER =
            ITEMS.register("wooden_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(64), 1));
    public static final DeferredHolder<Item, Item> STONE_HAMMER =
            ITEMS.register("stone_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(128), 2));
    public static final DeferredHolder<Item, Item> IRON_HAMMER =
            ITEMS.register("iron_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(256), 3));
    public static final DeferredHolder<Item, Item> GOLDEN_HAMMER =
            ITEMS.register("golden_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(32), 4));
    public static final DeferredHolder<Item, Item> DIAMOND_HAMMER =
            ITEMS.register("diamond_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(1561), 5));
    public static final DeferredHolder<Item, Item> NETHERITE_HAMMER =
            ITEMS.register("netherite_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(2031), 6));

    public static final List<DeferredHolder<Item, Item>> HAMMERS = List.of(
            WOODEN_HAMMER, STONE_HAMMER, IRON_HAMMER, GOLDEN_HAMMER, DIAMOND_HAMMER, NETHERITE_HAMMER);

    public static final DeferredHolder<Item, Item> WOODEN_CROOK =
            ITEMS.register("wooden_crook", () -> new CrookItem(
                    new Item.Properties().stacksTo(1).durability(64), 1));
    public static final DeferredHolder<Item, Item> BONE_CROOK =
            ITEMS.register("bone_crook", () -> new CrookItem(
                    new Item.Properties().stacksTo(1).durability(128), 2));

    public static final List<DeferredHolder<Item, Item>> CROOKS = List.of(WOODEN_CROOK, BONE_CROOK);

    public static final DeferredHolder<Item, Item> SILKWORM =
            ITEMS.register("silkworm", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COOKED_SILKWORM =
            ITEMS.register("cooked_silkworm", () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F).build())));

    public static final List<DeferredHolder<Item, Item>> BARREL_ITEMS = List.of(
            BARREL,
            SPRUCE_BARREL,
            BIRCH_BARREL,
            JUNGLE_BARREL,
            ACACIA_BARREL,
            DARK_OAK_BARREL,
            STONE_BARREL);

    private ModItems() {
    }

    private static DeferredHolder<Item, Item> registerCrucibleItem(final String name,
                                                            final DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredHolder<Item, Item> registerBarrelItem(final String name,
                                                           final DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
