package com.kaifengcrt.exnihilosatellitium.common.crucible;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableBuilder;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Crucible melt table, loaded from {@code data/<namespace>/crucible_melt/*.json}.
 *
 * <pre>
 * {
 *   "input": "minecraft:cobblestone",
 *   "fluid": "minecraft:lava",
 *   "amount": 250,
 *   "solid": 2000
 * }
 * </pre>
 *
 * <p>{@code amount} is the mB produced by one block; {@code solid} is the
 * classic solid-volume bookkeeping value (only used for documentation and
 * validation, the tank only tracks mB).</p>
 *
 * <p>One melt rule per block: a {@code #tag} input expands to every matching
 * block but never overwrites a block that already has a concrete rule. Use
 * {@code "patch": true} to replace a rule and {@code "remove": true} to delete
 * one. Tags are bound after the reload listeners run, so {@link #load} builds
 * the concrete part and {@link #rebuild(boolean)} runs again on
 * {@code TagsUpdatedEvent}. Script contributions from
 * {@link CustomizationOverlay} are replayed last.</p>
 */
public final class CrucibleRecipes {

    private static final String DIR = "crucible_melt";
    private static final int MAX_AMOUNT = 64000;

    public record Melt(Block input, Fluid fluid, int amount, int solidAmount) {
    }

    private static volatile Map<Block, Melt> recipes = Map.of();
    private static volatile Map<Block, Melt> clientRecipes = Map.of();
    private static volatile List<DataPackJson.Source> sources = List.of();
    private static volatile List<DataPackJson.Source> clientSources = List.of();

    private CrucibleRecipes() {
    }

    /** Gameplay lookup: always the server table, so client reloads cannot change behaviour. */
    public static Melt meltFor(final Block block) {
        return recipes.get(block);
    }

    /** JEI view: client copy when present, otherwise the server table. */
    public static Collection<Melt> all() {
        return activeClientOrServer().values();
    }

    private static Map<Block, Melt> activeClientOrServer() {
        return recipes.isEmpty() ? clientRecipes : recipes;
    }

    public static void load(final ResourceManager manager, final boolean clientView) {
        final List<DataPackJson.Source> read = DataPackJson.readAll(manager, DIR, "crucible");
        if (clientView) {
            clientSources = read;
        } else {
            sources = read;
        }
        // Concrete entries are available immediately; tags are expanded on TagsUpdatedEvent.
        // The client keeps the old bindings across F3+T, so it can expand right away.
        rebuild(clientView, clientView);
    }

    /** Rebuilds from the last read sources; called again after tags are bound. */
    public static void rebuild(final boolean clientView) {
        rebuild(clientView, true);
    }

    private static void rebuild(final boolean clientView, final boolean expandTags) {
        final List<DataPackJson.Source> sourceList = clientView ? clientSources : sources;
        final boolean tagsReady = expandTags && DataPackJson.tagsReady();
        final TableBuilder<Block, Melt> builder = new TableBuilder<>(TableId.CRUCIBLE_MELT.id(),
                block -> BuiltInRegistries.BLOCK.getKey(block).toString());
        final List<DataPackJson.Source> tagFiles = new ArrayList<>();
        final List<DataPackJson.Source> patchFiles = new ArrayList<>();

        try {
            for (final DataPackJson.Source source : sourceList) {
                if (!DataPackJson.modLoaded(source.root())) {
                    continue;
                }
                if (DataPackJson.isPatch(source.root())) {
                    patchFiles.add(source);
                    continue;
                }
                final String rawInput = DataPackJson.string(source.root(), "input");
                if (rawInput == null) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] missing input in {}", source.id());
                    continue;
                }
                if (DataPackJson.isTag(rawInput)) {
                    if (tagsReady) {
                        tagFiles.add(source);
                    }
                    continue;
                }
                applyFile(builder, source);
            }

            if (tagsReady) {
                for (final DataPackJson.Source source : tagFiles) {
                    applyTagFile(builder, source);
                }
                for (final DataPackJson.Source source : patchFiles) {
                    applyPatch(builder, source);
                }
                applyOverlay(builder);
            }
        } catch (final RuntimeException exception) {
            ExNihiloSatellitium.LOGGER.error("[crucible] rebuild failed, keeping previous snapshot",
                    exception);
            return;
        }

        final Map<Block, Melt> snapshot = builder.build();
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
            builder.logConflicts();
        }
        ExNihiloSatellitium.LOGGER.info(
                "[crucible] loaded {} meltables ({} tag files, {} patches, {} overlay{}){}",
                snapshot.size(), tagFiles.size(), patchFiles.size(),
                CustomizationOverlay.forTable(TableId.CRUCIBLE_MELT).size(),
                tagsReady ? "" : ", waiting for tags", clientView ? " (client)" : "");
    }

    private static void applyFile(final TableBuilder<Block, Melt> builder,
                                  final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
        final Template template = parseTemplate(source.root(), source.id());
        if (template == null) {
            return;
        }
        for (final Block input : inputs) {
            if (!builder.putIfAbsent(input, template.toMelt(input), source.id().toString())) {
                ExNihiloSatellitium.LOGGER.warn("[crucible] duplicate input {} in {}, skipped",
                        BuiltInRegistries.BLOCK.getKey(input), source.id());
            }
        }
    }

    private static void applyTagFile(final TableBuilder<Block, Melt> builder,
                                     final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
        final Template template = parseTemplate(source.root(), source.id());
        if (template == null) {
            return;
        }
        for (final Block input : inputs) {
            builder.putIfAbsent(input, template.toMelt(input), source.id().toString());
        }
    }

    private static void applyPatch(final TableBuilder<Block, Melt> builder,
                                   final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
        if (inputs.isEmpty()) {
            return;
        }
        if (DataPackJson.removeAll(source.root())) {
            for (final Block input : inputs) {
                builder.remove(input);
            }
            return;
        }
        final Template template = parseTemplate(source.root(), source.id());
        if (template == null) {
            return;
        }
        for (final Block input : inputs) {
            builder.put(input, template.toMelt(input), source.id().toString());
        }
    }

    private static void applyOverlay(final TableBuilder<Block, Melt> builder) {
        for (final CustomizationOverlay.Customization customization
                : CustomizationOverlay.forTable(TableId.CRUCIBLE_MELT)) {
            try {
                final ResourceLocation label = ResourceLocation.tryParse(
                        "customize:crucible_melt/" + customization.sequence());
                final JsonObject root = customization.payload();
                final List<Block> inputs = blocks(DataPackJson.string(root, "input"), label);
                if (inputs.isEmpty()) {
                    continue;
                }
                if (customization.action() == CustomizationOverlay.Action.REMOVE) {
                    for (final Block input : inputs) {
                        builder.remove(input, overlaySource(customization));
                    }
                    continue;
                }
                final Template template = parseTemplate(root, label);
                if (template == null) {
                    continue;
                }
                final String source = overlaySource(customization);
                if (customization.action() == CustomizationOverlay.Action.ADD) {
                    for (final Block input : inputs) {
                        builder.putIfAbsent(input, template.toMelt(input), source);
                    }
                } else {
                    for (final Block input : inputs) {
                        builder.put(input, template.toMelt(input), source);
                    }
                }
            } catch (final RuntimeException exception) {
                ExNihiloSatellitium.LOGGER.warn("[crucible] overlay {} failed: {}",
                        customization.describe(), exception.toString());
            }
        }
    }

    private static String overlaySource(final CustomizationOverlay.Customization customization) {
        return "customize:" + customization.source() + "#" + customization.sequence();
    }

    private static List<Block> blocks(final String raw, final ResourceLocation file) {
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] missing input in {}", file);
            return List.of();
        }
        return DataPackJson.resolve(BuiltInRegistries.BLOCK, raw, "crucible", file).values();
    }

    private record Template(Fluid fluid, int amount, int solid) {

        Melt toMelt(final Block input) {
            return new Melt(input, this.fluid, this.amount, this.solid);
        }
    }

    private static Template parseTemplate(final JsonObject root, final ResourceLocation file) {
        final String rawFluid = DataPackJson.string(root, "fluid");
        if (rawFluid == null) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] {} has no fluid", file);
            return null;
        }
        final ResourceLocation fluidId = DataPackJson.id(rawFluid, "crucible", file);
        final Fluid fluid = fluidId == null
                ? null
                : BuiltInRegistries.FLUID.getOptional(fluidId).orElse(null);
        if (fluid == null) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] unknown fluid {} in {}", rawFluid, file);
            return null;
        }
        final int amount = root.has("amount") ? root.get("amount").getAsInt() : 0;
        final int solid = root.has("solid") ? root.get("solid").getAsInt() : amount;
        if (amount < 1 || amount > MAX_AMOUNT || solid < 1 || solid > MAX_AMOUNT) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] invalid amounts in {} (amount {}, solid {})",
                    file, amount, solid);
            return null;
        }
        return new Template(fluid, amount, solid);
    }
}
