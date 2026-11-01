package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

/**
 * Client animation for the machines.
 *
 * <p>Hammers get a moving head (BER cuboid) that rises and slams once per work
 * cycle, with the input item sitting on the anvil. Sieves get the input item
 * resting on the mesh and jittering while the machine runs.</p>
 */
public final class MachineRenderer implements BlockEntityRenderer<MachineBlockEntity> {

    private static final ResourceLocation HEAD_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExNihiloSatellitium.MOD_ID, "textures/block/machine_hammer_head.png");

    public MachineRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(final MachineBlockEntity machine, final float partialTick,
                       final PoseStack pose, final MultiBufferSource buffer,
                       final int packedLight, final int packedOverlay) {
        final Level level = machine.getLevel();
        if (level == null) {
            return;
        }
        final ItemStack input = machine.getInventory()
                .getItem(MachineBlockEntity.INPUT_SLOT);
        final float ticks = Math.max(1, machine.ticksPerOperation());
        final float phase = ((level.getGameTime() % (long) ticks) + partialTick) / ticks;
        // Fallback: animate whenever material is loaded, even if the Active sync
        // has not arrived yet; a loaded machine with a stall still looks alive.
        final boolean animating = machine.isActive() || !input.isEmpty();

        if (machine.kind() == MachineKind.HAMMER) {
            // raised at the start of the cycle, slams down onto the anvil at the end
            final float lift = animating
                    ? (1.0F - phase) * (4.0F / 16.0F) - (1.2F / 16.0F) : 0.0F;
            this.renderHead(pose, buffer, lift);
            if (!input.isEmpty()) {
                this.renderItem(level, input, pose, buffer, 0.5F, 6.0F / 16.0F, 0.5F, 0.22F);
            }
        } else if (!input.isEmpty()) {
            final float jitter = animating
                    ? (float) Math.sin((level.getGameTime() + partialTick) * 2.0F) * 0.10F : 0.0F;
            this.renderItem(level, input, pose, buffer,
                    0.5F + jitter, 10.42F / 16.0F, 0.5F + jitter, 0.30F);
        }
    }

    private void renderItem(final Level level, final ItemStack stack, final PoseStack pose,
                            final MultiBufferSource buffer, final float x, final float y,
                            final float z, final float scale) {
        final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, pose, buffer, level, 0);
        pose.popPose();
    }

    /** 6x3x6 hammer head, drawn at 5..11 / 7+lift..10+lift / 5..11 (pixels). */
    private void renderHead(final PoseStack pose, final MultiBufferSource buffer,
                            final float lift) {
        final VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutout(HEAD_TEXTURE));
        final float x0 = 5.0F / 16.0F;
        final float x1 = 11.0F / 16.0F;
        final float y0 = 7.0F / 16.0F + lift;
        final float y1 = 10.0F / 16.0F + lift;
        final float z0 = 5.0F / 16.0F;
        final float z1 = 11.0F / 16.0F;
        // down
        this.quad(pose, vertices, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, 1, 0);
        // up
        this.quad(pose, vertices, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0, -1, 0);
        // north
        this.quad(pose, vertices, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0, 0, 0, -1);
        // south
        this.quad(pose, vertices, x1, y1, z1, x0, y1, z1, x0, y0, z1, x1, y0, z1, 0, 0, 1);
        // west
        this.quad(pose, vertices, x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1, -1, 0, 0);
        // east
        this.quad(pose, vertices, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0, 1, 0, 0);
    }

    private void quad(final PoseStack pose, final VertexConsumer vertices,
                      final float x0, final float y0, final float z0,
                      final float x1, final float y1, final float z1,
                      final float x2, final float y2, final float z2,
                      final float x3, final float y3, final float z3,
                      final float nx, final float ny, final float nz) {
        final Matrix4f matrix = pose.last().pose();
        this.vertex(vertices, matrix, x0, y0, z0, 0.0F, 1.0F, nx, ny, nz);
        this.vertex(vertices, matrix, x1, y1, z1, 1.0F, 1.0F, nx, ny, nz);
        this.vertex(vertices, matrix, x2, y2, z2, 1.0F, 0.0F, nx, ny, nz);
        this.vertex(vertices, matrix, x3, y3, z3, 0.0F, 0.0F, nx, ny, nz);
    }

    private void vertex(final VertexConsumer vertices, final Matrix4f matrix,
                        final float x, final float y, final float z,
                        final float u, final float v,
                        final float nx, final float ny, final float nz) {
        vertices.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setUv1(OverlayTexture.NO_OVERLAY & 0xFFFF, OverlayTexture.NO_OVERLAY >> 16)
                .setUv2(LightTexture.FULL_BRIGHT & 0xFFFF, LightTexture.FULL_BRIGHT >> 16)
                .setNormal(nx, ny, nz);
    }
}
