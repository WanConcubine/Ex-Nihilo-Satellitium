package com.kaifengcrt.exnihilosatellitium.common.crucible;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
 * {@code TagsUpdatedEvent}.</p>
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
        rebuild(clientView);
    }

    /** Rebuilds from the last read sources; called again after tags are bound. */
    public static void rebuild(final boolean clientView) {
        final List<DataPackJson.Source> sourceList = clientView ? clientSources : sources;
        final boolean tagsReady = DataPackJson.tagsReady();
        final Map<Block, Melt> built = new HashMap<>();
        final List<DataPackJson.Source> tagFiles = new ArrayList<>();
        final List<DataPackJson.Source> patchFiles = new ArrayList<>();

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
            final List<Block> inputs = blocks(rawInput, source.id());
            final Template template = parseTemplate(source.root(), source.id());
            if (template == null) {
                continue;
            }
            for (final Block input : inputs) {
                if (built.putIfAbsent(input, template.toMelt(input)) != null) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] duplicate input {} in {}, skipped",
                            BuiltInRegistries.BLOCK.getKey(input), source.id());
                }
            }
        }

        if (tagsReady) {
            for (final DataPackJson.Source source : tagFiles) {
                final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
                final Template template = parseTemplate(source.root(), source.id());
                if (template == null) {
                    continue;
                }
                for (final Block input : inputs) {
                    built.putIfAbsent(input, template.toMelt(input));
                }
            }

            for (final DataPackJson.Source source : patchFiles) {
                final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
                if (DataPackJson.removeAll(source.root())) {
                    for (final Block input : inputs) {
                        built.remove(input);
                    }
                    continue;
                }
                final Template template = parseTemplate(source.root(), source.id());
                if (template == null) {
                    continue;
                }
                for (final Block input : inputs) {
                    built.put(input, template.toMelt(input));
                }
            }
        }

        final Map<Block, Melt> snapshot = Map.copyOf(built);
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[crucible] loaded {} meltables ({} tag files, {} patches{}){}",
                built.size(), tagFiles.size(), patchFiles.size(),
                tagsReady ? "" : ", tags not ready", clientView ? " (client)" : "");
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
