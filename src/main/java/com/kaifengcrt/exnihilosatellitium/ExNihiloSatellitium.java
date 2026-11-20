package com.kaifengcrt.exnihilosatellitium;

import com.kaifengcrt.exnihilosatellitium.common.config.ConfigValues;
import com.kaifengcrt.exnihilosatellitium.platform.config.ExConfig;
import com.kaifengcrt.exnihilosatellitium.platform.config.ExConfigSource;
import com.kaifengcrt.exnihilosatellitium.common.platform.Platform;
import com.kaifengcrt.exnihilosatellitium.client.ConfigScreens;
import com.kaifengcrt.exnihilosatellitium.compat.MoonlightCompat;
import com.kaifengcrt.exnihilosatellitium.platform.ForgeModProbe;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.common.network.ModNetwork;
import com.kaifengcrt.exnihilosatellitium.registry.ModFluids;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModCreativeTabs;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import com.kaifengcrt.exnihilosatellitium.registry.ModMenuTypes;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Entry point of <strong>Ex Nihilo: Satellitium</strong> (无中生有：众星捧月).
 *
 * <p>This is an unofficial, clean-room reimplementation of the Ex Nihilo
 * skyblock progression. It contains no code, textures, models or other
 * assets copied from any existing Ex Nihilo mod. Only publicly documented
 * gameplay concepts are used as a design reference.</p>
 */
@Mod(ExNihiloSatellitium.MOD_ID)
public final class ExNihiloSatellitium {

    public static final String MOD_ID = "exnihilosatellitium";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExNihiloSatellitium(final FMLJavaModLoadingContext context) {
        final IEventBus modBus = context.getModEventBus();

        Platform.install(new ForgeModProbe());
        ConfigValues.install(ExConfigSource.INSTANCE);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExConfig.SPEC);

        // Forge enables the mod list's "Config" button only when this extension
        // point exists; the screen class is client-only and must not load on a server.
        if (FMLEnvironment.dist.isClient()) {
            ConfigScreens.register();
        }

        ModBlocks.register(modBus);
        ModFluids.register(modBus);
        ModOreBlocks.register(modBus);
        ModOreItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);
        ModMenuTypes.register(modBus);
        ModNetwork.register();

        modBus.addListener(this::commonSetup);

        LOGGER.info("Ex Nihilo: Satellitium initialised (clean-room build).");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MoonlightCompat::logStatus);
    }
}
