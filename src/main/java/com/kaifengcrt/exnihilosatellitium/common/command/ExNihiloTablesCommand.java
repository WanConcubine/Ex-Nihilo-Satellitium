package com.kaifengcrt.exnihilosatellitium.common.command;

import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.StringJoiner;

/**
 * Diagnostics for pack authors:
 *
 * <ul>
 *   <li>{@code /exnihilo tables} — effective table sizes after the last reload;</li>
 *   <li>{@code /exnihilo sieve <item>} — the drops currently registered for one input;</li>
 *   <li>{@code /exnihilo hammer <block>} — the hammer rule currently registered for one block.</li>
 * </ul>
 */
public final class ExNihiloTablesCommand {

    private ExNihiloTablesCommand() {
    }

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exnihilo")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("tables").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(summary()), false);
                    return 1;
                }))
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

    private static String summary() {
        final int sieveInputs = SieveRegistry.all().size();
        final int sieveDrops = SieveRegistry.all().values().stream().mapToInt(List::size).sum();
        return "Ex Nihilo tables: sieve " + sieveInputs + " inputs / " + sieveDrops + " drops"
                + "; hammer " + HammerRecipes.all().size()
                + "; crook " + CrookDrops.all().size()
                + "; barrel " + BarrelRecipes.all().size()
                + "; compost " + CompostRegistry.all().size()
                + "; crucible " + CrucibleRecipes.all().size() + " melt / "
                + CrucibleHeat.all().size() + " heat";
    }
}
