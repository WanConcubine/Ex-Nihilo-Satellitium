package com.kaifengcrt.exnihilosatellitium.datagen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Item models for every registered item, derived from the registries instead of a
 * hand-maintained list:
 *
 * <ul>
 *   <li>block items reuse the block model as parent ({@code sieve} points at the
 *       frame, ore blocks at their shared {@code ore_<kind>_block});</li>
 *   <li>everything else is a flat {@code item/generated} sprite named after the
 *       item, except the tools ({@code item/handheld}) and the porcelain bucket
 *       (two layers + the filled predicate);</li>
 *   <li>the hand-authored geometry models ({@code electric_hammer} / {@code fuel_hammer})
 *       stay normal files; their item ids are skipped here.</li>
 * </ul>
 *
 * <p>Registered after {@link ModBlockStates} so block-item parents already exist
 * when {@code ExistingFileHelper} validates them.</p>
 */
public final class ModItemModels extends ItemModelProvider {

    /** Hand-built item geometry/icons: these keep their JSON files. */
    private static final Set<String> HAND_MODELS = Set.of("electric_hammer", "fuel_hammer", "end_cake");

    public ModItemModels(final PackOutput output, final ExistingFileHelper helper) {
        super(output, ExNihiloSatellitium.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        final Set<Block> oreBlocks = oreBlockItems();
        blockItems(oreBlocks);
        tools();
        porcelainBucket();
        flatItems();
    }

    // ----------------------------------------------------------- block items

    /** Ore block items point at one shared model per kind, not at {@code block/<name>}. */
    private Set<Block> oreBlockItems() {
        final Set<Block> covered = new HashSet<>();
        for (final OreMaterial material : OreMaterials.all()) {
            for (final String kind : ModOreBlocks.KINDS) {
                final Block block = ((BlockItem) ModOreItems.byMaterial().get(material.id())
                        .get("ore_" + kind).get()).getBlock();
                covered.add(block);
                withExistingParent(material.id() + "_ore_" + kind, modLoc("block/ore_" + kind + "_block"));
            }
        }
        return covered;
    }

    private void blockItems(final Set<Block> oreBlocks) {
        for (final RegistryObject<? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            final Block block = holder.get();
            final String name = holder.getId().getPath();
            if (oreBlocks.contains(block) || block.asItem() == Items.AIR || HAND_MODELS.contains(name)) {
                continue;
            }
            // The sieve blockstate is a multipart with no plain block model; the item
            // icon has always been the empty frame.
            final String parent = "sieve".equals(name) ? "sieve_frame" : name;
            withExistingParent(name, modLoc("block/" + parent));
        }
    }

    // --------------------------------------------------------------- special

    private void tools() {
        for (final RegistryObject<Item> hammer : ModItems.HAMMERS) {
            handheld(hammer.get());
        }
        for (final RegistryObject<Item> crook : ModItems.CROOKS) {
            handheld(crook.get());
        }
    }

    /** Forge's provider has no handheldItem(): build the same model by hand. */
    private void handheld(final Item item) {
        final String name = key(item);
        getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile(mcLoc("item/handheld")))
                .texture("layer0", modLoc("item/" + name));
    }

    /** Bucket icon, plus the filled variant shown by the {@code filled} predicate. */
    private void porcelainBucket() {
        final Item bucket = ModItems.PORCELAIN_BUCKET.get();
        final String name = key(bucket);
        basicItem(bucket)
                .override()
                .predicate(modLoc("filled"), 0.5F)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/porcelain_bucket_filled")))
                .end();
        getBuilder("porcelain_bucket_filled")
                .parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + name))
                .texture("layer1", modLoc("item/porcelain_bucket_fluid"));
    }

    // ------------------------------------------------------------ flat items

    /** Every registered non-block item that is not a tool or the bucket. */
    private void flatItems() {
        final Set<Item> special = new HashSet<>();
        special.add(ModItems.PORCELAIN_BUCKET.get());
        for (final RegistryObject<Item> holder : ModItems.HAMMERS) {
            special.add(holder.get());
        }
        for (final RegistryObject<Item> holder : ModItems.CROOKS) {
            special.add(holder.get());
        }

        for (final RegistryObject<? extends Item> holder : ModItems.ITEMS.getEntries()) {
            flat(holder.get(), special);
        }
        for (final RegistryObject<? extends Item> holder : ModOreItems.ITEMS.getEntries()) {
            flat(holder.get(), special);
        }
    }

    private void flat(final Item item, final Set<Item> special) {
        if (special.contains(item) || item instanceof BlockItem) {
            return;
        }
        basicItem(item);
    }

    // --------------------------------------------------------------- helpers

    private static String key(final Item item) {
        return ForgeRegistries.ITEMS.getKey(item).getPath();
    }
}
