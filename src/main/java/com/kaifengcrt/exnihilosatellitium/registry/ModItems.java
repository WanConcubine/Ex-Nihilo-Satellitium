package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.item.AncientSporesItem;
import com.kaifengcrt.exnihilosatellitium.common.item.BrainPlatinumItem;
import com.kaifengcrt.exnihilosatellitium.common.item.CrookItem;
import com.kaifengcrt.exnihilosatellitium.common.item.GrassSeedsItem;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import com.kaifengcrt.exnihilosatellitium.common.item.InfestedLeavesBlockItem;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.common.item.NyliumSporesItem;
import com.kaifengcrt.exnihilosatellitium.common.item.PorcelainBucketItem;
import com.kaifengcrt.exnihilosatellitium.common.item.SilkwormItem;
import com.kaifengcrt.exnihilosatellitium.common.item.TinkersGrassSeedItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeType;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static final RegistryObject<Item> MANGROVE_BARREL =
            registerBarrelItem("mangrove_barrel", ModBlocks.MANGROVE_BARREL);
    public static final RegistryObject<Item> CHERRY_BARREL =
            registerBarrelItem("cherry_barrel", ModBlocks.CHERRY_BARREL);
    public static final RegistryObject<Item> BAMBOO_BARREL =
            registerBarrelItem("bamboo_barrel", ModBlocks.BAMBOO_BARREL);
    public static final RegistryObject<Item> CRIMSON_BARREL =
            registerBarrelItem("crimson_barrel", ModBlocks.CRIMSON_BARREL);
    public static final RegistryObject<Item> WARPED_BARREL =
            registerBarrelItem("warped_barrel", ModBlocks.WARPED_BARREL);
    public static final RegistryObject<Item> STONE_BARREL =
            registerBarrelItem("stone_barrel", ModBlocks.STONE_BARREL);

    public static final List<RegistryObject<Item>> BARREL_ITEMS = List.of(
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

    /** M10: the four automation machines (electric / fuel x sieve / hammer). */
    public static final RegistryObject<Item> ELECTRIC_SIEVE =
            ITEMS.register("electric_sieve", () -> new BlockItem(ModBlocks.ELECTRIC_SIEVE.get(),
                    new Item.Properties()));
    public static final RegistryObject<Item> ELECTRIC_HAMMER =
            ITEMS.register("electric_hammer", () -> new BlockItem(ModBlocks.ELECTRIC_HAMMER.get(),
                    new Item.Properties()));
    public static final RegistryObject<Item> FUEL_SIEVE =
            ITEMS.register("fuel_sieve", () -> new BlockItem(ModBlocks.FUEL_SIEVE.get(),
                    new Item.Properties()));
    public static final RegistryObject<Item> FUEL_HAMMER =
            ITEMS.register("fuel_hammer", () -> new BlockItem(ModBlocks.FUEL_HAMMER.get(),
                    new Item.Properties()));

    public static final List<RegistryObject<Item>> MACHINES = List.of(
            ELECTRIC_SIEVE, ELECTRIC_HAMMER, FUEL_SIEVE, FUEL_HAMMER);

    /** M10: speed / fortune upgrades, three tiers each. */
    public static final RegistryObject<Item> SPEED_UPGRADE_1 =
            registerUpgrade("speed_upgrade_1", UpgradeType.SPEED, 1);
    public static final RegistryObject<Item> SPEED_UPGRADE_2 =
            registerUpgrade("speed_upgrade_2", UpgradeType.SPEED, 2);
    public static final RegistryObject<Item> SPEED_UPGRADE_3 =
            registerUpgrade("speed_upgrade_3", UpgradeType.SPEED, 3);
    public static final RegistryObject<Item> FORTUNE_UPGRADE_1 =
            registerUpgrade("fortune_upgrade_1", UpgradeType.FORTUNE, 1);
    public static final RegistryObject<Item> FORTUNE_UPGRADE_2 =
            registerUpgrade("fortune_upgrade_2", UpgradeType.FORTUNE, 2);
    public static final RegistryObject<Item> FORTUNE_UPGRADE_3 =
            registerUpgrade("fortune_upgrade_3", UpgradeType.FORTUNE, 3);

    public static final List<RegistryObject<Item>> UPGRADES = List.of(
            SPEED_UPGRADE_1, SPEED_UPGRADE_2, SPEED_UPGRADE_3,
            FORTUNE_UPGRADE_1, FORTUNE_UPGRADE_2, FORTUNE_UPGRADE_3);

    public static final RegistryObject<Item> UNFIRED_CRUCIBLE =
            ITEMS.register("unfired_crucible", () -> new BlockItem(ModBlocks.UNFIRED_CRUCIBLE.get(),
                    new Item.Properties()));
    public static final RegistryObject<Item> CRUCIBLE =
            ITEMS.register("crucible", () -> new BlockItem(ModBlocks.CRUCIBLE.get(),
                    new Item.Properties()));

    /** M6: the 11 wooden crucible variants, registered after the porcelain one. */
    public static final RegistryObject<Item> WOODEN_CRUCIBLE =
            registerCrucibleItem("wooden_crucible", ModBlocks.WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> SPRUCE_WOODEN_CRUCIBLE =
            registerCrucibleItem("spruce_wooden_crucible", ModBlocks.SPRUCE_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> BIRCH_WOODEN_CRUCIBLE =
            registerCrucibleItem("birch_wooden_crucible", ModBlocks.BIRCH_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> JUNGLE_WOODEN_CRUCIBLE =
            registerCrucibleItem("jungle_wooden_crucible", ModBlocks.JUNGLE_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> ACACIA_WOODEN_CRUCIBLE =
            registerCrucibleItem("acacia_wooden_crucible", ModBlocks.ACACIA_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> DARK_OAK_WOODEN_CRUCIBLE =
            registerCrucibleItem("dark_oak_wooden_crucible", ModBlocks.DARK_OAK_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> MANGROVE_WOODEN_CRUCIBLE =
            registerCrucibleItem("mangrove_wooden_crucible", ModBlocks.MANGROVE_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> CHERRY_WOODEN_CRUCIBLE =
            registerCrucibleItem("cherry_wooden_crucible", ModBlocks.CHERRY_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> BAMBOO_WOODEN_CRUCIBLE =
            registerCrucibleItem("bamboo_wooden_crucible", ModBlocks.BAMBOO_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> CRIMSON_WOODEN_CRUCIBLE =
            registerCrucibleItem("crimson_wooden_crucible", ModBlocks.CRIMSON_WOODEN_CRUCIBLE);
    public static final RegistryObject<Item> WARPED_WOODEN_CRUCIBLE =
            registerCrucibleItem("warped_wooden_crucible", ModBlocks.WARPED_WOODEN_CRUCIBLE);

    public static final List<RegistryObject<Item>> WOODEN_CRUCIBLES = List.of(
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
    public static final RegistryObject<Item> UNFIRED_PORCELAIN_BUCKET =
            ITEMS.register("unfired_porcelain_bucket", () -> new Item(new Item.Properties().stacksTo(1)));

    /** M6: universal 1000 mB bucket, accepts lava and witch water. */
    public static final RegistryObject<Item> PORCELAIN_BUCKET =
            ITEMS.register("porcelain_bucket", () -> new PorcelainBucketItem(
                    new Item.Properties().stacksTo(1)));

    /** M6: grass seeds turn dirt into grass; hammered netherrack / end stone. */
    /** Dirt sifting pebbles: four of a kind craft the matching stone block. */
    public static final RegistryObject<Item> COBBLESTONE_PEBBLE =
            ITEMS.register("cobblestone_pebble", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANDESITE_PEBBLE =
            ITEMS.register("andesite_pebble", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIORITE_PEBBLE =
            ITEMS.register("diorite_pebble", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GRANITE_PEBBLE =
            ITEMS.register("granite_pebble", () -> new Item(new Item.Properties()));
    /** 2026-11-05: deepslate source for the Primitive Alloy Smelter. */
    public static final RegistryObject<Item> DEEPSLATE_PEBBLE =
            ITEMS.register("deepslate_pebble", () -> new Item(new Item.Properties()));

    public static final List<RegistryObject<Item>> PEBBLES = List.of(
            COBBLESTONE_PEBBLE, ANDESITE_PEBBLE, DIORITE_PEBBLE, GRANITE_PEBBLE,
            DEEPSLATE_PEBBLE);

    public static final RegistryObject<Item> GRASS_SEEDS =
            ITEMS.register("grass_seeds", () -> new GrassSeedsItem(new Item.Properties()));
    public static final RegistryObject<Item> CRUSHED_NETHERRACK =
            ITEMS.register("crushed_netherrack", () -> new BlockItem(ModBlocks.CRUSHED_NETHERRACK.get(),
                    new Item.Properties()));
    public static final RegistryObject<Item> CRUSHED_END_STONE =
            ITEMS.register("crushed_end_stone", () -> new BlockItem(ModBlocks.CRUSHED_END_STONE.get(),
                    new Item.Properties()));

    /** The End route: a cake that teleports the eater; ender eyes add slices. */
    public static final RegistryObject<Item> END_CAKE =
            ITEMS.register("end_cake", () -> new BlockItem(ModBlocks.END_CAKE.get(),
                    new Item.Properties().stacksTo(64)));

    /** M6: dolls, the nether/end mob recipe (angry -> blaze, creepy -> enderman). */
    public static final RegistryObject<Item> DOLL =
            ITEMS.register("doll", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ANGRY_DOLL =
            ITEMS.register("angry_doll", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> CREEPY_DOLL =
            ITEMS.register("creepy_doll", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHULKER_DOLL =
            ITEMS.register("shulker_doll", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> GUARDIAN_DOLL =
            ITEMS.register("guardian_doll", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> VILLAGER_DOLL =
            ITEMS.register("villager_doll", () -> new Item(new Item.Properties().stacksTo(16)));

    /** "Brain Platinum": turns a nitwit villager into an ordinary unemployed villager. */
    public static final RegistryObject<Item> BRAIN_PLATINUM =
            ITEMS.register("brain_platinum", () -> new BrainPlatinumItem(
                    new Item.Properties().stacksTo(16)));

    /**
     * Thermal dolls: the crafting catalyst (niter dust) and the summoned mobs
     * both come from Thermal (1.20.1 only), so the items only exist while
     * Thermal Foundation is loaded. The fields stay {@code null} otherwise and
     * the hand-written recipe / barrel JSON is gated on the same mod id.
     */
    @Nullable
    public static final RegistryObject<Item> BLITZ_DOLL =
            registerThermalDoll("blitz_doll");
    @Nullable
    public static final RegistryObject<Item> BLIZZ_DOLL =
            registerThermalDoll("blizz_doll");

    /** M6: infested leaves item; the block-entity tag picks 0 % or mature. */
    public static final RegistryObject<Item> INFESTED_LEAVES =
            ITEMS.register("infested_leaves", () -> new InfestedLeavesBlockItem(
                    ModBlocks.INFESTED_LEAVES.get(), new Item.Properties()));

    /** M6: ancient spores turn dirt into mycelium (the witch water chain start). */
    public static final RegistryObject<Item> ANCIENT_SPORES =
            ITEMS.register("ancient_spores", () -> new AncientSporesItem(new Item.Properties()));

    /**
     * Tinkers-only grass seeds. Right-click vanilla dirt / grass to grow the
     * matching slime grass. The slime-typed variant is used on purpose: mining it
     * (no silk touch) drops the matching {@code tconstruct:<type>_slime_dirt},
     * which is the only skyblock source of slime soil. All three fields stay
     * {@code null} when Tinkers is absent.
     */
    public static final RegistryObject<Item> SKY_SLIME_GRASS_SEEDS =
            registerTinkersGrassSeeds("sky_slime_grass_seeds",
                    "tconstruct:sky_sky_slime_grass");
    public static final RegistryObject<Item> EARTH_SLIME_GRASS_SEEDS =
            registerTinkersGrassSeeds("earth_slime_grass_seeds",
                    "tconstruct:earth_earth_slime_grass");
    public static final RegistryObject<Item> ENDER_SLIME_GRASS_SEEDS =
            registerTinkersGrassSeeds("ender_slime_grass_seeds",
                    "tconstruct:ender_ender_slime_grass");

    /**
     * Nether-tree start: crushing netherrack drops these spores, and right-clicking
     * netherrack with them grows the matching nylium. Vanilla bone meal does the rest.
     */
    public static final RegistryObject<Item> CRIMSON_NYLIUM_SPORES =
            ITEMS.register("crimson_nylium_spores", () -> new NyliumSporesItem(
                    new Item.Properties(), Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM,
                    ParticleTypes.CRIMSON_SPORE));
    public static final RegistryObject<Item> WARPED_NYLIUM_SPORES =
            ITEMS.register("warped_nylium_spores", () -> new NyliumSporesItem(
                    new Item.Properties(), Blocks.NETHERRACK, Blocks.WARPED_NYLIUM,
                    ParticleTypes.WARPED_SPORE));

    /** M6: witch water bucket (fluid id lives in {@link ModFluids}). */
    public static final RegistryObject<Item> WITCH_WATER_BUCKET =
            ITEMS.register("witch_water_bucket", () -> new BucketItem(ModFluids.WITCH_WATER,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegistryObject<Item> SUGAR_WATER_BUCKET =
            ITEMS.register("sugar_water_bucket", () -> new BucketItem(ModFluids.SUGAR_WATER,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> CONCENTRATED_HONEY_BUCKET =
            ITEMS.register("concentrated_honey_bucket", () -> new BucketItem(
                    ModFluids.CONCENTRATED_HONEY,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

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
    /**
     * Extra hammers for the metals in {@code materials.json}.
     *
     * <p>Same 64-point base as the vanilla hammers: tin / aluminium / zinc /
     * copper 256, silver 320, lead 384, nickel 448, osmium 512, platinum 1920.
     * Cobalt and ardite go the Tinkers route and uranium is left out on
     * purpose, so none of those gets a hammer here. The registry is generated
     * from the material table: disabling a metal removes its hammer and its
     * recipe instead of leaving an item whose ingot tag is empty.</p>
     */
    private static final Map<String, HammerSpec> METAL_HAMMER_SPECS = metalHammerSpecs();

    public static final Map<String, RegistryObject<Item>> METAL_HAMMERS = registerMetalHammers();

    /** BambooMod 0.6.2 gems; this line only exists on the 1.20.1 branch. */
    @Nullable
    public static final RegistryObject<Item> SAKURA_HAMMER = bambooHammer("sakura", 6, 2048);
    @Nullable
    public static final RegistryObject<Item> MAPLE_HAMMER = bambooHammer("maple", 6, 2048);
    @Nullable
    public static final RegistryObject<Item> GINKGO_HAMMER = bambooHammer("ginkgo", 6, 2048);

    /** Optional DE hammers; loaded reflectively only when DE is present. */
    private static final List<RegistryObject<Item>> DE_HAMMERS = draconicHammers();

    /**
     * Optional Tinkers' Construct hammer. A real TiC modifiable tool (built at
     * the Tinker Station from a hammer head and a tough handle), registered
     * only while {@code tconstruct} is loaded. It is kept
     * out of the plain creative-tab loop because a bare TiC tool has no
     * materials; {@link #acceptTinkersCreative} asks TiC for material variants.
     */
    public static final List<RegistryObject<Item>> TINKERS_HAMMERS = tinkersHammers();

    public static final List<RegistryObject<Item>> HAMMERS = allHammers();

    private static Map<String, HammerSpec> metalHammerSpecs() {
        final Map<String, HammerSpec> specs = new LinkedHashMap<>();
        specs.put("tin", new HammerSpec(3, 256));
        specs.put("aluminum", new HammerSpec(3, 256));
        specs.put("zinc", new HammerSpec(3, 256));
        specs.put("copper", new HammerSpec(3, 256));
        specs.put("silver", new HammerSpec(4, 320));
        specs.put("lead", new HammerSpec(4, 384));
        specs.put("nickel", new HammerSpec(4, 448));
        specs.put("osmium", new HammerSpec(4, 512));
        specs.put("platinum", new HammerSpec(5, 1920));
        return Collections.unmodifiableMap(specs);
    }

    private static Map<String, RegistryObject<Item>> registerMetalHammers() {
        final Set<String> materials = OreMaterials.all().stream()
                .map(OreMaterial::id)
                .collect(Collectors.toSet());
        final Map<String, RegistryObject<Item>> hammers = new LinkedHashMap<>();
        METAL_HAMMER_SPECS.forEach((id, spec) -> {
            if (!materials.contains(id)) {
                return;
            }
            hammers.put(id, ITEMS.register(id + "_hammer", () -> new HammerItem(
                    new Item.Properties().stacksTo(1).durability(spec.durability()), spec.tier())));
        });
        return Collections.unmodifiableMap(hammers);
    }

    private static List<RegistryObject<Item>> allHammers() {
        final List<RegistryObject<Item>> hammers = new ArrayList<>(List.of(
                WOODEN_HAMMER, STONE_HAMMER, IRON_HAMMER, GOLDEN_HAMMER,
                DIAMOND_HAMMER, NETHERITE_HAMMER));
        hammers.addAll(METAL_HAMMERS.values());
        addHammer(hammers, SAKURA_HAMMER);
        addHammer(hammers, MAPLE_HAMMER);
        addHammer(hammers, GINKGO_HAMMER);
        hammers.addAll(DE_HAMMERS);
        hammers.addAll(TINKERS_HAMMERS);
        return List.copyOf(hammers);
    }

    /**
     * Registers the TiC hammer through the optional {@code tinkers} source set.
     * The compat class is only loaded when Tinkers is on the mod list, so a
     * normal build / game never links against TConstruct.
     */
    @SuppressWarnings("unchecked")
    private static List<RegistryObject<Item>> tinkersHammers() {
        if (ModList.get() == null || !ModList.get().isLoaded("tconstruct")) {
            return List.of();
        }
        try {
            final Class<?> type = Class.forName(
                    "com.kaifengcrt.exnihilosatellitium.compat.tconstruct.TinkersTools");
            final Object value = type.getMethod("register", DeferredRegister.class)
                    .invoke(null, ITEMS);
            return (List<RegistryObject<Item>>) value;
        } catch (final ReflectiveOperationException | ClassCastException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[tinkers] failed to register the Tinkers' Construct hammer", exception);
            return List.of();
        }
    }

    /** Adds the TiC hammer's random-material creative variants, when present. */
    public static void acceptTinkersCreative(final CreativeModeTab.Output output) {
        if (TINKERS_HAMMERS.isEmpty()) {
            return;
        }
        try {
            Class.forName("com.kaifengcrt.exnihilosatellitium.compat.tconstruct.TinkersTools")
                    .getMethod("addCreativeItems", CreativeModeTab.Output.class)
                    .invoke(null, output);
        } catch (final ReflectiveOperationException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[tinkers] failed to build creative tool variants", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<RegistryObject<Item>> draconicHammers() {
        if (ModList.get() == null || !ModList.get().isLoaded("draconicevolution")) {
            return List.of();
        }
        try {
            final Class<?> type = Class.forName(
                    "com.kaifengcrt.exnihilosatellitium.compat.draconicevolution.DEHammers");
            final Object value = type.getMethod("register", DeferredRegister.class)
                    .invoke(null, ITEMS);
            return (List<RegistryObject<Item>>) value;
        } catch (final ReflectiveOperationException | ClassCastException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[de] failed to register Draconic Evolution hammers", exception);
            return List.of();
        }
    }

    private static void addHammer(final List<RegistryObject<Item>> hammers,
                                  @Nullable final RegistryObject<Item> hammer) {
        if (hammer != null) {
            hammers.add(hammer);
        }
    }

    /**
     * Conditional gem hammer: registered only while BambooMod is loaded, or in
     * the datagen JVM so the icon/model exists for packs that do install it.
     */
    @Nullable
    private static RegistryObject<Item> bambooHammer(final String name, final int tier,
                                                     final int durability) {
        final boolean datagen = Boolean.getBoolean("exnihilosatellitium.datagen");
        if (!datagen && (ModList.get() == null || !ModList.get().isLoaded("bamboomod"))) {
            return null;
        }
        return ITEMS.register(name + "_hammer", () -> new HammerItem(
                new Item.Properties().stacksTo(1).durability(durability), tier));
    }

    /** Registry metadata for one generated metal hammer. */
    private record HammerSpec(int tier, int durability) {
    }

    public static final RegistryObject<Item> WOODEN_CROOK =
            ITEMS.register("wooden_crook", () -> new CrookItem(
                    new Item.Properties().stacksTo(1).durability(64), 1));
    public static final RegistryObject<Item> BONE_CROOK =
            ITEMS.register("bone_crook", () -> new CrookItem(
                    new Item.Properties().stacksTo(1).durability(128), 2));
    public static final List<RegistryObject<Item>> CROOKS = List.of(WOODEN_CROOK, BONE_CROOK);

    /** M6: silkworms infest leaves; cooked silkworms are a small snack. */
    public static final RegistryObject<Item> SILKWORM =
            ITEMS.register("silkworm", () -> new SilkwormItem(new Item.Properties()));
    public static final RegistryObject<Item> COOKED_SILKWORM =
            ITEMS.register("cooked_silkworm", () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.6F).build())));

    private ModItems() {
    }

    private static RegistryObject<Item> registerBarrelItem(final String name,
                                                           final RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Item> registerUpgrade(final String name, final UpgradeType type,
                                                        final int tier) {
        return ITEMS.register(name, () -> new UpgradeItem(
                new Item.Properties().stacksTo(8), type, tier));
    }

    private static RegistryObject<Item> registerCrucibleItem(final String name,
                                                             final RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /** Conditional item: absent unless Tinkers is on the mod list. */
    private static RegistryObject<Item> registerTinkersGrassSeeds(final String name,
                                                                  final String targetBlock) {
        if (ModList.get() == null || !ModList.get().isLoaded("tconstruct")) {
            return null;
        }
        return ITEMS.register(name, () -> new TinkersGrassSeedItem(
                new Item.Properties(), targetBlock));
    }

    /** Conditional item: absent unless Thermal Foundation is on the mod list. */
    @Nullable
    private static RegistryObject<Item> registerThermalDoll(final String name) {
        if (ModList.get() == null || !ModList.get().isLoaded("thermal_foundation")) {
            return null;
        }
        return ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(16)));
    }

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
        registerTinkersModifiers(modBus);
        registerTinkersClient(modBus);
        registerDraconicModules(modBus);
    }

    /**
     * Registers the Draconic Evolution chain module. The module lives in DE's
     * own registry, which DE creates in {@code NewRegistryEvent}; the compat
     * class attaches to that registry's {@code RegisterEvent}. Reflective so a
     * build without DE never links the class.
     */
    private static void registerDraconicModules(final IEventBus modBus) {
        if (DE_HAMMERS.isEmpty()) {
            return;
        }
        try {
            Class.forName(
                            "com.kaifengcrt.exnihilosatellitium.compat.draconicevolution.DEHammers")
                    .getMethod("registerModules", IEventBus.class, DeferredRegister.class)
                    .invoke(null, modBus, ITEMS);
        } catch (final ReflectiveOperationException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[de] failed to register the chain module", exception);
        }
    }

    /** Creative-tab entry for the DE chain module, when DE is loaded. */
    public static void acceptDraconicCreative(final CreativeModeTab.Output output) {
        if (DE_HAMMERS.isEmpty()) {
            return;
        }
        try {
            Class.forName(
                            "com.kaifengcrt.exnihilosatellitium.compat.draconicevolution.DEHammers")
                    .getMethod("addCreativeItems", CreativeModeTab.Output.class)
                    .invoke(null, output);
        } catch (final ReflectiveOperationException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[de] failed to build the chain module creative entry", exception);
        }
    }

    /**
     * Registers the TiC {@code chain} modifier. TiC fires its
     * {@code ModifierRegistrationEvent} in common setup, so the listener has to
     * be on the mod bus from the constructor. Reflective for the same reason as
     * the client colours: a build without TConstruct must never link the class.
     */
    private static void registerTinkersModifiers(final IEventBus modBus) {
        if (TINKERS_HAMMERS.isEmpty()) {
            return;
        }
        try {
            Class.forName("com.kaifengcrt.exnihilosatellitium.compat.tconstruct.TinkersTools")
                    .getMethod("registerModifiers", IEventBus.class)
                    .invoke(null, modBus);
        } catch (final ReflectiveOperationException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[tinkers] failed to register the chain modifier", exception);
        }
    }

    /**
     * Registers the TiC model colour handler, but only on a client that has
     * Tinkers. Loading {@code TinkersClient} without TConstruct would fail on
     * its TiC imports, so the call is reflective and guarded here instead of
     * using {@code @EventBusSubscriber} on the compat class.
     */
    private static void registerTinkersClient(final IEventBus modBus) {
        if (TINKERS_HAMMERS.isEmpty() || FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        try {
            Class.forName("com.kaifengcrt.exnihilosatellitium.compat.tconstruct.TinkersTools")
                    .getMethod("registerClient", IEventBus.class)
                    .invoke(null, modBus);
        } catch (final ReflectiveOperationException exception) {
            ExNihiloSatellitium.LOGGER.error(
                    "[tinkers] failed to register the client model colours", exception);
        }
    }
}
