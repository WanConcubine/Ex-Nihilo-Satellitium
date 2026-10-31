package com.kaifengcrt.exnihilosatellitium.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Blends every colour written to the wrapped buffer towards white. Used to draw
 * the original leaf block with a whitening factor instead of pasting a white
 * box over it, so the leaf texture stays readable.
 */
final class WhiteningVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final float amount;

    WhiteningVertexConsumer(final VertexConsumer delegate, final float amount) {
        this.delegate = delegate;
        this.amount = Math.max(0.0F, Math.min(1.0F, amount));
    }

    @Override
    public VertexConsumer addVertex(final float x, final float y, final float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
        this.delegate.setColor(
                red + Math.round((255 - red) * this.amount),
                green + Math.round((255 - green) * this.amount),
                blue + Math.round((255 - blue) * this.amount),
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
