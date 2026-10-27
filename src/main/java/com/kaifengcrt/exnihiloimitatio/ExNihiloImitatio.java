package com.kaifengcrt.exnihiloimitatio;

import com.kaifengcrt.exnihiloimitatio.registry.ModBlocks;
import com.kaifengcrt.exnihiloimitatio.registry.ModCreativeTabs;
import com.kaifengcrt.exnihiloimitatio.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Entry point of <strong>Ex Nihilo: Imitatio</strong> (无中生有：东施效颦).
 *
 * <p>This is an unofficial, clean-room reimplementation of the Ex Nihilo
 * skyblock progression. It contains no code, textures, models or other
 * assets copied from any existing Ex Nihilo mod. Only publicly documented
 * gameplay concepts are used as a design reference.</p>
 */
@Mod(ExNihiloImitatio.MOD_ID)
public final class ExNihiloImitatio {

    public static final String MOD_ID = "exnihiloimitatio";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExNihiloImitatio(final FMLJavaModLoadingContext context) {
        final IEventBus modBus = context.getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);

        LOGGER.info("Ex Nihilo: Imitatio initialised (clean-room build).");
    }
}
