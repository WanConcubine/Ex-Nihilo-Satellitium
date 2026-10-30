package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.CrucibleBlockEntity;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

/**
 * Draws the crucible contents: the melt material while solid > fluid, the
 * fluid otherwise (classic Ex Nihilo). Both are drawn as a filled box in the
 * cavity (2..14, 4..16).
 */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {

    // Slightly inside the cavity walls (2..14 / 4..16) to avoid z-fighting with
    // the inner faces of the model.
    private static final float X0 = 2.02F / 16.0F;
    private static final float X1 = 13.98F / 16.0F;
    private static final float Z0 = 2.02F / 16.0F;
    private static final float Z1 = 13.98F / 16.0F;
    private static final float BOTTOM = 4.02F / 16.0F;
    private static final float HEIGHT = 11.96F / 16.0F;

    public CrucibleRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final CrucibleBlockEntity crucible, final float partialTick, final PoseStack poseStack,
                       final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        final int light = crucible.getLevel() != null
                ? LevelRenderer.getLightColor(crucible.getLevel(), crucible.getBlockPos().above())
                : 0xF000F0;

        if (crucible.isMeltDominant() && crucible.getSolidVolume() > 0.0D) {
            final ItemStack stack = crucible.getPendingStack();
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                final TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer()
                        .getBlockModelShaper()
                        .getParticleIcon(blockItem.getBlock().defaultBlockState());
                final float top = BOTTOM + HEIGHT * crucible.getSolidRatio();
                final VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
                contentBox(consumer, poseStack.last().pose(), sprite, top, light, 1.0F, 1.0F, 1.0F, 1.0F);
                return;
            }
        }

        final FluidStack fluid = crucible.getTank().getFluid();
        if (fluid.isEmpty() || crucible.getTank().getCapacity() <= 0) {
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
        final float top = BOTTOM + HEIGHT * crucible.getFluidRatio();
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        contentBox(consumer, poseStack.last().pose(), sprite, top, light, red, green, blue, alpha);
    }

    private static void contentBox(final VertexConsumer consumer, final Matrix4f matrix,
                                   final TextureAtlasSprite sprite, final float topY, final int light,
                                   final float red, final float green, final float blue, final float alpha) {
        topQuad(consumer, matrix, sprite, topY, light, red, green, blue, alpha);

        side(consumer, matrix, sprite,
                X0, BOTTOM, Z0, X0, topY, Z0, X1, topY, Z0, X1, BOTTOM, Z0,
                light, red * 0.68F, green * 0.68F, blue * 0.68F, 0.0F, 0.0F, -1.0F);
        side(consumer, matrix, sprite,
                X1, BOTTOM, Z1, X1, topY, Z1, X0, topY, Z1, X0, BOTTOM, Z1,
                light, red * 0.86F, green * 0.86F, blue * 0.86F, 0.0F, 0.0F, 1.0F);
        side(consumer, matrix, sprite,
                X0, BOTTOM, Z1, X0, topY, Z1, X0, topY, Z0, X0, BOTTOM, Z0,
                light, red * 0.74F, green * 0.74F, blue * 0.74F, -1.0F, 0.0F, 0.0F);
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
                .setNormal(normalX, normalY, normalZ);
        vert(consumer, matrix, x1, y1, z1)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(0.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);
        vert(consumer, matrix, x2, y2, z2)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(1.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);
        vert(consumer, matrix, x3, y3, z3)
                .setColor(red, green, blue, 1.0F)
                .setUv(sprite.getU(1.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);
    }

    private static void topQuad(final VertexConsumer consumer, final Matrix4f matrix,
                                final TextureAtlasSprite sprite, final float y, final int light,
                                final float red, final float green, final float blue, final float alpha) {
        vert(consumer, matrix, X0, y, Z1)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(0.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
        vert(consumer, matrix, X1, y, Z1)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(1.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
        vert(consumer, matrix, X1, y, Z0)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(1.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
        vert(consumer, matrix, X0, y, Z0)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(0.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
