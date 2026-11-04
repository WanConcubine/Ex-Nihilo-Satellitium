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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

/**
 * Draws the barrel contents.
 *
 * <p>The barrel is an inset open-top body (14x16x14). Compost is drawn as a
 * vanilla dirt block model that grows with the fill; its vertices are tinted by
 * the weighted average colour of the items put in (rotten flesh pulls it
 * reddish-brown, leaves push it green). Over the composting timer the tint
 * fades back to plain dirt, and the DIRT state shows the untinted dirt model
 * waiting for extraction.</p>
 */
public class BarrelRenderer implements BlockEntityRenderer<BarrelBlockEntity> {

    private static final float X0 = 2.0F / 16.0F;
    private static final float X1 = 14.0F / 16.0F;
    private static final float Z0 = 2.0F / 16.0F;
    private static final float Z1 = 14.0F / 16.0F;
    private static final float BOTTOM = 1.0F / 16.0F;
    private static final float HEIGHT = 15.0F / 16.0F;

    /** Dirt model footprint inside the 2..14 cavity, with a little clearance. */
    private static final float DIRT_MIN = 0.15F;
    private static final float DIRT_WIDTH = 0.70F;
    /** A full barrel is filled up to just below the rim. */
    private static final float DIRT_HEIGHT = 0.90F;
    /** Keep some of the dirt colour readable under the item tint. */
    private static final float TINT_STRENGTH = 0.65F;

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

        if (barrel.hasSolid()) {
            this.renderSolid(barrel, poseStack, bufferSource, light);
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
        // A top-only quad is nearly invisible at eye level, which made pale
        // fluids look empty. Draw the four inner sides too, inset half a pixel
        // so they do not z-fight with the barrel walls.
        contentBox(consumer, poseStack.last().pose(), sprite, y, light, red, green, blue,
                X0 + 0.5F / 16.0F, X1 - 0.5F / 16.0F,
                Z0 + 0.5F / 16.0F, Z1 - 0.5F / 16.0F);
    }

    private void renderCompost(final BarrelBlockEntity barrel, final PoseStack poseStack,
                               final MultiBufferSource bufferSource, final int light) {
        final float fill = barrel.getCompostFill();
        if (fill <= 0.0F) {
            return;
        }

        final ItemTint.Tint tint = compostTint(barrel);
        // Fade the item colours back to plain dirt over the composting timer.
        final float t = barrel.getCompostTransition();
        final float red = tint.red() + (1.0F - tint.red()) * t;
        final float green = tint.green() + (1.0F - tint.green()) * t;
        final float blue = tint.blue() + (1.0F - tint.blue()) * t;
        this.renderDirt(poseStack, bufferSource, light, red, green, blue, fill);
    }

    /**
     * Weighted average of the item colours: one entry per ingredient, weighted
     * by the compost points it contributed.
     */
    private static ItemTint.Tint compostTint(final BarrelBlockEntity barrel) {
        double red = 0.0D;
        double green = 0.0D;
        double blue = 0.0D;
        double weight = 0.0D;
        for (final BarrelBlockEntity.CompostIngredient ingredient : barrel.getCompostIngredients()) {
            final ItemTint.Tint tint = ItemTint.of(ingredient.item());
            final double points = ingredient.points();
            red += tint.red() * points;
            green += tint.green() * points;
            blue += tint.blue() * points;
            weight += points;
        }
        if (weight <= 0.0D) {
            return ItemTint.Tint.WHITE;
        }
        final float r = 1.0F - (1.0F - (float) (red / weight)) * TINT_STRENGTH;
        final float g = 1.0F - (1.0F - (float) (green / weight)) * TINT_STRENGTH;
        final float b = 1.0F - (1.0F - (float) (blue / weight)) * TINT_STRENGTH;
        return new ItemTint.Tint(r, g, b);
    }

    private void renderSolid(final BarrelBlockEntity barrel, final PoseStack poseStack,
                             final MultiBufferSource bufferSource, final int light) {
        final ItemStack stack = barrel.getSolid();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        final BlockState state = blockItem.getBlock().defaultBlockState();
        final TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer()
                .getBlockModelShaper().getParticleIcon(state);
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        contentBox(consumer, poseStack.last().pose(), sprite,
                BOTTOM + HEIGHT, light, 1.0F, 1.0F, 1.0F);
    }

    private void renderDirt(final PoseStack poseStack, final MultiBufferSource bufferSource, final int light) {
        this.renderDirt(poseStack, bufferSource, light, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws the vanilla dirt model inside the cavity. Scaling only Y keeps the
     * top texture square while the pile grows from a thin layer to a full barrel.
     */
    private void renderDirt(final PoseStack poseStack, final MultiBufferSource bufferSource,
                            final int light, final float red, final float green, final float blue,
                            final float fill) {
        if (fill <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(DIRT_MIN, BOTTOM, DIRT_MIN);
        poseStack.scale(DIRT_WIDTH, DIRT_HEIGHT * fill, DIRT_WIDTH);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.DIRT.defaultBlockState(), poseStack,
                TintVertexConsumer.buffer(bufferSource, red, green, blue),
                light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void contentBox(final VertexConsumer consumer, final Matrix4f matrix,
                                   final TextureAtlasSprite sprite, final float topY, final int light,
                                   final float red, final float green, final float blue) {
        contentBox(consumer, matrix, sprite, topY, light, red, green, blue, X0, X1, Z0, Z1);
    }

    private static void contentBox(final VertexConsumer consumer, final Matrix4f matrix,
                                   final TextureAtlasSprite sprite, final float topY, final int light,
                                   final float red, final float green, final float blue,
                                   final float x0, final float x1, final float z0, final float z1) {
        topQuad(consumer, matrix, sprite, topY, light, red, green, blue, 1.0F, x0, x1, z0, z1);

        // north
        side(consumer, matrix, sprite,
                x0, BOTTOM, z0, x0, topY, z0, x1, topY, z0, x1, BOTTOM, z0,
                light, red * 0.68F, green * 0.68F, blue * 0.68F, 0.0F, 0.0F, -1.0F);
        // south
        side(consumer, matrix, sprite,
                x1, BOTTOM, z1, x1, topY, z1, x0, topY, z1, x0, BOTTOM, z1,
                light, red * 0.86F, green * 0.86F, blue * 0.86F, 0.0F, 0.0F, 1.0F);
        // west
        side(consumer, matrix, sprite,
                x0, BOTTOM, z1, x0, topY, z1, x0, topY, z0, x0, BOTTOM, z0,
                light, red * 0.74F, green * 0.74F, blue * 0.74F, -1.0F, 0.0F, 0.0F);
        // east
        side(consumer, matrix, sprite,
                x1, BOTTOM, z0, x1, topY, z0, x1, topY, z1, x1, BOTTOM, z1,
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
                                final float red, final float green, final float blue,
                                final float alpha, final float x0, final float x1,
                                final float z0, final float z1) {
        vert(consumer, matrix, x0, y, z1)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(0.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vert(consumer, matrix, x1, y, z1)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(1.0F), sprite.getV(1.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vert(consumer, matrix, x1, y, z0)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(1.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vert(consumer, matrix, x0, y, z0)
                .setColor(red, green, blue, alpha)
                .setUv(sprite.getU(0.0F), sprite.getV(0.0F))
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
    }
}
