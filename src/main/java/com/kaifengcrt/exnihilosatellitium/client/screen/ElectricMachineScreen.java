package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.client.GuiLayers;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.platform.Platform;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Electric machine screen: vertical FE gauge (blue bar + bolt), vanilla-style
 * arrow progress, four upgrade slots.
 */
public class ElectricMachineScreen extends AbstractContainerScreen<ElectricMachineMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/electric_machine.png");
    private static final ResourceLocation ENERGY_FILL =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/energy_fill.png");
    private static final ResourceLocation ENERGY_ICON =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "textures/gui/energy_icon.png");
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
    private static final int PAGE_Y = 74;
    private static final int PAGE_BUTTON_W = 12;
    private static final int PAGE_BUTTON_H = 14;
    private static final int PAGE_PREV_X = 99;
    private static final int PAGE_NEXT_X = 157;
    private static final int PAGE_LABEL_X = 134;

    private Button prevPageButton;
    private Button nextPageButton;

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
    protected void init() {
        super.init();
        this.addRenderableWidget(new OutputConfigButton(
                this.leftPos - OutputConfigButton.TAB_WIDTH, this.topPos + OUTPUT_TAB_Y,
                () -> this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0,
                Component.translatable("gui.exnihilosatellitium.output.button"),
                button -> this.openOutputConfig()));
        // Small page arrows under the 3x4 output grid. They stay hidden until
        // the machine actually has more than the base page.
        this.prevPageButton = this.addRenderableWidget(Button
                .builder(Component.literal("<"), button -> this.turnPage(-1))
                .bounds(this.leftPos + PAGE_PREV_X, this.topPos + PAGE_Y,
                        PAGE_BUTTON_W, PAGE_BUTTON_H)
                .build());
        this.prevPageButton.setTooltip(Tooltip.create(
                Component.translatable("gui.exnihilosatellitium.page.prev")));
        this.nextPageButton = this.addRenderableWidget(Button
                .builder(Component.literal(">"), button -> this.turnPage(1))
                .bounds(this.leftPos + PAGE_NEXT_X, this.topPos + PAGE_Y,
                        PAGE_BUTTON_W, PAGE_BUTTON_H)
                .build());
        this.nextPageButton.setTooltip(Tooltip.create(
                Component.translatable("gui.exnihilosatellitium.page.next")));
    }

    /** Switches the local view at once, then sends the absolute page to the server. */
    private void turnPage(final int delta) {
        final int target = this.menu.page() + delta;
        if (target < 0 || target >= this.menu.pages()) {
            return;
        }
        this.menu.setPage(target);
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, target);
        }
    }

    /** Opens the auto-output config as a layer, so this menu stays open. */
    private void openOutputConfig() {
        final MachineBlockEntity machine = this.menu.owner();
        if (machine != null) {
            final Direction facing = this.minecraft.player != null
                    ? this.minecraft.player.getDirection() : Direction.NORTH;
            GuiLayers.push(this.minecraft,
                    new MachineOutputScreen(this.menu, machine.getBlockPos(), facing));
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick,
                            final int mouseX, final int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        MachineGui.drawGhosts(graphics, this.menu, this.leftPos, this.topPos);
        if (this.menu.pages() > 1) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.exnihilosatellitium.page.label",
                            this.menu.page() + 1, this.menu.pages()),
                    this.leftPos + PAGE_LABEL_X, this.topPos + PAGE_Y + 3, 0x404040);
        }

        final int ticks = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS));
        final int work = this.menu.data().get(MachineBlockEntity.DATA_WORK);
        graphics.blit(ARROW_TRACK, this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y,
                0, 0, PROGRESS_W, PROGRESS_H);
        final int progress = Math.min(PROGRESS_W, work * PROGRESS_W / ticks);
        if (progress > 0) {
            graphics.blit(ARROW_FILL, this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y,
                    0, 0, progress, PROGRESS_H);
        }

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
        final boolean paged = this.menu.pages() > 1;
        this.prevPageButton.visible = paged;
        this.nextPageButton.visible = paged;
        this.prevPageButton.active = paged && this.menu.page() > 0;
        this.nextPageButton.active = paged && this.menu.page() < this.menu.pages() - 1;
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        MachineGui.drawStatus(graphics, this.font, this.menu,
                this.leftPos + this.imageWidth - 8, this.topPos + 6);
        MachineGui.drawHoverHint(graphics, this.font, this.menu, this.hoveredSlot, mouseX, mouseY);
        if (this.isHovering(GAUGE_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.exnihilosatellitium.energy",
                            this.menu.data().get(MachineBlockEntity.DATA_ENERGY),
                            this.menu.data().get(MachineBlockEntity.DATA_ENERGY_MAX)),
                    mouseX, mouseY);
        } else if (this.isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)) {
            final String progressKey = Platform.isModLoaded("jei")
                    ? "gui.exnihilosatellitium.progress.hint"
                    : "gui.exnihilosatellitium.progress";
            graphics.renderTooltip(this.font, Component.translatable(progressKey,
                            this.menu.data().get(MachineBlockEntity.DATA_WORK) * 100
                                    / Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS))),
                    mouseX, mouseY);
        }
    }
}
