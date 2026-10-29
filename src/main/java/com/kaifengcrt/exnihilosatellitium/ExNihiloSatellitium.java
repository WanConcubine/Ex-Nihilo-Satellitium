package com.kaifengcrt.exnihilosatellitium;

import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModCreativeTabs;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Entry point of <strong>Ex Nihilo: Satellitium</strong> (无中生有：众星捧月).
 *
 * <p>Unofficial, clean-room reimplementation; no code or assets are taken from
 * any existing Ex Nihilo mod.</p>
 */
@Mod(ExNihiloSatellitium.MOD_ID)
public final class ExNihiloSatellitium {

    public static final String MOD_ID = "exnihilosatellitium";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExNihiloSatellitium(final IEventBus modBus, final ModContainer modContainer) {
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModItems.register(modBus);
        ModOreBlocks.register(modBus);
        ModOreItems.register(modBus);
        ModCreativeTabs.register(modBus);

        LOGGER.info("Ex Nihilo: Satellitium initialised on NeoForge (clean-room build).");
    }
}
