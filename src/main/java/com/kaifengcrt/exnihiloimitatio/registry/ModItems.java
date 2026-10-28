package com.kaifengcrt.exnihiloimitatio.registry;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
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
            DeferredRegister.create(ForgeRegistries.ITEMS, ExNihiloImitatio.MOD_ID);

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
