package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.block.BarrelBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade (玉) plugin: shows compost/dirt/fluid state for the barrel.
 *
 * <p>This class is only loaded by Jade's plugin scanner. Without Jade it is
 * never touched, so the mod still runs standalone.</p>
 */
@WailaPlugin(ExNihiloSatellitium.MOD_ID)
public final class BarrelJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(final IWailaClientRegistration registration) {
        registration.registerBlockComponent(BarrelComponentProvider.INSTANCE, BarrelBlock.class);
    }
}
