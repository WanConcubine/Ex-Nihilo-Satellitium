package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

/** Entry point for Minecraft DataGen; output lands in src/generated/resources. */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModDataGen {

    private ModDataGen() {
    }

    @SubscribeEvent
    public static void gather(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();
        event.createBlockAndItemTags(
                (packOutput, lookup) -> new ModBlockTags(packOutput, lookup,
                        event.getExistingFileHelper()),
                (packOutput, lookup, blockTags) -> new ModItemTags(packOutput, lookup,
                        blockTags, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModLootTables::new,
                        LootContextParamSets.BLOCK)),
                event.getLookupProvider()));
        generator.addProvider(event.includeServer(),
                new ModRecipes(output, event.getLookupProvider()));
    }
}
