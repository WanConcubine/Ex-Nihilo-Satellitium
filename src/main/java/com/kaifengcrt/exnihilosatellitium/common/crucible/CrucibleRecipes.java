package com.kaifengcrt.exnihilosatellitium.common.crucible;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
 */
public final class CrucibleRecipes {

    private static final String DIR = "crucible_melt";
    private static final int MAX_AMOUNT = 64000;

    public record Melt(Block input, Fluid fluid, int amount, int solidAmount) {
    }

    private static volatile Map<Block, Melt> recipes = Map.of();
    private static volatile Map<Block, Melt> clientRecipes = Map.of();

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
        final Map<Block, Melt> built = new HashMap<>();
        int files = 0;
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(DIR, path -> path.getPath().endsWith(".json")).entrySet()) {
            final ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                final ResourceLocation inputId = ResourceLocation.parse(root.get("input").getAsString());
                final Optional<Block> input = BuiltInRegistries.BLOCK.getOptional(inputId);
                if (input.isEmpty()) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] unknown input block {} in {}", inputId, id);
                    continue;
                }
                final ResourceLocation fluidId = ResourceLocation.parse(root.get("fluid").getAsString());
                final Optional<Fluid> fluid = BuiltInRegistries.FLUID.getOptional(fluidId);
                if (fluid.isEmpty()) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] unknown fluid {} in {}", fluidId, id);
                    continue;
                }
                final int amount = root.get("amount").getAsInt();
                final int solid = root.has("solid") ? root.get("solid").getAsInt() : amount;
                if (amount < 1 || amount > MAX_AMOUNT || solid < 1 || solid > MAX_AMOUNT) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] invalid amounts in {} (amount {}, solid {})",
                            id, amount, solid);
                    continue;
                }
                if (built.putIfAbsent(input.get(), new Melt(input.get(), fluid.get(), amount, solid)) != null) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] duplicate input {} in {}, skipped", inputId, id);
                    continue;
                }
                files++;
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[crucible] failed to read {}: {}", id, exception.toString());
            }
        }
        final Map<Block, Melt> snapshot = Map.copyOf(built);
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[crucible] loaded {} meltables{}", files,
                clientView ? " (client)" : "");
    }
}
