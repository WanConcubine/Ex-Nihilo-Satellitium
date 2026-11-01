package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Electric machine screen: vertical FE gauge (blue bar + bolt), vanilla-style
 * arrow progress, four upgrade slots.
 */
public class ElectricMachineScreen extends AbstractMachineScreen<ElectricMachineMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/electric_machine.png");
    public static final ResourceLocation ENERGY_FILL = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/energy_fill.png");
    public static final ResourceLocation ENERGY_ICON = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/energy_icon.png");
    public static final ResourceLocation ARROW_TRACK = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/arrow_track.png");
    public static final ResourceLocation ARROW_FILL = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/arrow_fill.png");

    private static final int GAUGE_X = 8;
    private static final int GAUGE_Y = 19;
    private static final int GAUGE_W = 10;
    private static final int GAUGE_H = 44;
    private static final int ICON_X = 9;
    private static final int ICON_Y = 36;
    private static final int ICON_SIZE = 8;

    public ElectricMachineScreen(final ElectricMachineMenu menu, final Inventory playerInventory,
                                 final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void drawBackground(final GuiGraphics graphics) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void drawGauges(final GuiGraphics graphics) {
        this.renderProgressBar(graphics);
        final int energy = this.menu.data().get(MachineBlockEntity.DATA_ENERGY);
        final int capacity = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_ENERGY_MAX));
        final int filled = Math.min(GAUGE_H, energy * GAUGE_H / capacity);
        if (filled > 0) {
            graphics.blit(ENERGY_FILL, this.leftPos + GAUGE_X, this.topPos + GAUGE_Y + GAUGE_H - filled,
                    0, GAUGE_H - filled, GAUGE_W, filled);
        }
        graphics.blit(ENERGY_ICON, this.leftPos + ICON_X, this.topPos + ICON_Y,
                0, 0, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void renderExtraTooltips(final GuiGraphics graphics, final int mouseX,
                                       final int mouseY) {
        if (this.isHovering(GAUGE_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.exnihilosatellitium.energy",
                            this.menu.data().get(MachineBlockEntity.DATA_ENERGY),
                            this.menu.data().get(MachineBlockEntity.DATA_ENERGY_MAX)),
                    mouseX, mouseY);
            return;
        }
        this.renderProgressTooltip(graphics, mouseX, mouseY);
    }
}
