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

        final float fill = Math.max(0.08F, sieve.progress());
        pose.pushPose();
        pose.translate(0.125F, 0.09F, 0.125F);
        pose.scale(0.75F, 0.28F * fill, 0.75F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffers, light, overlay,
                ModelData.EMPTY, RenderType.solid());
        pose.popPose();
    }
}
