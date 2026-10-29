package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.SieveBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Draws the material being sifted as a pile that grows with the work progress.
 *
 * <p>The pile is the input block squashed into the sieve interior; height is
 * proportional to {@link SieveBlockEntity#progress()}, so the fill is continuous
 * instead of jumping in steps.</p>
 */
public final class SieveRenderer implements BlockEntityRenderer<SieveBlockEntity> {

    public SieveRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final SieveBlockEntity sieve, final float partialTick, final PoseStack pose,
                       final MultiBufferSource buffers, final int light, final int overlay) {
        final ItemStack contents = sieve.getContents();
        if (contents.isEmpty() || !(contents.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        final BlockState state = blockItem.getBlock().defaultBlockState();
        if (state.isAir()) {
            return;
        }

        // 原版：底面固定 0.70，顶面 = 0.70 + 0.20 * 剩余量
        // 装进去是满的，越筛越薄（不是越筛越涨）
        final float remaining = 1.0F - Math.min(1.0F, sieve.progress());
        final float thickness = 0.20F * remaining;
        if (thickness <= 0.002F) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0625F, 0.70F, 0.0625F);
        pose.scale(0.875F, thickness, 0.875F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, light, overlay,
                ModelData.EMPTY, RenderType.solid());
        pose.popPose();
    }
}
