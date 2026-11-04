package com.kaifengcrt.exnihilosatellitium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Average colour of an item's icon, used to tint the barrel compost pile.
 *
 * <p>The sprite is sampled once per item and cached against the sprite
 * instance, so a resource reload (which replaces the sprite) invalidates the
 * entry automatically. Block items use their block particle sprite, plain
 * items their baked item model.</p>
 */
final class ItemTint {

    /** Normalised colour (the brightest channel is 1.0), so dark icons still tint. */
    record Tint(float red, float green, float blue) {
        static final Tint WHITE = new Tint(1.0F, 1.0F, 1.0F);
    }

    private record Cached(TextureAtlasSprite sprite, Tint tint) {
    }

    private static final Map<Item, Cached> CACHE = new HashMap<>();

    private ItemTint() {
    }

    static Tint of(final Item item) {
        final TextureAtlasSprite sprite = sprite(item);
        if (sprite == null) {
            return Tint.WHITE;
        }
        final Cached cached = CACHE.get(item);
        if (cached != null && cached.sprite() == sprite) {
            return cached.tint();
        }
        final Tint tint = average(sprite, item);
        CACHE.put(item, new Cached(sprite, tint));
        return tint;
    }

    @Nullable
    private static TextureAtlasSprite sprite(final Item item) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (item instanceof BlockItem blockItem) {
            return minecraft.getBlockRenderer().getBlockModelShaper()
                    .getParticleIcon(blockItem.getBlock().defaultBlockState());
        }
        final BakedModel model = minecraft.getItemRenderer()
                .getModel(new ItemStack(item), null, null, 0);
        return model == null ? null : model.getParticleIcon();
    }

    private static Tint average(final TextureAtlasSprite sprite, final Item item) {
        final int width = Math.max(1, sprite.contents().width());
        final int height = Math.max(1, sprite.contents().height());
        double red = 0.0D;
        double green = 0.0D;
        double blue = 0.0D;
        double weight = 0.0D;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // NativeImage stores RGBA bytes little-endian: R is the low byte.
                final int pixel = sprite.getPixelRGBA(0, x, y);
                final int alpha = pixel >>> 24;
                if (alpha < 32) {
                    continue;
                }
                red += (pixel & 0xFF) * alpha;
                green += ((pixel >>> 8) & 0xFF) * alpha;
                blue += ((pixel >>> 16) & 0xFF) * alpha;
                weight += alpha;
            }
        }
        if (weight <= 0.0D) {
            return Tint.WHITE;
        }
        float r = (float) (red / weight);
        float g = (float) (green / weight);
        float b = (float) (blue / weight);
        // Vanilla leaves/grass textures are grey and get their colour from the
        // item colour handler (biome foliage/grass colour); apply it here.
        final int tint = Minecraft.getInstance().getItemColors().getColor(new ItemStack(item), 0);
        if (tint != -1) {
            r *= ((tint >> 16) & 0xFF) / 255.0F;
            g *= ((tint >> 8) & 0xFF) / 255.0F;
            b *= (tint & 0xFF) / 255.0F;
        }
        final float max = Math.max(r, Math.max(g, b));
        if (max <= 0.0F) {
            return Tint.WHITE;
        }
        r /= max;
        g /= max;
        b /= max;
        return new Tint(r, g, b);
    }
}
