package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tints the ore-block overlay layer. The translucent render layer comes from
 * the model's {@code render_type}, not from code. Items keep their pre-coloured
 * textures, so only block items (whose model is the block model) need a colour.
 */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OreColors {

    private static final Map<Block, Integer> BLOCK_COLORS = new IdentityHashMap<>();
    private static final Map<Item, Integer> ITEM_COLORS = new IdentityHashMap<>();
    private static boolean ready;

    private OreColors() {
    }

    private static synchronized void build() {
        if (ready) {
            return;
        }
        for (final OreMaterial material : OreMaterials.all()) {
            for (final String kind : ModOreBlocks.KINDS) {
                final DeferredHolder<Block, Block> holder = ModOreBlocks.byMaterial().get(material.id()).get(kind);
                final Block block = holder.get();
                BLOCK_COLORS.put(block, material.color());
                ITEM_COLORS.put(block.asItem(), material.color());
            }
        }
        ready = true;
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(OreColors::build);
    }

    @SubscribeEvent
    public static void blockColors(final RegisterColorHandlersEvent.Block event) {
        build();
        final List<Block> blocks = new ArrayList<>(BLOCK_COLORS.keySet());
        event.register((state, level, pos, tint) ->
                tint == 0 ? BLOCK_COLORS.getOrDefault(state.getBlock(), -1) : -1,
                blocks.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void itemColors(final RegisterColorHandlersEvent.Item event) {
        build();
        final List<Item> items = new ArrayList<>(ITEM_COLORS.keySet());
        event.register((stack, tint) ->
                tint == 0 ? ITEM_COLORS.getOrDefault(stack.getItem(), -1) : -1,
                items.toArray(new Item[0]));
    }
}
