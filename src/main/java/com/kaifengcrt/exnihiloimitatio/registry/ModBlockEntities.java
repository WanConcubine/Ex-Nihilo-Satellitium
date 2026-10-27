package com.kaifengcrt.exnihiloimitatio.registry;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
import com.kaifengcrt.exnihiloimitatio.common.blockentity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Block entity registry.
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ExNihiloImitatio.MOD_ID);

    public static final RegistryObject<BlockEntityType<BarrelBlockEntity>> BARREL =
            BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder
                    .of(BarrelBlockEntity::new, ModBlocks.BARREL.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(final IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
