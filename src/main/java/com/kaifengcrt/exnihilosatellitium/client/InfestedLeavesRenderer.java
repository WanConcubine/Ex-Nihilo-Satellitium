package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.InfestedLeavesBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Draws the leaf block that was replaced (so infested leaves keep their wood
 * type) plus a white wash that gets stronger as the infestation matures, which
 * is the classic Ex Nihilo read. The white motes come from the block entity tick.
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

        // White wash: same cube silhouette, alpha follows the progress.
        final float alpha = Math.min(0.8F, leaves.getProgress() * 0.8F);
        if (alpha > 0.02F) {
            pose.pushPose();
            LevelRenderer.renderVoxelShape(pose, buffers.getBuffer(RenderType.DEBUG_QUADS),
                    Shapes.block(), 0.0D, 0.0D, 0.0D, 1.0F, 1.0F, 1.0F, alpha, true);
            pose.popPose();
        }
    }
}
