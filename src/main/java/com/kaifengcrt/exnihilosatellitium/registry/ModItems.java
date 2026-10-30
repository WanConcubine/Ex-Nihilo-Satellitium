package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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

    public static final DeferredHolder<Item, Item> WOOD_MESH =
            ITEMS.register("wood_mesh", () -> new MeshItem(new Item.Properties().stacksTo(1).durability(10), 1));
    public static final DeferredHolder<Item, Item> SILK_MESH =
            ITEMS.register("silk_mesh", () -> new MeshItem(new Item.Properties().stacksTo(1).durability(64), 2));
    public static final DeferredHolder<Item, Item> IRON_MESH =
            ITEMS.register("iron_mesh", () -> new MeshItem(new Item.Properties().stacksTo(1).durability(256), 3));

    public static final List<DeferredHolder<Item, Item>> MESHES = List.of(WOOD_MESH, SILK_MESH, IRON_MESH);

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

    private static DeferredHolder<Item, Item> registerBarrelItem(final String name,
                                                           final DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
