package com.kaifengcrt.exnihiloimitatio.client;

import com.kaifengcrt.exnihiloimitatio.common.blockentity.BarrelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

/**
 * Draws the fluid surface inside the barrel.
 *
 * <p>The barrel model is an open-top box, so a single top quad is enough to
 * make water/lava visible. This is intentionally simple; a later milestone can
 * add side quads and flowing textures.</p>
 */
public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity> {

    public BarrelRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final BarrelBlockEntity barrel, final float partialTick, final PoseStack poseStack,
                       final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        final FluidStack fluid = barrel.getTank().getFluid();
        if (fluid.isEmpty()) {
            return;
        }

        final int amount = barrel.getTank().getFluidAmount();
        final int capacity = barrel.getTank().getCapacity();
        final float fill = (float) amount / (float) capacity;
        if (fill <= 0.0F) {
            return;
        }

        final IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        final ResourceLocation stillTexture = extensions.getStillTexture(fluid);
        if (stillTexture == null) {
            return;
        }

        final TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        final int tint = extensions.getTintColor(fluid);
        final float red = (tint >> 16 & 0xFF) / 255.0F;
        final float green = (tint >> 8 & 0xFF) / 255.0F;
        final float blue = (tint & 0xFF) / 255.0F;
        final float alpha = (tint >> 24 & 0xFF) / 255.0F;

        final float x0 = 1.0F / 16.0F;
        final float x1 = 15.0F / 16.0F;
        final float z0 = 1.0F / 16.0F;
        final float z1 = 15.0F / 16.0F;
        final float y = 1.0F / 16.0F + (14.0F / 16.0F) * fill;

        final int light = barrel.getLevel() != null
                ? LevelRenderer.getLightColor(barrel.getLevel(), barrel.getBlockPos().above())
                : 0xF000F0;

        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        final Matrix4f matrix = poseStack.last().pose();

        consumer.vertex(matrix, x0, y, z1)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0.0F), sprite.getV(1.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, x1, y, z1)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(1.0F), sprite.getV(1.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, x1, y, z0)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(1.0F), sprite.getV(0.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, x0, y, z0)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0.0F), sprite.getV(0.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
