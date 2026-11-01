package com.kaifengcrt.exnihilosatellitium.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Blends every colour written to the wrapped buffer towards white. Used to draw
 * the original leaf block with a whitening factor instead of pasting a white
 * box over it, so the leaf texture stays readable.
 *
 * <p>1.20.1's {@link VertexConsumer} is a fluent, stateful builder (colour,
 * UV and normal are set before {@code endVertex}), so only the colour calls
 * need to be rewritten.</p>
 */
final class WhiteningVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final float amount;

    WhiteningVertexConsumer(final VertexConsumer delegate, final float amount) {
        this.delegate = delegate;
        this.amount = Math.max(0.0F, Math.min(1.0F, amount));
    }

    @Override
    public VertexConsumer vertex(final double x, final double y, final double z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
        this.delegate.color(blend(red), blend(green), blend(blue), alpha);
        return this;
    }

    private int blend(final int channel) {
        return channel + Math.round((255 - channel) * this.amount);
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
        this.delegate.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }
}
