package com.kaifengcrt.exnihilosatellitium.common.network;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Mod network channel (Forge 1.20.1 {@link SimpleChannel}). Only one
 * client-to-server message so far: the machine auto-output configuration
 * written by the machine screen's output panel.
 */
public final class ModNetwork {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExNihiloSatellitium.MOD_ID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private ModNetwork() {
    }

    /** Called once from the mod constructor. */
    public static void register() {
        int index = 0;
        CHANNEL.registerMessage(index++, MachineOutputMessage.class,
                MachineOutputMessage::encode, MachineOutputMessage::decode,
                MachineOutputMessage::handle);
    }
}
