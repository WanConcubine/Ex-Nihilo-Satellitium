package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
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
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

/**
 * Draws the barrel contents.
 *
 * <p>The barrel is an inset open-top body (14x16x14). Compost and dirt are
 * drawn as a small textured box so the fill reads as volume, not a flat colour
 * plane. During the composting timer the compost tint blends towards dirt; the
 * DIRT state draws the vanilla dirt sprite and waits for extraction.</p>
 */
public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity> {

    private static final float X0 = 2.0F / 16.0F;
    private static final float X1 = 14.0F / 16.0F;
    private static final float Z0 = 2.0F / 16.0F;
    private static final float Z1 = 14.0F / 16.0F;
    private static final float BOTTOM = 1.0F / 16.0F;
    private static final float HEIGHT = 15.0F / 16.0F;

    private static final ResourceLocation COMPOST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "block/barrel_compost");
    private static final ResourceLocation DIRT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/dirt");

    public BarrelRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final BarrelBlockEntity barrel, final float partialTick, final PoseStack poseStack,
                       final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        final int light = barrel.getLevel() != null
                ? LevelRenderer.getLightColor(barrel.getLevel(), barrel.getBlockPos().above())
                : 0xF000F0;

        if (barrel.isDirtReady()) {
            this.renderDirt(poseStack, bufferSource, light);
            return;
        }

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
        final float fill = (float) barrel.getTank().getFluidAmount() / (float) barrel.getTank().getCapacity();
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
        final float fill = barrel.getCompostFill();
        if (fill <= 0.0F) {
            return;
        }

        final TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(COMPOST_TEXTURE);

        // Blend the compost towards a dirt-brown tint while the timer runs.
        final float t = barrel.getCompostTransition();
        final float red = 1.0F + (0.82F - 1.0F) * t;
        final float green = 0.97F + (0.68F - 0.97F) * t;
        final float blue = 0.88F + (0.52F - 0.88F) * t;

        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        contentBox(consumer, poseStack.last().pose(), sprite,
                BOTTOM + HEIGHT * fill, light, red, green, blue);
    }

    private void renderDirt(final PoseStack poseStack, final MultiBufferSource bufferSource, final int light) {
        final TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(DIRT_TEXTURE);

        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        contentBox(consumer, poseStack.last().pose(), sprite, BOTTOM + HEIGHT, light, 1.0F, 1.0F, 1.0F);
    }

    private static void contentBox(final VertexConsumer consumer, final Matrix4f matrix,
                                   final TextureAtlasSprite sprite, final float topY, final int light,
                                   final float red, final float green, final float blue) {
        topQuad(consumer, matrix, sprite, topY, light, red, green, blue, 1.0F);

        // north
        side(consumer, matrix, sprite,
                X0, BOTTOM, Z0, X0, topY, Z0, X1, topY, Z0, X1, BOTTOM, Z0,
                light, red * 0.68F, green * 0.68F, blue * 0.68F, 0.0F, 0.0F, -1.0F);
        // south
        side(consumer, matrix, sprite,
                X1, BOTTOM, Z1, X1, topY, Z1, X0, topY, Z1, X0, BOTTOM, Z1,
                light, red * 0.86F, green * 0.86F, blue * 0.86F, 0.0F, 0.0F, 1.0F);
        // west
        side(consumer, matrix, sprite,
                X0, BOTTOM, Z1, X0, topY, Z1, X0, topY, Z0, X0, BOTTOM, Z0,
                light, red * 0.74F, green * 0.74F, blue * 0.74F, -1.0F, 0.0F, 0.0F);
        // east
        side(consumer, matrix, sprite,
                X1, BOTTOM, Z0, X1, topY, Z0, X1, topY, Z1, X1, BOTTOM, Z1,
                light, red * 0.92F, green * 0.92F, blue * 0.92F, 1.0F, 0.0F, 0.0F);
    }

    private static VertexConsumer vert(final VertexConsumer consumer, final Matrix4f matrix,
                                       final float x, final float y, final float z) {
        final org.joml.Vector3f v = new org.joml.Vector3f(x, y, z).mulPosition(matrix);
        return consumer.addVertex(v.x, v.y, v.z);
    }

    private static void side(final VertexConsumer consumer, final Matrix4f matrix,
                             final TextureAtlasSprite sprite,
                             final float x0, final float y0, final float z0,
                             final float x1, final float y1, final float z1,
                             final float x2, final float y2, final float z2,
                             final float x3, final float y3, final float z3,
                             final int light, final float red, final float green, final float blue,
                             final float normalX, final float normalY, final float normalZ) {
        vert(consumer, matrix, x0, y0, z0)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(0.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ)
                ;
        vert(consumer, matrix, x1, y1, z1)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(0.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ)
                ;
        vert(consumer, matrix, x2, y2, z2)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(1.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ)
                ;
        vert(consumer, matrix, x3, y3, z3)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(1.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ)
                ;
    }

    private static void topQuad(final VertexConsumer consumer, final Matrix4f matrix,
                                final TextureAtlasSprite sprite, final float y, final int light,
                                final float red, final float green, final float blue, final float alpha) {
        vert(consumer, matrix, X0, y, Z1)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(0.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vert(consumer, matrix, X1, y, Z1)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(1.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vert(consumer, matrix, X1, y, Z0)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(1.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vert(consumer, matrix, X0, y, Z0)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(0.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
    }
}
