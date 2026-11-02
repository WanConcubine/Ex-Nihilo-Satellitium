package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

/** Entry point for Minecraft DataGen; output lands in src/generated/resources. */
@Mod.EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModDataGen {

    private ModDataGen() {
    }

    @SubscribeEvent
    public static void gather(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();

        final ModBlockTags blockTags = new ModBlockTags(output, event.getLookupProvider(),
                event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ModItemTags(output,
                event.getLookupProvider(), blockTags.contentsGetter(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModLootTables::new,
                        LootContextParamSets.BLOCK))));
        generator.addProvider(event.includeServer(), new ModRecipes(output));
        // Blockstates first: the item provider validates block-item parents against
        // the models generated here (ExistingFileHelper#trackGenerated).
        generator.addProvider(event.includeClient(),
                new ModBlockStates(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(),
                new ModItemModels(output, event.getExistingFileHelper()));
    }
}
