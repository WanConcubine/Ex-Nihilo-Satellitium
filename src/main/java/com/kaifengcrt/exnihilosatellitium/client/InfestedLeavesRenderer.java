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
 * Draws the leaf block that was replaced, so infested leaves keep their wood
 * type. The white motes come from the block entity tick.
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
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(leaves.getMimic(), pose, buffers,
                light, overlay, ModelData.EMPTY, RenderType.cutoutMipped());
    }
}
