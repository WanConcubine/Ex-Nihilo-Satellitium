package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.InfestedLeavesBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Draws the leaf block that was replaced, tinted towards white as the
 * infestation matures (the original mod's read). The tint is applied through
 * {@link WhiteningVertexConsumer} so the leaf texture stays visible instead of
 * being covered by a flat white box. White motes come from the block entity tick.
 */
public final class InfestedLeavesRenderer implements BlockEntityRenderer<InfestedLeavesBlockEntity> {

    public InfestedLeavesRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final InfestedLeavesBlockEntity leaves, final float partialTick,
                       final PoseStack pose, final MultiBufferSource buffers, final int light, final int overlay) {
        if (leaves.getMimic().isAir()) {
            return;
        }
        final float white = Math.min(0.85F, leaves.getProgress() * 0.9F);
        final MultiBufferSource whitening = white < 0.02F ? buffers : type ->
                new WhiteningVertexConsumer(buffers.getBuffer(type), white);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(leaves.getMimic(), pose, whitening,
                light, overlay, ModelData.EMPTY, RenderType.cutoutMipped());
    }
}
