package com.kaifengcrt.exnihilosatellitium.common.barrel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableBuilder;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Barrel transformation table, loaded from {@code data/<namespace>/barrel/*.json}.
 *
 * <p>A recipe has a base fluid the barrel must be full of, one additive (either
 * a second fluid poured in, or an item right-clicked), and the block that
 * appears inside the barrel. The block entity keeps the "tank must be full"
 * rule, so this table only answers "base + additive -> what".</p>
 *
 * <p>Both the base fluid and the additive may be a {@code #tag}; tags expand to
 * the current registry contents and the cross product is registered. A patch
 * file can delete a rule ({@code "remove": true}) or replace it
 * ({@code "patch": true} plus a result). Tags are bound after the reload
 * listeners run, so {@link #load} builds the concrete part and
 * {@link #rebuild(boolean)} runs again on {@code TagsUpdatedEvent}.</p>
 *
 * <p>Read by the block entity on the server and by JEI on the client (the
 * client's resource manager only exposes {@code assets/}, so processResources
 * mirrors this directory there). Script contributions from
 * {@link CustomizationOverlay} are replayed last.</p>
 */
public final class BarrelRecipes {

    /**
     * One transformation; exactly one of {@code addedFluid} / {@code addedItem} is set.
     *
     * <p>Normal rules put a block into {@code result}. Rules with an {@code entity}
     * (dolls) cook for {@code cookTicks} and then spawn that entity; in peaceful
     * mode they drop {@code peacefulDrop} instead, and {@code result} mirrors that
     * drop so JEI still has something to show.</p>
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

    private record Template(ItemStack result, @Nullable ResourceLocation entity,
                            List<Item> peacefulDrops, int cookTicks) {
    }

    private record Added(boolean fluid, String raw) {
    }

    /** Resolved base/additive cross product, used by normal files, patches and overlay ops. */
    private record Keys(boolean fluid, List<Fluid> bases, List<Fluid> addedFluids, List<Item> addedItems) {

        List<String> keys() {
            final List<String> keys = new ArrayList<>();
            for (final Fluid base : this.bases) {
                if (this.fluid) {
                    for (final Fluid added : this.addedFluids) {
                        keys.add(key(base, added, null));
                    }
                } else {
                    for (final Item added : this.addedItems) {
                        keys.add(key(base, null, added));
                    }
                }
            }
            return keys;
        }
    }

    private static final String DIR = "barrel";
    private static final int MAX_RECIPES = 4096;
    /** Gameplay table (server); replaced wholesale on every reload. */
    private static volatile List<Recipe> recipes = List.of();
    /** Client-side copy for JEI and client prediction. */
    private static volatile List<Recipe> clientRecipes = List.of();
    private static volatile List<DataPackJson.Source> sources = List.of();
    private static volatile List<DataPackJson.Source> clientSources = List.of();

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
        final Recipe recipe = ruleForItem(base, held);
        return recipe == null || recipe.spawnsEntity() ? ItemStack.EMPTY : recipe.result().copy();
    }

    /** The full rule matched by right-clicking {@code held} (may cook/spawn an entity). */
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
        final List<DataPackJson.Source> read = DataPackJson.readAll(manager, DIR, "barrel");
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
        final TableBuilder<String, Recipe> builder = new TableBuilder<>(TableId.BARREL.id());
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
                final String rawBase = DataPackJson.string(source.root(), "base");
                final Added added = parseAdded(source.root(), source.id());
                if (rawBase == null || added == null) {
                    continue;
                }
                if (DataPackJson.isTag(rawBase) || DataPackJson.isTag(added.raw())) {
                    if (tagsReady) {
                        tagFiles.add(source);
                    }
                    continue;
                }
                applyNormal(builder, source, true);
            }

            if (tagsReady) {
                for (final DataPackJson.Source source : tagFiles) {
                    applyNormal(builder, source, false);
                }
                for (final DataPackJson.Source source : patchFiles) {
                    applyPatch(builder, source);
                }
                applyOverlay(builder);
            }
        } catch (final RuntimeException exception) {
            ExNihiloSatellitium.LOGGER.error("[barrel] rebuild failed, keeping previous snapshot",
                    exception);
            return;
        }

        final List<Recipe> snapshot = List.copyOf(builder.build().values());
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
            builder.logConflicts();
        }
        final long fluidReactions = snapshot.stream().filter(Recipe::isFluidReaction).count();
        final long itemReactions = snapshot.stream().filter(Recipe::isItemReaction).count();
        ExNihiloSatellitium.LOGGER.info(
                "[barrel] loaded {} transforms ({} fluid, {} item; {} tag files, {} patches, {} overlay{}){}",
                snapshot.size(), fluidReactions, itemReactions, tagFiles.size(), patchFiles.size(),
                CustomizationOverlay.forTable(TableId.BARREL).size(),
                tagsReady ? "" : ", waiting for tags", clientView ? " (client)" : "");
    }

    private static void applyNormal(final TableBuilder<String, Recipe> builder,
                                    final DataPackJson.Source source, final boolean warnDuplicate) {
        final Keys keys = keysFor(source.root(), source.id());
        if (keys == null) {
            return;
        }
        final Template template = parseTemplate(source.root(), source.id());
        if (template == null) {
            return;
        }
        for (final Fluid base : keys.bases()) {
            if (keys.fluid()) {
                for (final Fluid fluid : keys.addedFluids()) {
                    addRecipe(builder, new Recipe(base, fluid, null, template.result().copy(),
                            template.entity(), template.peacefulDrops(), template.cookTicks()),
                            source, warnDuplicate);
                }
            } else {
                for (final Item item : keys.addedItems()) {
                    addRecipe(builder, new Recipe(base, null, item, template.result().copy(),
                            template.entity(), template.peacefulDrops(), template.cookTicks()),
                            source, warnDuplicate);
                }
            }
        }
    }

    private static void applyPatch(final TableBuilder<String, Recipe> builder,
                                   final DataPackJson.Source source) {
        final Keys keys = keysFor(source.root(), source.id());
        if (keys == null) {
            return;
        }
        final boolean removeAll = DataPackJson.removeAll(source.root());
        // Parse the replacement before touching the table: a malformed patch must
        // not delete the rule it failed to replace.
        final Template template = removeAll ? null : parseTemplate(source.root(), source.id());
        if (!removeAll && template == null) {
            return;
        }
        if (removeAll) {
            for (final String key : keys.keys()) {
                builder.remove(key, source.id().toString());
            }
            return;
        }
        for (final Fluid base : keys.bases()) {
            if (keys.fluid()) {
                for (final Fluid fluid : keys.addedFluids()) {
                    putRecipe(builder, new Recipe(base, fluid, null, template.result().copy(),
                            template.entity(), template.peacefulDrops(), template.cookTicks()), source);
                }
            } else {
                for (final Item item : keys.addedItems()) {
                    putRecipe(builder, new Recipe(base, null, item, template.result().copy(),
                            template.entity(), template.peacefulDrops(), template.cookTicks()), source);
                }
            }
        }
    }

    private static void applyOverlay(final TableBuilder<String, Recipe> builder) {
        for (final CustomizationOverlay.Customization customization
                : CustomizationOverlay.forTable(TableId.BARREL)) {
            try {
                final DataPackJson.Source source = new DataPackJson.Source(
                        overlayLabel(customization), customization.payload());
                final Keys keys = keysFor(customization.payload(), source.id());
                if (keys == null) {
                    continue;
                }
                switch (customization.action()) {
                    case ADD -> applyNormal(builder, source, false);
                    case REPLACE -> applyPatch(builder, source);
                    case REMOVE -> {
                        for (final String key : keys.keys()) {
                            builder.remove(key, source.id().toString());
                        }
                    }
                }
            } catch (final RuntimeException exception) {
                ExNihiloSatellitium.LOGGER.warn("[barrel] overlay {} failed: {}",
                        customization.describe(), exception.toString());
            }
        }
    }

    private static void addRecipe(final TableBuilder<String, Recipe> builder, final Recipe recipe,
                                  final DataPackJson.Source source, final boolean warnDuplicate) {
        if (builder.size() >= MAX_RECIPES) {
            ExNihiloSatellitium.LOGGER.warn("[barrel] recipe cap {} reached, {} skipped",
                    MAX_RECIPES, source.id());
            return;
        }
        if (!builder.putIfAbsent(key(recipe), recipe, source.id().toString()) && warnDuplicate) {
            ExNihiloSatellitium.LOGGER.warn("[barrel] duplicate rule {} + {} in {}",
                    BuiltInRegistries.FLUID.getKey(recipe.base()), addedKey(recipe), source.id());
        }
    }

    private static void putRecipe(final TableBuilder<String, Recipe> builder, final Recipe recipe,
                                  final DataPackJson.Source source) {
        if (builder.size() >= MAX_RECIPES && !builder.contains(key(recipe))) {
            ExNihiloSatellitium.LOGGER.warn("[barrel] recipe cap {} reached, {} skipped",
                    MAX_RECIPES, source.id());
            return;
        }
        builder.put(key(recipe), recipe, source.id().toString());
    }

    private static String key(final Recipe recipe) {
        return key(recipe.base(), recipe.addedFluid(), recipe.addedItem());
    }

    private static String key(final Fluid base, @Nullable final Fluid addedFluid,
                              @Nullable final Item addedItem) {
        final String baseId = BuiltInRegistries.FLUID.getKey(base).toString();
        return addedFluid != null
                ? baseId + "|fluid:" + BuiltInRegistries.FLUID.getKey(addedFluid)
                : baseId + "|item:" + BuiltInRegistries.ITEM.getKey(addedItem);
    }

    private static String addedKey(final Recipe recipe) {
        return recipe.addedFluid() != null
                ? "fluid:" + BuiltInRegistries.FLUID.getKey(recipe.addedFluid())
                : "item:" + BuiltInRegistries.ITEM.getKey(recipe.addedItem());
    }

    @Nullable
    private static Keys keysFor(final JsonObject root, final ResourceLocation file) {
        final String rawBase = DataPackJson.string(root, "base");
        final Added added = parseAdded(root, file);
        if (rawBase == null || added == null) {
            return null;
        }
        final List<Fluid> bases = sourceFluids(rawBase, file);
        final List<Fluid> addedFluids = added.fluid() ? sourceFluids(added.raw(), file) : List.of();
        final List<Item> addedItems = added.fluid()
                ? List.of()
                : DataPackJson.resolve(BuiltInRegistries.ITEM, added.raw(), "barrel", file).values()
                        .stream().filter(item -> item != Items.AIR).toList();
        if (bases.isEmpty() || (added.fluid() ? addedFluids.isEmpty() : addedItems.isEmpty())) {
            return null;
        }
        return new Keys(added.fluid(), bases, addedFluids, addedItems);
    }

    /**
     * Resolves a fluid id or tag to <em>source</em> fluids only.
     *
     * <p>Fluid tags such as {@code #minecraft:water} contain both the still and the
     * flowing variant. A barrel tank only ever holds the still fluid, so a flowing
     * rule would never match; dropping it keeps the table and {@code /exnihilo tables}
     * honest.</p>
     */
    private static List<Fluid> sourceFluids(final String raw, final ResourceLocation file) {
        final List<Fluid> resolved = DataPackJson.resolve(
                BuiltInRegistries.FLUID, raw, "barrel", file).values();
        return resolved.stream()
                .filter(fluid -> fluid.isSource(fluid.defaultFluidState()))
                .toList();
    }

    @Nullable
    private static Added parseAdded(final JsonObject root, final ResourceLocation file) {
        if (!root.has("added") || !root.get("added").isJsonObject()) {
            ExNihiloSatellitium.LOGGER.warn("[barrel] {} has no added object", file);
            return null;
        }
        final JsonObject added = root.getAsJsonObject("added");
        final boolean hasFluid = added.has("fluid");
        final boolean hasItem = added.has("item");
        if (hasFluid == hasItem) {
            ExNihiloSatellitium.LOGGER.warn(
                    "[barrel] {} needs exactly one of added.fluid / added.item", file);
            return null;
        }
        final String raw = hasFluid
                ? DataPackJson.string(added, "fluid")
                : DataPackJson.string(added, "item");
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[barrel] invalid added entry in {}", file);
            return null;
        }
        return new Added(hasFluid, raw);
    }

    @Nullable
    private static Template parseTemplate(final JsonObject root, final ResourceLocation file) {
        ResourceLocation entityId = null;
        final List<Item> peacefulDrops = new ArrayList<>();
        int cookTicks = 600;
        if (root.has("entity")) {
            final String rawEntity = DataPackJson.string(root, "entity");
            entityId = rawEntity == null ? null : DataPackJson.id(rawEntity, "barrel", file);
            if (entityId == null
                    || BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).isEmpty()) {
                ExNihiloSatellitium.LOGGER.warn("[barrel] unknown entity {} in {}", rawEntity, file);
                return null;
            }
            if (!root.has("peaceful_drop")) {
                ExNihiloSatellitium.LOGGER.warn("[barrel] entity rule {} needs peaceful_drop", file);
                return null;
            }
            final JsonElement dropElement = root.get("peaceful_drop");
            final List<String> dropIds = new ArrayList<>();
            if (dropElement.isJsonArray()) {
                for (final JsonElement element : dropElement.getAsJsonArray()) {
                    if (element.isJsonPrimitive()) {
                        dropIds.add(element.getAsString());
                    }
                }
            } else {
                dropIds.add(dropElement.getAsString());
            }
            for (final String rawId : dropIds) {
                final ResourceLocation dropId = DataPackJson.id(rawId, "barrel", file);
                final Item drop = dropId == null
                        ? null
                        : BuiltInRegistries.ITEM.getOptional(dropId).orElse(null);
                if (drop == null || drop == Items.AIR) {
                    ExNihiloSatellitium.LOGGER.warn("[barrel] unknown peaceful_drop {} in {}",
                            rawId, file);
                    continue;
                }
                peacefulDrops.add(drop);
            }
            if (peacefulDrops.isEmpty()) {
                return null;
            }
            cookTicks = root.has("cook_ticks") ? root.get("cook_ticks").getAsInt() : 600;
        }
        final ItemStack resultStack;
        if (entityId != null) {
            resultStack = new ItemStack(peacefulDrops.get(0));
        } else {
            final String rawResult = DataPackJson.string(root, "result");
            if (rawResult == null) {
                ExNihiloSatellitium.LOGGER.warn("[barrel] {} has no result", file);
                return null;
            }
            final ResourceLocation resultId = DataPackJson.id(rawResult, "barrel", file);
            final Item result = resultId == null
                    ? null
                    : BuiltInRegistries.ITEM.getOptional(resultId).orElse(null);
            if (result == null || result == Items.AIR) {
                ExNihiloSatellitium.LOGGER.warn("[barrel] unknown result {} in {}", rawResult, file);
                return null;
            }
            resultStack = new ItemStack(result);
        }
        return new Template(resultStack, entityId, List.copyOf(peacefulDrops), cookTicks);
    }

    private static ResourceLocation overlayLabel(final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = ResourceLocation.tryParse(
                "customize:barrel/" + customization.sequence());
        return label == null ? ResourceLocation.tryParse("customize:barrel/0") : label;
    }
}
