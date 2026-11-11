package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * Adds a slime-dirt drop to Tinkers' "vanilla soil" slime grass blocks.
 *
 * <p>Our slime grass seeds plant {@code tconstruct:<type>_vanilla_slime_grass}. TiC's own loot
 * table for that block only drops plain dirt, so a skyblock would never see slime dirt. We do
 * not replace TiC's table: one extra pool is appended, so the original dirt drop stays and the
 * matching slime dirt is added next to it. The slime-typed grass
 * ({@code <type>_<type>_slime_grass}) already drops slime dirt through TiC's own table.</p>
 */
@Mod.EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LootEvents {

    /** {@code tconstruct:blocks/<vanilla slime grass>} -> the slime dirt item to add. */
    private static final Map<ResourceLocation, ResourceLocation> EXTRA_SLIME_DIRT = Map.of(
            table("sky_vanilla_slime_grass"), item("sky_slime_dirt"),
            table("earth_vanilla_slime_grass"), item("earth_slime_dirt"),
            table("ender_vanilla_slime_grass"), item("ender_slime_dirt"));

    private LootEvents() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(final LootTableLoadEvent event) {
        final ResourceLocation drop = EXTRA_SLIME_DIRT.get(event.getName());
        if (drop == null) {
            return;
        }
        final Item dirt = BuiltInRegistries.ITEM.get(drop);
        if (dirt == Items.AIR) {
            return;
        }
        event.getTable().addPool(LootPool.lootPool()
                .add(LootItem.lootTableItem(dirt))
                .build());
    }

    private static ResourceLocation table(final String block) {
        return new ResourceLocation("tconstruct", "blocks/" + block);
    }

    private static ResourceLocation item(final String path) {
        return new ResourceLocation("tconstruct", path);
    }
}
