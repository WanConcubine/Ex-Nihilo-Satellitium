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

        // 原版把料画成贴在网面上的平面：底 0.70，随进度略微抬升
        final float y = 0.70F + 0.14F * Math.min(1.0F, sieve.progress());
        pose.pushPose();
        pose.translate(0.0625F, y, 0.0625F);
        pose.scale(0.875F, 0.02F, 0.875F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, light, overlay,
                ModelData.EMPTY, RenderType.solid());
        pose.popPose();
    }
}
