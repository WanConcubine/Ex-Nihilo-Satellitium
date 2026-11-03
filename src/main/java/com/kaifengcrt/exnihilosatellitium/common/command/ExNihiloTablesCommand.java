package com.kaifengcrt.exnihilosatellitium.common.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableDump;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import com.kaifengcrt.exnihilosatellitium.common.table.Tables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Diagnostics and (until KubeJS / CraftTweaker land) the programmatic overlay
 * entry point:
 *
 * <ul>
 *   <li>{@code /exnihilo tables} — effective table sizes after the last reload;</li>
 *   <li>{@code /exnihilo dump <table>} — sorted, diffable lines for one table;</li>
 *   <li>{@code /exnihilo customize <table> add|remove|replace <json>} — queue an
 *       overlay operation; the payload uses the same JSON shape as a datapack file;</li>
 *   <li>{@code /exnihilo customize list} — queued operations;</li>
 *   <li>{@code /exnihilo customize clear [source]} — drop overlay operations;</li>
 *   <li>{@code /exnihilo sieve <item>} / {@code hammer <block>} / {@code compost <item>}
 *       — single-entry lookups.</li>
 * </ul>
 */
public final class ExNihiloTablesCommand {

    private static final SuggestionProvider<CommandSourceStack> TABLES = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    List.of(TableId.values()).stream().map(TableId::id).toList(), builder);

    private ExNihiloTablesCommand() {
    }

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exnihilo")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("tables").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(summary()), false);
                    return 1;
                }))
                .then(Commands.literal("dump")
                        .then(Commands.argument("table", StringArgumentType.word())
                                .suggests(TABLES)
                                .executes(context -> {
                                    final TableId table = TableId.byId(
                                            StringArgumentType.getString(context, "table")).orElse(null);
                                    if (table == null) {
                                        context.getSource().sendFailure(
                                                Component.literal("unknown table"));
                                        return 0;
                                    }
                                    return dump(context.getSource(), table);
                                })))
                .then(Commands.literal("customize")
                        .then(Commands.literal("list").executes(context -> {
                            list(context.getSource());
                            return 1;
                        }))
                        .then(Commands.literal("clear")
                                .executes(context -> clear(context.getSource(), null))
                                .then(Commands.argument("source", StringArgumentType.word())
                                        .executes(context -> clear(context.getSource(),
                                                StringArgumentType.getString(context, "source")))))
                        .then(Commands.argument("table", StringArgumentType.word())
                                .suggests(TABLES)
                                .then(Commands.literal("add")
                                        .then(Commands.argument("json", StringArgumentType.greedyString())
                                                .executes(context -> customize(context,
                                                        CustomizationOverlay.Action.ADD))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("json", StringArgumentType.greedyString())
                                                .executes(context -> customize(context,
                                                        CustomizationOverlay.Action.REMOVE))))
                                .then(Commands.literal("replace")
                                        .then(Commands.argument("json", StringArgumentType.greedyString())
                                                .executes(context -> customize(context,
                                                        CustomizationOverlay.Action.REPLACE))))))
                .then(Commands.literal("sieve")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(context -> {
                                    final CommandSourceStack source = context.getSource();
                                    final ResourceLocation id = ResourceLocationArgument.getId(context, "item");
                                    final Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                                    if (item == null) {
                                        source.sendFailure(Component.literal("unknown item " + id));
                                        return 0;
                                    }
                                    final List<SieveDrop> drops = SieveRegistry.dropsFor(new ItemStack(item));
                                    if (drops.isEmpty()) {
                                        source.sendSuccess(() -> Component.literal(id + ": no sieve drops"), false);
                                        return 0;
                                    }
                                    final StringJoiner joiner = new StringJoiner(", ");
                                    for (final SieveDrop drop : drops) {
                                        joiner.add(BuiltInRegistries.ITEM.getKey(drop.item())
                                                + " " + drop.chance()
                                                + (drop.min() == drop.max()
                                                    ? " x" + drop.min()
                                                    : " x" + drop.min() + "-" + drop.max()));
                                    }
                                    source.sendSuccess(() -> Component.literal(
                                            id + ": " + drops.size() + " drops — " + joiner), false);
                                    return drops.size();
                                })))
                .then(Commands.literal("compost")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(context -> {
                                    final CommandSourceStack source = context.getSource();
                                    final ResourceLocation id = ResourceLocationArgument.getId(context, "item");
                                    final Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                                    if (item == null) {
                                        source.sendFailure(Component.literal("unknown item " + id));
                                        return 0;
                                    }
                                    final int value = CompostRegistry.valueOf(new ItemStack(item));
                                    source.sendSuccess(() -> Component.literal(id + ": compost " + value), false);
                                    return value;
                                })))
                .then(Commands.literal("hammer")
                        .then(Commands.argument("block", ResourceLocationArgument.id())
                                .executes(context -> {
                                    final CommandSourceStack source = context.getSource();
                                    final ResourceLocation id = ResourceLocationArgument.getId(context, "block");
                                    final var block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
                                    if (block == null) {
                                        source.sendFailure(Component.literal("unknown block " + id));
                                        return 0;
                                    }
                                    final HammerRecipes.Rule rule = HammerRecipes.ruleFor(block.defaultBlockState());
                                    if (rule == null) {
                                        source.sendSuccess(() -> Component.literal(id + ": no hammer rule"), false);
                                        return 0;
                                    }
                                    source.sendSuccess(() -> Component.literal(id + ": "
                                            + BuiltInRegistries.ITEM.getKey(rule.item())
                                            + " x" + rule.min() + "-" + rule.max()), false);
                                    return 1;
                                }))));
    }

    private static int dump(final CommandSourceStack source, final TableId table) {
        final List<String> lines = TableDump.lines(table);
        source.sendSuccess(() -> Component.literal(TableDump.header(table, lines.size())), false);
        for (final String line : lines) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return lines.size();
    }

    private static int customize(final CommandContext<CommandSourceStack> context,
                                 final CustomizationOverlay.Action action) {
        final CommandSourceStack source = context.getSource();
        final TableId table = TableId.byId(StringArgumentType.getString(context, "table")).orElse(null);
        if (table == null) {
            source.sendFailure(Component.literal("unknown table"));
            return 0;
        }
        final JsonObject payload = parseJson(source, StringArgumentType.getString(context, "json"));
        if (payload == null) {
            return 0;
        }
        final int before = TableDump.lines(table).size();
        CustomizationOverlay.submit(table, action, payload, "command");
        Tables.rebuild(table, false);
        final int after = TableDump.lines(table).size();
        source.sendSuccess(() -> Component.literal(
                table.id() + " " + action.name().toLowerCase(java.util.Locale.ROOT)
                        + ": " + before + " -> " + after + " rules, overlay "
                        + CustomizationOverlay.forTable(table).size() + " ops"), false);
        return after;
    }

    private static JsonObject parseJson(final CommandSourceStack source, final String raw) {
        try {
            final JsonElement element = JsonParser.parseString(raw);
            if (!element.isJsonObject()) {
                source.sendFailure(Component.literal("payload must be a JSON object"));
                return null;
            }
            return element.getAsJsonObject();
        } catch (final RuntimeException exception) {
            source.sendFailure(Component.literal("invalid JSON: " + exception.getMessage()));
            return null;
        }
    }

    private static int clear(final CommandSourceStack source, final String origin) {
        final int dropped = origin == null
                ? CustomizationOverlay.clearAll()
                : CustomizationOverlay.clear(origin);
        Tables.rebuildAll(false);
        source.sendSuccess(() -> Component.literal("cleared " + dropped + " overlay op(s)"
                + (origin == null ? "" : " from " + origin)), false);
        return dropped;
    }

    private static void list(final CommandSourceStack source) {
        final List<CustomizationOverlay.Customization> operations = new ArrayList<>(
                CustomizationOverlay.all());
        if (operations.isEmpty()) {
            source.sendSuccess(() -> Component.literal("no overlay ops"), false);
            return;
        }
        for (final CustomizationOverlay.Customization customization : operations) {
            source.sendSuccess(() -> Component.literal(
                    "#" + customization.sequence() + " [" + customization.source() + "] "
                            + customization.describe()), false);
        }
    }

    private static String summary() {
        final int sieveInputs = SieveRegistry.all().size();
        final int sieveDrops = SieveRegistry.all().values().stream().mapToInt(List::size).sum();
        return "Ex Nihilo tables: sieve " + sieveInputs + " inputs / " + sieveDrops + " drops"
                + "; hammer " + HammerRecipes.all().size()
                + "; crook " + CrookDrops.all().size()
                + "; barrel " + BarrelRecipes.all().size()
                + "; compost " + CompostRegistry.all().size()
                + "; crucible " + CrucibleRecipes.all().size() + " melt / "
                + CrucibleHeat.all().size() + " heat"
                + "; overlay " + CustomizationOverlay.size() + " ops";
    }
}
