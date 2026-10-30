package com.kaifengcrt.exnihilosatellitium.common.barrel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;

/**
 * Barrel transformation table, loaded from {@code data/<namespace>/barrel/*.json}.
 *
 * <p>A recipe has a base fluid the barrel must be full of, one additive (either
 * a second fluid poured in, or an item right-clicked), and the block that
 * appears inside the barrel. The block entity keeps the "tank must be full"
 * rule, so this table only answers "base + additive -> what".</p>
 *
 * <p>Read by the block entity on the server and by JEI on the client (the
 * client's resource manager only exposes {@code assets/}, so processResources
 * mirrors this directory there).</p>
 */
public final class BarrelRecipes {

    /** One transformation; exactly one of {@code addedFluid} / {@code addedItem} is set. */
    public record Recipe(Fluid base, @Nullable Fluid addedFluid, @Nullable Item addedItem, ItemStack result) {

        public boolean isFluidReaction() {
            return this.addedFluid != null;
        }

        public boolean isItemReaction() {
            return this.addedItem != null;
        }
    }

    private static final String DIR = "barrel";
    /** Gameplay table (server); replaced wholesale on every reload. */
    private static volatile List<Recipe> recipes = List.of();
    /** Client-side copy for JEI and client prediction. */
    private static volatile List<Recipe> clientRecipes = List.of();

    private BarrelRecipes() {
    }

    public static List<Recipe> all() {
        return recipes.isEmpty() ? clientRecipes : recipes;
    }

    /** The block produced by pouring {@code incoming} into a full barrel of {@code base}. */
    public static ItemStack resultForFluid(final Fluid base, final Fluid incoming) {
        for (final Recipe recipe : active()) {
            if (recipe.addedFluid() == incoming && recipe.base() == base) {
                return recipe.result().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /** The block produced by right-clicking a full barrel of {@code base} with {@code held}. */
    public static ItemStack resultForItem(final Fluid base, final ItemStack held) {
        for (final Recipe recipe : active()) {
            if (recipe.addedItem() != null && recipe.base() == base && held.is(recipe.addedItem())) {
                return recipe.result().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /** Server gameplay first; a dedicated client falls back to its built-in copy. */
    private static List<Recipe> active() {
        return recipes.isEmpty() ? clientRecipes : recipes;
    }

    public static void load(final ResourceManager manager, final boolean clientView) {
        final List<Recipe> built = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        int files = 0;
        int fluidReactions = 0;
        int itemReactions = 0;
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(DIR, path -> path.getPath().endsWith(".json")).entrySet()) {
            final ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                final ResourceLocation baseId = ResourceLocation.parse(root.get("base").getAsString());
                final Optional<Fluid> baseOptional = BuiltInRegistries.FLUID.getOptional(baseId);
                if (baseOptional.isEmpty()) {
                    ExNihiloSatellitium.LOGGER.warn("[barrel] unknown base fluid {} in {}", baseId, id);
                    continue;
                }
                final Fluid base = baseOptional.get();
                final JsonObject added = root.getAsJsonObject("added");
                final boolean hasFluid = added.has("fluid");
                final boolean hasItem = added.has("item");
                if (hasFluid == hasItem) {
                    ExNihiloSatellitium.LOGGER.warn(
                            "[barrel] {} needs exactly one of added.fluid / added.item", id);
                    continue;
                }

                final ResourceLocation resultId = ResourceLocation.parse(root.get("result").getAsString());
                final Optional<Item> resultOptional = BuiltInRegistries.ITEM.getOptional(resultId);
                if (resultOptional.isEmpty()) {
                    ExNihiloSatellitium.LOGGER.warn("[barrel] unknown result {} in {}", resultId, id);
                    continue;
                }
                final Item resultItem = resultOptional.get();
                if (resultItem == Items.AIR) {
                    ExNihiloSatellitium.LOGGER.warn("[barrel] air result in {}", id);
                    continue;
                }
                final String addedKey = hasFluid
                        ? "fluid:" + added.get("fluid").getAsString()
                        : "item:" + added.get("item").getAsString();
                if (!seen.add(baseId + "|" + addedKey)) {
                    ExNihiloSatellitium.LOGGER.warn("[barrel] duplicate rule {} + {} in {}",
                            baseId, addedKey, id);
                    continue;
                }

                if (hasFluid) {
                    final ResourceLocation fluidId = ResourceLocation.parse(added.get("fluid").getAsString());
                    final Optional<Fluid> fluidOptional = BuiltInRegistries.FLUID.getOptional(fluidId);
                    if (fluidOptional.isEmpty()) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] unknown added fluid {} in {}", fluidId, id);
                        continue;
                    }
                    final Fluid fluid = fluidOptional.get();
                    built.add(new Recipe(base, fluid, null, new ItemStack(resultItem)));
                    fluidReactions++;
                } else {
                    final ResourceLocation itemId = ResourceLocation.parse(added.get("item").getAsString());
                    final Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(itemId);
                    if (itemOptional.isEmpty()) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] unknown added item {} in {}", itemId, id);
                        continue;
                    }
                    final Item item = itemOptional.get();
                    built.add(new Recipe(base, null, item, new ItemStack(resultItem)));
                    itemReactions++;
                }
                files++;
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[barrel] failed to read {}: {}", id, exception.toString());
            }
        }
        final List<Recipe> snapshot = List.copyOf(built);
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[barrel] loaded {} transforms ({} fluid, {} item){}",
                files, fluidReactions, itemReactions, clientView ? " (client)" : "");
    }
}
