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
    public VertexConsumer vertex(final double x, final double y, final double z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
        this.delegate.color(
                Math.round(red * this.red),
                Math.round(green * this.green),
                Math.round(blue * this.blue),
                alpha);
        return this;
    }

    @Override
    public VertexConsumer uv(final float u, final float v) {
        this.delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(final int u, final int v) {
        this.delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(final int u, final int v) {
        this.delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(final float x, final float y, final float z) {
        this.delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        this.delegate.endVertex();
    }

    @Override
    public void defaultColor(final int red, final int green, final int blue, final int alpha) {
        this.delegate.defaultColor(
                Math.round(red * this.red),
                Math.round(green * this.green),
                Math.round(blue * this.blue),
                alpha);
    }

    @Override
    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }
}
