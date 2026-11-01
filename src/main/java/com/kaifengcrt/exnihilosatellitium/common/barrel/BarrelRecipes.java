package com.kaifengcrt.exnihilosatellitium.common.barrel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Barrel transformation table, loaded from {@code data/<namespace>/barrel/*.json}.
 *
 * <p>A recipe has a base fluid the barrel must be full of, one additive (either
 * a second fluid poured in, or an item right-clicked), and the block that
 * appears inside the barrel. The block entity keeps the "tank must be full"
 * rule, so this table only answers "base + additive -> what".</p>
 *
 * <p>Rules with an {@code entity} (dolls) spawn that entity instead of putting
 * a block in the barrel; in peaceful mode they drop {@code peaceful_drop}
 * instead, and {@code result} mirrors that drop so JEI still has something to
 * show.</p>
 */
public final class BarrelRecipes {

    /**
     * One transformation; exactly one of {@code addedFluid} / {@code addedItem} is set.
     */
    public record Recipe(Fluid base, @Nullable Fluid addedFluid, @Nullable Item addedItem, ItemStack result,
                         @Nullable ResourceLocation entity, List<Item> peacefulDrops, int cookTicks) {

        public boolean isFluidReaction() {
            return this.addedFluid != null;
        }

        public boolean isItemReaction() {
            return this.addedItem != null;
        }

        public boolean spawnsEntity() {
            return this.entity != null;
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
        return active();
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
        final Recipe recipe = ruleForItem(base, held);
        return recipe == null || recipe.spawnsEntity() ? ItemStack.EMPTY : recipe.result().copy();
    }

    /** The full rule matched by right-clicking {@code held} (may spawn an entity). */
    @Nullable
    public static Recipe ruleForItem(final Fluid base, final ItemStack held) {
        for (final Recipe recipe : active()) {
            if (recipe.addedItem() != null && recipe.base() == base && held.is(recipe.addedItem())) {
                return recipe;
            }
        }
        return null;
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
                final ResourceLocation baseId = ResourceLocation.tryParse(root.get("base").getAsString());
                final Fluid base = baseId == null ? null : ForgeRegistries.FLUIDS.getValue(baseId);
                if (base == null) {
                    ExNihiloSatellitium.LOGGER.warn("[barrel] unknown base fluid {} in {}", baseId, id);
                    continue;
                }
                final JsonObject added = root.getAsJsonObject("added");
                final boolean hasFluid = added.has("fluid");
                final boolean hasItem = added.has("item");
                if (hasFluid == hasItem) {
                    ExNihiloSatellitium.LOGGER.warn(
                            "[barrel] {} needs exactly one of added.fluid / added.item", id);
                    continue;
                }

                ResourceLocation entityId = null;
                final List<Item> peacefulDrops = new ArrayList<>();
                int cookTicks = 600;
                if (root.has("entity")) {
                    entityId = ResourceLocation.tryParse(root.get("entity").getAsString());
                    if (entityId == null || !ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] unknown entity {} in {}", entityId, id);
                        continue;
                    }
                    if (!root.has("peaceful_drop")) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] entity rule {} needs peaceful_drop", id);
                        continue;
                    }
                    final JsonElement dropElement = root.get("peaceful_drop");
                    final List<String> dropIds = new ArrayList<>();
                    if (dropElement.isJsonArray()) {
                        for (final JsonElement element : dropElement.getAsJsonArray()) {
                            dropIds.add(element.getAsString());
                        }
                    } else {
                        dropIds.add(dropElement.getAsString());
                    }
                    for (final String rawId : dropIds) {
                        final ResourceLocation dropId = ResourceLocation.tryParse(rawId);
                        final Item drop = dropId == null ? null : ForgeRegistries.ITEMS.getValue(dropId);
                        if (drop == null || drop == Items.AIR) {
                            ExNihiloSatellitium.LOGGER.warn("[barrel] unknown peaceful_drop {} in {}",
                                    dropId, id);
                            continue;
                        }
                        peacefulDrops.add(drop);
                    }
                    if (peacefulDrops.isEmpty()) {
                        continue;
                    }
                    cookTicks = root.has("cook_ticks") ? root.get("cook_ticks").getAsInt() : 600;
                }
                final ItemStack resultStack;
                if (entityId != null) {
                    resultStack = new ItemStack(peacefulDrops.get(0));
                } else {
                    final ResourceLocation resultId = ResourceLocation.tryParse(root.get("result").getAsString());
                    final Item resultItem = resultId == null ? null : ForgeRegistries.ITEMS.getValue(resultId);
                    if (resultItem == null || resultItem == Items.AIR) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] unknown result {} in {}", resultId, id);
                        continue;
                    }
                    resultStack = new ItemStack(resultItem);
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
                    final ResourceLocation fluidId = ResourceLocation.tryParse(added.get("fluid").getAsString());
                    final Fluid fluid = fluidId == null ? null : ForgeRegistries.FLUIDS.getValue(fluidId);
                    if (fluid == null) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] unknown added fluid {} in {}", fluidId, id);
                        continue;
                    }
                    built.add(new Recipe(base, fluid, null, resultStack, entityId,
                            List.copyOf(peacefulDrops), cookTicks));
                    fluidReactions++;
                } else {
                    final ResourceLocation itemId = ResourceLocation.tryParse(added.get("item").getAsString());
                    final Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
                    if (item == null) {
                        ExNihiloSatellitium.LOGGER.warn("[barrel] unknown added item {} in {}", itemId, id);
                        continue;
                    }
                    built.add(new Recipe(base, null, item, resultStack, entityId,
                            List.copyOf(peacefulDrops), cookTicks));
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
