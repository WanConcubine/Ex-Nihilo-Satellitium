package com.kaifengcrt.exnihilosatellitium.compat.tconstruct;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.List;

/**
 * Registration entry point for the optional Tinkers' Construct hammer.
 *
 * <p>This class lives in the {@code tinkers} source set, which is only compiled
 * when the TConstruct and Mantle jars are present in {@code libs/}. Core code
 * reaches it through reflection, so a build without Tinkers never loads a TiC
 * class, and a player without Tinkers simply has no such item.</p>
 *
 * <p>The tool definition is created here (before the item registers) because
 * TiC's datapack loader only reads {@code tinkering/tool_definitions/<id>.json}
 * for definitions that were registered in code. The matching JSON lives in
 * {@code data/exnihilosatellitium/tinkering/tool_definitions/ex_nihilo_hammer.json}.</p>
 */
public final class TinkersTools {

    /**
     * Tool definition id; also the item id and the station-layout id. The
     * building recipe id follows its file path
     * ({@code exnihilosatellitium:tools/building/ex_nihilo_hammer}).
     */
    public static final ResourceLocation ID =
            new ResourceLocation(ExNihiloSatellitium.MOD_ID, "ex_nihilo_hammer");

    private static final ToolDefinition DEFINITION = ToolDefinition.create(ID);

    private static RegistryObject<Item> hammer;

    private TinkersTools() {
    }

    /** Registers the TiC hammer and returns it for the shared hammer list. */
    public static List<RegistryObject<Item>> register(final DeferredRegister<Item> items) {
        hammer = items.register(ID.getPath(), () -> new ExNihiloHammerItem(
                new Item.Properties().stacksTo(1), DEFINITION));
        return List.of(hammer);
    }

    /** Client-only colour registration; only called when Tinkers is present. */
    public static void registerClient(final net.minecraftforge.eventbus.api.IEventBus modBus) {
        TinkersClient.register(modBus);
    }

    /** The registered item, or {@code null} before registration / without Tinkers. */
    public static Item item() {
        return hammer == null ? null : hammer.get();
    }

    /**
     * Creative-tab entries. A bare TiC tool has no materials and would show a
     * blank/none durability bar, so let TiC build the same random-material
     * variants it uses in its own tool tab.
     */
    public static void addCreativeItems(final CreativeModeTab.Output output) {
        if (hammer == null) {
            return;
        }
        ToolBuildHandler.addVariants(output::accept, (IModifiable) hammer.get(), "");
    }
}
