package com.kaifengcrt.exnihiloimitatio.client;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
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
 * Draws the barrel contents.
 *
 * <p>The model is an inset open-top body (14x16x14), so a single top quad is
 * enough to show either fluid or compost. The quad grows with the fill level.
 * A later milestone can add side quads and flowing textures.</p>
 */
public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity> {

    private static final float X0 = 2.0F / 16.0F;
    private static final float X1 = 14.0F / 16.0F;
    private static final float Z0 = 2.0F / 16.0F;
    private static final float Z1 = 14.0F / 16.0F;
    private static final float BOTTOM = 1.0F / 16.0F;
    private static final float HEIGHT = 15.0F / 16.0F;

    public BarrelRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final BarrelBlockEntity barrel, final float partialTick, final PoseStack poseStack,
                       final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        final int light = barrel.getLevel() != null
                ? LevelRenderer.getLightColor(barrel.getLevel(), barrel.getBlockPos().above())
                : 0xF000F0;

        final FluidStack fluid = barrel.getTank().getFluid();
        if (!fluid.isEmpty()) {
            this.renderFluid(barrel, poseStack, bufferSource, light, fluid);
            return;
        }

        if (barrel.getCompostProgress() > 0) {
            this.renderCompost(barrel, poseStack, bufferSource, light);
        }
    }

    private void renderFluid(final BarrelBlockEntity barrel, final PoseStack poseStack,
                             final MultiBufferSource bufferSource, final int light, final FluidStack fluid) {
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

        final float y = BOTTOM + HEIGHT * fill;
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        topQuad(consumer, poseStack.last().pose(), sprite, y, light, red, green, blue, alpha);
    }

    private void renderCompost(final BarrelBlockEntity barrel, final PoseStack poseStack,
                               final MultiBufferSource bufferSource, final int light) {
        final float fill = barrel.getCompostProgress() / (float) BarrelBlockEntity.COMPOST_MAX;
        final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                ExNihiloImitatio.MOD_ID, "block/barrel_compost");
        final TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(texture);

        // Keep the full heap slightly below the rim so it does not z-fight.
        final float y = BOTTOM + HEIGHT * fill * 0.96F;
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        topQuad(consumer, poseStack.last().pose(), sprite, y, light, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void topQuad(final VertexConsumer consumer, final Matrix4f matrix,
                                final TextureAtlasSprite sprite, final float y, final int light,
                                final float red, final float green, final float blue, final float alpha) {
        consumer.vertex(matrix, X0, y, Z1)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0.0F), sprite.getV(1.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, X1, y, Z1)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(1.0F), sprite.getV(1.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, X1, y, Z0)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(1.0F), sprite.getV(0.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(matrix, X0, y, Z0)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0.0F), sprite.getV(0.0F))
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
