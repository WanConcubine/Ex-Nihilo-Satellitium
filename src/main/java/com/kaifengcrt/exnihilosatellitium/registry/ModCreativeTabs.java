package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative tab. Keeps the mod's content in one place and gives the mod a
 * visible entry point in the creative inventory.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExNihiloSatellitium.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ExNihiloSatellitium.MOD_ID))
            .icon(() -> new ItemStack(ModItems.PORCELAIN.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.PORCELAIN.get());
                ModItems.BARREL_ITEMS.forEach(item -> output.accept(item.get()));
                output.accept(ModItems.SIEVE.get());
                ModItems.MESHES.forEach(mesh -> output.accept(mesh.get()));
                output.accept(ModItems.DUST.get());
                output.accept(ModItems.UNFIRED_CRUCIBLE.get());
                output.accept(ModItems.CRUCIBLE.get());
                ModItems.HAMMERS.forEach(hammer -> {
                    // TiC's tool needs a material list before it renders; the
                    // compat hook below adds random-material variants instead.
                    if (!ModItems.TINKERS_HAMMERS.contains(hammer)) {
                        output.accept(hammer.get());
                    }
                });
                ModItems.acceptTinkersCreative(output);
                ModItems.CROOKS.forEach(crook -> output.accept(crook.get()));
                output.accept(ModItems.INFESTED_LEAVES.get());
                output.accept(com.kaifengcrt.exnihilosatellitium.common.item.InfestedLeavesBlockItem
                        .withProgress(new ItemStack(ModItems.INFESTED_LEAVES.get()), 1.0F));
                output.accept(ModItems.SILKWORM.get());
                output.accept(ModItems.COOKED_SILKWORM.get());
                ModItems.WOODEN_CRUCIBLES.forEach(crucible -> output.accept(crucible.get()));
                ModItems.MACHINES.forEach(machine -> output.accept(machine.get()));
                ModItems.UPGRADES.forEach(upgrade -> output.accept(upgrade.get()));
                output.accept(ModItems.UNFIRED_PORCELAIN_BUCKET.get());
                output.accept(ModItems.PORCELAIN_BUCKET.get());
                output.accept(ModItems.WITCH_WATER_BUCKET.get());
                output.accept(ModItems.SUGAR_WATER_BUCKET.get());
                output.accept(ModItems.CONCENTRATED_HONEY_BUCKET.get());
                output.accept(ModItems.GRASS_SEEDS.get());
                ModItems.PEBBLES.forEach(pebble -> output.accept(pebble.get()));
                output.accept(ModItems.CRUSHED_NETHERRACK.get());
                output.accept(ModItems.CRUSHED_END_STONE.get());
                output.accept(ModItems.END_CAKE.get());
                output.accept(ModItems.ANCIENT_SPORES.get());
                if (ModItems.SKY_SLIME_GRASS_SEEDS != null) {
                    output.accept(ModItems.SKY_SLIME_GRASS_SEEDS.get());
                }
                if (ModItems.ENDER_SLIME_GRASS_SEEDS != null) {
                    output.accept(ModItems.ENDER_SLIME_GRASS_SEEDS.get());
                }
                output.accept(ModItems.CRIMSON_NYLIUM_SPORES.get());
                output.accept(ModItems.WARPED_NYLIUM_SPORES.get());
                output.accept(ModItems.DOLL.get());
                output.accept(ModItems.ANGRY_DOLL.get());
                output.accept(ModItems.CREEPY_DOLL.get());
                output.accept(ModItems.SHULKER_DOLL.get());
                output.accept(ModItems.GUARDIAN_DOLL.get());
                output.accept(ModItems.VILLAGER_DOLL.get());
                output.accept(ModItems.BRAIN_PLATINUM.get());
                // Thermal dolls are only registered while Thermal is loaded.
                if (ModItems.BLITZ_DOLL != null) {
                    output.accept(ModItems.BLITZ_DOLL.get());
                }
                if (ModItems.BLIZZ_DOLL != null) {
                    output.accept(ModItems.BLIZZ_DOLL.get());
                }
                for (final OreMaterial material : OreMaterials.all()) {
                    final var entries = ModOreItems.byMaterial().get(material.id());
                    for (final String kind : ModOreBlocks.KINDS) {
                        final RegistryObject<? extends net.minecraft.world.item.Item> item =
                                entries.get("ore_" + kind);
                        output.accept(item.get());
                    }
                    for (final String stage : ModOreItems.STAGES) {
                        output.accept(entries.get(stage).get());
                    }
                    final RegistryObject<Item> product = entries.get("product");
                    if (product != null) {
                        output.accept(product.get());
                    }
                }
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(final IEventBus modBus) {
        TABS.register(modBus);
    }
}
