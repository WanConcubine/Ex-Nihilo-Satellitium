package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components (1.21 style item NBT). */
public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ExNihiloSatellitium.MOD_ID);

    /** Contents of the portable porcelain bucket. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidContents>> FLUID_CONTENTS =
            COMPONENTS.register("fluid_contents", () -> DataComponentType.<FluidContents>builder()
                    .persistent(FluidContents.CODEC)
                    .networkSynchronized(FluidContents.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }

    public static void register(final IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    /** Small, registry-free fluid content payload: {@code {id, amount}}. */
    public record FluidContents(ResourceLocation fluid, int amount) {

        public static final Codec<FluidContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(FluidContents::fluid),
                Codec.intRange(0, 64_000).fieldOf("amount").forGetter(FluidContents::amount)
        ).apply(instance, FluidContents::new));

        public static final net.minecraft.network.codec.StreamCodec<
                net.minecraft.network.RegistryFriendlyByteBuf, FluidContents> STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, FluidContents::fluid,
                        net.minecraft.network.codec.ByteBufCodecs.VAR_INT, FluidContents::amount,
                        FluidContents::new);

        public boolean isEmpty() {
            return this.amount <= 0;
        }
    }
}
