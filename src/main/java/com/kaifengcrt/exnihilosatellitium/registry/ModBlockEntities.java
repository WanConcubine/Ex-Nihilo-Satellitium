package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Block entity registry.
 *
 * <p>Every barrel material shares this one block entity type; the block class is
 * the same for all of them, so registration is just the list of valid blocks.</p>
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExNihiloSatellitium.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BarrelBlockEntity>> BARREL =
            BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder
                    .of(BarrelBlockEntity::new, ModBlocks.BARRELS.stream()
                            .map(DeferredHolder::get)
                            .toArray(Block[]::new))
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(final IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
