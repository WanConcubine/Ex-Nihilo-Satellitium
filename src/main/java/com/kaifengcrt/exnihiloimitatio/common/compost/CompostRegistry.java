package com.kaifengcrt.exnihiloimitatio.common.compost;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data-driven compost values.
 *
 * <p>Loaded from {@code data/<namespace>/compost/*.json} on server resource
 * reload. Values are additive: a sapling is worth 125, a leaf block 125, and
 * 1000 points fill the barrel. The scale is Ex Nihilo Omnia's 1000 mB table,
 * so 0.125 becomes 125 and the 1/6 melon/pumpkin value becomes 150.</p>
 *
 * <p>This registry is server-side only. The client uses the
 * {@code #exnihiloimitatio:compostable} item tag purely for interaction
 * prediction.</p>
 */
public final class CompostRegistry {

    public record Entry(Item item, TagKey<Item> tag, int value) {

        public boolean matches(final ItemStack stack) {
            return this.item != null ? stack.is(this.item) : stack.is(this.tag);
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private CompostRegistry() {
    }

    public static void reload(final Map<ResourceLocation, JsonElement> jsons) {
        final List<Entry> next = new ArrayList<>();

        for (final Map.Entry<ResourceLocation, JsonElement> file : jsons.entrySet()) {
            try {
                final JsonObject root = file.getValue().getAsJsonObject();
                if (!root.has("values")) {
                    continue;
                }

                for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("values").entrySet()) {
                    final String key = entry.getKey();
                    final int value = entry.getValue().getAsInt();
                    if (value <= 0) {
                        continue;
                    }

                    if (key.startsWith("#")) {
                        final ResourceLocation id = ResourceLocation.tryParse(key.substring(1));
                        if (id == null) {
                            ExNihiloImitatio.LOGGER.warn("[compost] invalid tag id '{}' in {}", key, file.getKey());
                            continue;
                        }
                        next.add(new Entry(null, TagKey.create(Registries.ITEM, id), value));
                    } else {
                        final ResourceLocation id = ResourceLocation.tryParse(key);
                        if (id == null) {
                            ExNihiloImitatio.LOGGER.warn("[compost] invalid item id '{}' in {}", key, file.getKey());
                            continue;
                        }
                        final Item item = ForgeRegistries.ITEMS.getValue(id);
                        if (item == null) {
                            ExNihiloImitatio.LOGGER.warn("[compost] unknown item {}", id);
                            continue;
                        }
                        next.add(new Entry(item, null, value));
                    }
                }
            } catch (final Exception exception) {
                ExNihiloImitatio.LOGGER.warn("[compost] failed to parse {}", file.getKey(), exception);
            }
        }

        ENTRIES.clear();
        ENTRIES.addAll(next);
        ExNihiloImitatio.LOGGER.info("[compost] loaded {} entries", ENTRIES.size());
    }

    /** @return the best matching compost value, or 0 if the stack is not compostable. */
    public static int valueOf(final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int best = 0;
        for (final Entry entry : ENTRIES) {
            if (entry.matches(stack)) {
                best = Math.max(best, entry.value());
            }
        }
        return best;
    }
}
