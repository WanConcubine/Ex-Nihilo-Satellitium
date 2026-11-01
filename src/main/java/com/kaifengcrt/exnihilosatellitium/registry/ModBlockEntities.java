package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.CrucibleBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.InfestedLeavesBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.SieveBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Block entity registry.
 *
 * <p>Every barrel material shares this one block entity type; the block class is
 * the same for all of them, so registration is just the list of valid blocks.</p>
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ExNihiloSatellitium.MOD_ID);

    public static final RegistryObject<BlockEntityType<BarrelBlockEntity>> BARREL =
            BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder
                    .of(BarrelBlockEntity::new, ModBlocks.BARRELS.stream()
                            .map(RegistryObject::get)
                            .toArray(Block[]::new))
                    .build(null));

    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible", () -> {
                final java.util.List<Block> valid = new java.util.ArrayList<>();
                valid.add(ModBlocks.CRUCIBLE.get());
                ModBlocks.WOODEN_CRUCIBLES.forEach(block -> valid.add(block.get()));
                return BlockEntityType.Builder
                        .of(CrucibleBlockEntity::new, valid.toArray(new Block[0]))
                        .build(null);
            });

    public static final RegistryObject<BlockEntityType<InfestedLeavesBlockEntity>> INFESTED_LEAVES =
            BLOCK_ENTITIES.register("infested_leaves", () -> BlockEntityType.Builder
                    .of(InfestedLeavesBlockEntity::new, ModBlocks.INFESTED_LEAVES.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<SieveBlockEntity>> SIEVE =
            BLOCK_ENTITIES.register("sieve", () -> BlockEntityType.Builder
                    .of(SieveBlockEntity::new, ModBlocks.SIEVE.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(final IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
