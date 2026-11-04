package com.kaifengcrt.exnihilosatellitium.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Multiplies every vertex colour by a tint. Used when a block model (the
 * compost dirt) is drawn inside a block entity renderer with a colour derived
 * from the items that went into it.
 */
final class TintVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final float red;
    private final float green;
    private final float blue;

    private TintVertexConsumer(final VertexConsumer delegate, final float red,
                               final float green, final float blue) {
        this.delegate = delegate;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    static MultiBufferSource buffer(final MultiBufferSource source,
                                    final float red, final float green, final float blue) {
        return type -> new TintVertexConsumer(source.getBuffer(type), red, green, blue);
    }

    @Override
    public VertexConsumer addVertex(final float x, final float y, final float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
        this.delegate.setColor(
                Math.round(red * this.red),
                Math.round(green * this.green),
                Math.round(blue * this.blue),
                alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(final float u, final float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(final int u, final int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(final int u, final int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(final float x, final float y, final float z) {
        this.delegate.setNormal(x, y, z);
        return this;
    }
}
