package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.FuelMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Fuel machine screen: the same vertical gauge as the electric one, but an
 * orange fill and a flame icon; vanilla-style arrow progress.
 */
public class FuelMachineScreen extends AbstractContainerScreen<FuelMachineMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/fuel_machine.png");
    private static final ResourceLocation FUEL_FILL =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/fuel_fill.png");
    private static final ResourceLocation FUEL_ICON =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/fuel_icon.png");
    private static final ResourceLocation ARROW_TRACK =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/arrow_track.png");
    private static final ResourceLocation ARROW_FILL =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/arrow_fill.png");

    private static final int GAUGE_X = 8;
    private static final int GAUGE_Y = 19;
    private static final int GAUGE_W = 10;
    private static final int GAUGE_H = 44;
    private static final int ICON_X = 9;
    private static final int ICON_Y = 36;
    private static final int ICON_SIZE = 8;
    private static final int PROGRESS_X = 52;
    private static final int PROGRESS_Y = 26;
    private static final int PROGRESS_W = 24;
    private static final int PROGRESS_H = 16;
    private static final int OUTPUT_TAB_Y = 72;

    public FuelMachineScreen(final FuelMachineMenu menu, final Inventory playerInventory,
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
    protected void init() {
        super.init();
        this.addRenderableWidget(new OutputConfigButton(
                this.leftPos - OutputConfigButton.TAB_WIDTH, this.topPos + OUTPUT_TAB_Y,
                () -> this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0,
                Component.translatable("gui.exnihilosatellitium.output.button"),
                button -> this.openOutputConfig()));
    }

    /** Opens the auto-output config as a layer, so this menu stays open. */
    private void openOutputConfig() {
        final MachineBlockEntity machine = this.menu.owner();
        if (machine != null) {
            final Direction facing = this.minecraft.player != null
                    ? this.minecraft.player.getDirection() : Direction.NORTH;
            this.minecraft.pushGuiLayer(
                    new MachineOutputScreen(this.menu, machine.getBlockPos(), facing));
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick,
                            final int mouseX, final int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        MachineGui.drawGhosts(graphics, this.menu, this.leftPos, this.topPos);

        final int ticks = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS));
        final int work = this.menu.data().get(MachineBlockEntity.DATA_WORK);
        graphics.blit(ARROW_TRACK, this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y,
                0, 0, PROGRESS_W, PROGRESS_H);
        final int progress = Math.min(PROGRESS_W, work * PROGRESS_W / ticks);
        if (progress > 0) {
            graphics.blit(ARROW_FILL, this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y,
                    0, 0, progress, PROGRESS_H);
        }

        final int burn = this.menu.data().get(MachineBlockEntity.DATA_BURN);
        final int burnMax = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_BURN_MAX));
        final int filled = Math.min(GAUGE_H, burn * GAUGE_H / burnMax);
        if (filled > 0) {
            graphics.blit(FUEL_FILL, this.leftPos + GAUGE_X, this.topPos + GAUGE_Y + GAUGE_H - filled,
                    0, GAUGE_H - filled, GAUGE_W, filled);
        }
        graphics.blit(FUEL_ICON, this.leftPos + ICON_X, this.topPos + ICON_Y,
                0, 0, ICON_SIZE, ICON_SIZE);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0
                && this.isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)
                && MachineGui.openJei(this.menu)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY,
                       final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        MachineGui.drawStatus(graphics, this.font, this.menu,
                this.leftPos + this.imageWidth - 8, this.topPos + 6);
        MachineGui.drawHoverHint(graphics, this.font, this.menu, this.hoveredSlot, mouseX, mouseY);
        if (this.isHovering(GAUGE_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.exnihilosatellitium.burn",
                            this.menu.data().get(MachineBlockEntity.DATA_BURN) / 2,
                            this.menu.data().get(MachineBlockEntity.DATA_BURN_MAX) / 2),
                    mouseX, mouseY);
        } else if (this.isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)) {
            final String progressKey = net.neoforged.fml.ModList.get().isLoaded("jei")
                    ? "gui.exnihilosatellitium.progress.hint"
                    : "gui.exnihilosatellitium.progress";
            graphics.renderTooltip(this.font, Component.translatable(progressKey,
                            this.menu.data().get(MachineBlockEntity.DATA_WORK) * 100
                                    / Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS))),
                    mouseX, mouseY);
        }
    }
}
