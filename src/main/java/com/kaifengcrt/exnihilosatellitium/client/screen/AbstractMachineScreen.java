package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.AbstractMachineMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Shared machine screen: background texture, ghost slots, status line, the
 * progress-bar-to-JEI click and the inline auto-output panel.
 *
 * <p>1.20.1's forge has no GUI-layer stack, so the auto-output configuration is
 * an inline panel owned by this screen instead of a second screen.</p>
 */
public abstract class AbstractMachineScreen<T extends AbstractMachineMenu>
        extends AbstractContainerScreen<T> {

    public static final ResourceLocation ARROW_TRACK = new ResourceLocation(
            "exnihilosatellitium", "textures/gui/arrow_track.png");
    public static final ResourceLocation ARROW_FILL = new ResourceLocation(
            "exnihilosatellitium", "textures/gui/arrow_fill.png");
    public static final int PROGRESS_X = 52;
    public static final int PROGRESS_Y = 26;
    public static final int PROGRESS_W = 24;
    public static final int PROGRESS_H = 16;

    private static final int PAGE_Y = 74;
    private static final int PAGE_BUTTON_W = 12;
    private static final int PAGE_BUTTON_H = 14;
    private static final int PAGE_PREV_X = 99;
    private static final int PAGE_NEXT_X = 157;
    private static final int PAGE_LABEL_X = 134;

    private final MachineOutputPanel outputPanel;
    private Button prevPageButton;
    private Button nextPageButton;

    protected AbstractMachineScreen(final T menu, final Inventory playerInventory,
                                    final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 93;
        this.outputPanel = new MachineOutputPanel(this, menu);
    }

    @Override
    protected void init() {
        super.init();
        this.outputPanel.init();
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

    @Override
    protected void containerTick() {
        super.containerTick();
        this.outputPanel.tick();
    }

    /** Public wrapper for the output panel's widgets ({@code addRenderableWidget} is protected). */
    public <W extends net.minecraft.client.gui.components.AbstractWidget> W addMachineWidget(
            final W widget) {
        return this.addRenderableWidget(widget);
    }

    /** Bottom-left corner of the machine panel, exposed for the output panel. */
    public final int guiLeft() {
        return this.leftPos;
    }

    public final int guiTop() {
        return this.topPos;
    }

    public final Font getFont() {
        return this.font;
    }

    /** The player's facing, frozen when the screen opens. */
    public final Direction playerFacing() {
        return this.minecraft != null && this.minecraft.player != null
                ? this.minecraft.player.getDirection() : Direction.NORTH;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick,
                            final int mouseX, final int mouseY) {
        // 1.20.1's AbstractContainerScreen#render does NOT call renderBackground()
        // (1.21.1 moved it into render() instead); every vanilla container screen
        // calls it from its own renderBg. Without this the machine panel sits on
        // the world with no dimming.
        this.renderBackground(graphics);
        this.drawBackground(graphics);
        MachineGui.drawGhosts(graphics, this.menu, this.leftPos, this.topPos);
        this.drawGauges(graphics);
        if (this.menu.pages() > 1) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.exnihilosatellitium.page.label",
                            this.menu.page() + 1, this.menu.pages()),
                    this.leftPos + PAGE_LABEL_X, this.topPos + PAGE_Y + 3, 0x404040);
        }
        this.outputPanel.renderBackground(graphics);
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
        this.renderExtraTooltips(graphics, mouseX, mouseY);
        this.outputPanel.renderOverlay(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        // Check the progress arrow before the container handles the click: the
        // vanilla handler consumes clicks that land on the GUI background in some
        // paths, which used to swallow the JEI shortcut.
        final boolean overProgress = mouseX >= this.leftPos + PROGRESS_X - 1
                && mouseX < this.leftPos + PROGRESS_X + PROGRESS_W + 1
                && mouseY >= this.topPos + PROGRESS_Y - 1
                && mouseY < this.topPos + PROGRESS_Y + PROGRESS_H + 1;
        if (button == 0 && overProgress && MachineGui.openJei(this.menu)) {
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.outputPanel.isInside(mouseX, mouseY)) {
            return true;
        }
        return false;
    }

    /** Machine panel texture. */
    protected abstract void drawBackground(GuiGraphics graphics);

    /** Per-tier gauges (energy bar or flame gauge). */
    protected abstract void drawGauges(GuiGraphics graphics);

    /** Extra tooltips on gauge hover. */
    protected void renderExtraTooltips(final GuiGraphics graphics, final int mouseX,
                                       final int mouseY) {
    }

    /** Tooltip shared by both progress bars. */
    protected final void renderProgressTooltip(final GuiGraphics graphics, final int mouseX,
                                               final int mouseY) {
        if (!this.isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)) {
            return;
        }
        final String progressKey = net.minecraftforge.fml.ModList.get().isLoaded("jei")
                ? "gui.exnihilosatellitium.progress.hint"
                : "gui.exnihilosatellitium.progress";
        graphics.renderTooltip(this.font, Component.translatable(progressKey,
                        this.menu.data().get(MachineBlockEntity.DATA_WORK) * 100
                                / Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS))),
                mouseX, mouseY);
    }

    protected final void renderProgressBar(final GuiGraphics graphics) {
        final int ticks = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_TICKS));
        final int work = this.menu.data().get(MachineBlockEntity.DATA_WORK);
        graphics.blit(ARROW_TRACK,
                this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y, 0, 0, PROGRESS_W, PROGRESS_H);
        final int progress = Math.min(PROGRESS_W, work * PROGRESS_W / ticks);
        if (progress > 0) {
            graphics.blit(ARROW_FILL,
                    this.leftPos + PROGRESS_X, this.topPos + PROGRESS_Y, 0, 0, progress, PROGRESS_H);
        }
    }
}
