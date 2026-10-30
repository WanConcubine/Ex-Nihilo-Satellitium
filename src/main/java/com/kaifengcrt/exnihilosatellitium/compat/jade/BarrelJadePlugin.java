package com.kaifengcrt.exnihilosatellitium.compat.jade;

import com.kaifengcrt.exnihilosatellitium.common.block.BarrelBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.CrucibleBlock;
import com.kaifengcrt.exnihilosatellitium.common.block.SieveBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Registers the barrel tooltip with Jade. Jade itself is optional. */
@WailaPlugin
public final class BarrelJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(final IWailaClientRegistration registration) {
        registration.registerBlockComponent(BarrelComponentProvider.INSTANCE, BarrelBlock.class);
        registration.registerBlockComponent(SieveComponentProvider.INSTANCE, SieveBlock.class);
        registration.registerBlockComponent(CrucibleComponentProvider.INSTANCE, CrucibleBlock.class);
    }
}
