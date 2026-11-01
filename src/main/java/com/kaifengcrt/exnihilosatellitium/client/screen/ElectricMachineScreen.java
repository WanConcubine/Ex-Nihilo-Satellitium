package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Electric machine screen: energy bar, progress arrow, four upgrade slots. */
public class ElectricMachineScreen extends AbstractContainerScreen<ElectricMachineMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/electric_machine.png");

    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 20;
    private static final int ENERGY_W = 9;
    private static final int ENERGY_H = 44;
    private static final int PROGRESS_X = 52;
    private static final int PROGRESS_Y = 32;
    private static final int PROGRESS_W = 20;
    private static final int PROGRESS_H = 6;

    public ElectricMachineScreen(final ElectricMachineMenu menu, final Inventory playerInventory,
                                 final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 93;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick,
                            final int mouseX, final int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        MachineGui.drawGhosts(graphics, this.menu, this.leftPos, this.topPos);

        graphics.fill(this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y,
                this.leftPos + PROGRESS_X + PROGRESS_W, this.topPos + PROGRESS_Y + PROGRESS_H,
                0xFF2B2B2B);

        final int ticks = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS));
        final int work = this.menu.data().get(MachineBlockEntity.DATA_WORK);
        final int progress = Math.min(PROGRESS_W, work * PROGRESS_W / ticks);
        if (progress > 0) {
            graphics.fill(this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y,
                    this.leftPos + PROGRESS_X + progress, this.topPos + PROGRESS_Y + PROGRESS_H,
                    0xFF5FBF4A);
        }

        graphics.fill(this.leftPos + ENERGY_X, this.topPos + ENERGY_Y,
                this.leftPos + ENERGY_X + ENERGY_W, this.topPos + ENERGY_Y + ENERGY_H,
                0xFF2B2B2B);

        final int energy = this.menu.data().get(MachineBlockEntity.DATA_ENERGY);
        final int capacity = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_ENERGY_MAX));
        final int filled = Math.min(ENERGY_H - 2, energy * (ENERGY_H - 2) / capacity);
        if (filled > 0) {
            graphics.fill(this.leftPos + ENERGY_X + 1,
                    this.topPos + ENERGY_Y + ENERGY_H - 1 - filled,
                    this.leftPos + ENERGY_X + ENERGY_W - 1,
                    this.topPos + ENERGY_Y + ENERGY_H - 1,
                    0xFFE0701C);
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY,
                       final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        MachineGui.drawStatus(graphics, this.font, this.menu,
                this.leftPos + this.imageWidth - 8, this.topPos + 6);
        MachineGui.drawHoverHint(graphics, this.font, this.menu, this.hoveredSlot, mouseX, mouseY);
        if (this.isHovering(ENERGY_X, ENERGY_Y, ENERGY_W, ENERGY_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.exnihilosatellitium.energy",
                            this.menu.data().get(MachineBlockEntity.DATA_ENERGY),
                            this.menu.data().get(MachineBlockEntity.DATA_ENERGY_MAX)),
                    mouseX, mouseY);
        } else if (this.isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.exnihilosatellitium.progress",
                            this.menu.data().get(MachineBlockEntity.DATA_WORK) * 100
                                    / Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS))),
                    mouseX, mouseY);
        }
    }
}
