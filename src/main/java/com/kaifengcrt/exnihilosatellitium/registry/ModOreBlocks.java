package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The three ore-block states of every material in {@code materials.json}.
 *
 * <p>All of them share three models (gravel / sand / dust): the block face is
 * a vanilla texture plus our mask, and the colour comes from {@code BlockColor}
 * at render time, so adding a material needs no new model or texture.</p>
 */
public final class ModOreBlocks {

    public static final List<String> KINDS = List.of("gravel", "sand", "dust");

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ExNihiloSatellitium.MOD_ID);

    private static final Map<String, Map<String, RegistryObject<Block>>> BY_MATERIAL = new LinkedHashMap<>();

    static {
        for (final OreMaterial material : OreMaterials.all()) {
            final Map<String, RegistryObject<Block>> kinds = new LinkedHashMap<>();
            for (final String kind : KINDS) {
                kinds.put(kind, BLOCKS.register(material.id() + "_ore_" + kind,
                        () -> new Block(properties(kind))));
            }
            BY_MATERIAL.put(material.id(), kinds);
        }
    }

    private ModOreBlocks() {
    }

    private static BlockBehaviour.Properties properties(final String kind) {
        return switch (kind) {
            case "gravel" -> BlockBehaviour.Properties.copy(Blocks.GRAVEL);
            case "sand" -> BlockBehaviour.Properties.copy(Blocks.SAND);
            default -> BlockBehaviour.Properties.copy(Blocks.SAND);
        };
    }

    public static Map<String, Map<String, RegistryObject<Block>>> byMaterial() {
        return BY_MATERIAL;
    }

    public static void register(final IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
