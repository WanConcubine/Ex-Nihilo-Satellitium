package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.AbstractMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.network.MachineOutputMessage;
import com.kaifengcrt.exnihilosatellitium.common.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Auto-output configuration panel attached to the left edge of the machine GUI.
 *
 * <p>1.20.1 has no {@code pushGuiLayer}, so unlike the 1.21.1 version this is not
 * a separate screen: the panel is drawn by the machine screen itself, to the
 * left of the machine panel, and its widgets are hidden until the tab is
 * clicked. The machine menu therefore stays open while configuring.</p>
 *
 * <p>Faces are labelled relative to the player (front / back / left / right)
 * because a machine has no facing of its own; the mapping is frozen when the
 * panel is first opened. The world direction stays in the tooltip.</p>
 */
public final class MachineOutputPanel {

    private static final int PANEL_WIDTH = 132;
    private static final int PANEL_HEIGHT = 168;
    private static final int FACE_WIDTH = 58;
    private static final int FACE_HEIGHT = 18;

    private final AbstractMachineScreen<?> screen;
    private final AbstractMachineMenu menu;
    private final BlockPos pos;
    private Direction front = Direction.NORTH;
    private final Direction[] faces = new Direction[6];
    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);

    private OutputConfigButton tab;
    private Button autoButton;
    private Button doneButton;
    private int left;
    private int top;
    private boolean open;

    /** Local copy so rapid clicks never compute from a stale synced value. */
    private int localMask;
    private boolean localAuto;
    private int syncedMask = Integer.MIN_VALUE;
    private boolean syncedAuto;

    MachineOutputPanel(final AbstractMachineScreen<?> screen, final AbstractMachineMenu menu) {
        this.screen = screen;
        this.menu = menu;
        this.pos = menu.owner() != null ? menu.owner().getBlockPos() : BlockPos.ZERO;
    }

    void init() {
        // The screen constructor runs before Minecraft attaches itself to the
        // screen, so the player facing must be frozen here, in init().
        this.front = this.screen.playerFacing();
        this.faces[0] = Direction.UP;
        this.faces[1] = Direction.DOWN;
        this.faces[2] = this.front;
        this.faces[3] = this.front.getOpposite();
        this.faces[4] = this.front.getCounterClockWise();
        this.faces[5] = this.front.getClockWise();
        final int guiLeft = this.screen.guiLeft();
        final int guiTop = this.screen.guiTop();
        this.left = Math.max(2, guiLeft - PANEL_WIDTH);
        this.top = Math.max(2, Math.min(guiTop + 9, this.screen.height - PANEL_HEIGHT - 4));

        this.tab = this.screen.addMachineWidget(new OutputConfigButton(
                Math.max(2, guiLeft - OutputConfigButton.TAB_WIDTH), guiTop + 72,
                () -> this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0,
                Component.translatable("gui.exnihilosatellitium.output.button"),
                button -> this.toggleOpen()));

        this.localMask = this.menu.data().get(MachineBlockEntity.DATA_OUTPUT_MASK);
        this.localAuto = this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0;
        this.syncedMask = this.localMask;
        this.syncedAuto = this.localAuto;
        this.faceButtons.clear();

        this.autoButton = this.screen.addMachineWidget(Button
                .builder(Component.empty(), button -> this.toggleAuto())
                .pos(this.left + 6, this.top + 24)
                .width(120)
                .build());

        for (int index = 0; index < this.faces.length; index++) {
            final Direction face = this.faces[index];
            final Button button = this.screen.addMachineWidget(Button
                    .builder(Component.empty(), pressed -> this.toggleFace(face))
                    .pos(this.left + 6 + (index % 2) * (FACE_WIDTH + 4),
                            this.top + 48 + (index / 2) * (FACE_HEIGHT + 4))
                    .width(FACE_WIDTH)
                    .tooltip(Tooltip.create(this.tooltipFor(face)))
                    .build());
            this.faceButtons.put(face, button);
        }

        this.doneButton = this.screen.addMachineWidget(Button
                .builder(CommonComponents.GUI_DONE, button -> this.toggleOpen())
                .pos(this.left + (PANEL_WIDTH - 60) / 2, this.top + 140)
                .width(60)
                .build());

        this.applyVisibility();
        this.refreshLabels();
    }

    private void applyVisibility() {
        this.autoButton.visible = this.open;
        this.doneButton.visible = this.open;
        this.faceButtons.values().forEach(button -> button.visible = this.open);
    }

    private void toggleOpen() {
        this.open = !this.open;
        if (this.open) {
            this.localMask = this.menu.data().get(MachineBlockEntity.DATA_OUTPUT_MASK);
            this.localAuto = this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0;
            this.syncedMask = this.localMask;
            this.syncedAuto = this.localAuto;
        }
        this.applyVisibility();
        this.refreshLabels();
    }

    void tick() {
        if (!this.open) {
            return;
        }
        // Only adopt server values when they actually changed; this keeps an
        // optimistic click alive until the server round-trip lands.
        final int serverMask = this.menu.data().get(MachineBlockEntity.DATA_OUTPUT_MASK);
        final boolean serverAuto = this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0;
        if (serverMask != this.syncedMask || serverAuto != this.syncedAuto) {
            this.syncedMask = serverMask;
            this.syncedAuto = serverAuto;
            this.localMask = serverMask;
            this.localAuto = serverAuto;
        }
        this.refreshLabels();
    }

    private void toggleAuto() {
        this.localAuto = !this.localAuto;
        if (this.localAuto && this.localMask == 0) {
            this.localMask = MachineBlockEntity.FACE_DOWN;
        }
        this.send();
    }

    private void toggleFace(final Direction face) {
        this.localMask ^= 1 << face.get3DDataValue();
        if (this.localMask == 0) {
            // Auto output without a target face would be a silent no-op.
            this.localAuto = false;
        }
        this.send();
    }

    private void send() {
        this.refreshLabels();
        ModNetwork.CHANNEL.sendToServer(new MachineOutputMessage(this.pos, this.localAuto, this.localMask));
    }

    private boolean isOn(final Direction face) {
        return (this.localMask & (1 << face.get3DDataValue())) != 0;
    }

    private void refreshLabels() {
        if (this.autoButton == null) {
            return;
        }
        this.autoButton.setMessage(Component.translatable(this.localAuto
                        ? "gui.exnihilosatellitium.output.auto_on"
                        : "gui.exnihilosatellitium.output.auto_off")
                .withStyle(this.localAuto ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_GRAY));
        this.faceButtons.forEach((face, button) -> button.setMessage(this.labelFor(face)
                .copy()
                .withStyle(this.isOn(face) ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_GRAY)));
    }

    private Component labelFor(final Direction face) {
        if (face == Direction.UP) {
            return Component.translatable("gui.exnihilosatellitium.output.side.up");
        }
        if (face == Direction.DOWN) {
            return Component.translatable("gui.exnihilosatellitium.output.side.down");
        }
        if (face == this.front) {
            return Component.translatable("gui.exnihilosatellitium.output.side.front");
        }
        if (face == this.front.getOpposite()) {
            return Component.translatable("gui.exnihilosatellitium.output.side.back");
        }
        if (face == this.front.getCounterClockWise()) {
            return Component.translatable("gui.exnihilosatellitium.output.side.left");
        }
        return Component.translatable("gui.exnihilosatellitium.output.side.right");
    }

    private static Component worldName(final Direction face) {
        return Component.translatable("gui.exnihilosatellitium.output.world."
                + face.getSerializedName());
    }

    private Component tooltipFor(final Direction face) {
        final Component name = this.labelFor(face);
        if (face.getAxis().isVertical()) {
            return Component.translatable("gui.exnihilosatellitium.output.face_hint", name);
        }
        return Component.translatable("gui.exnihilosatellitium.output.face_hint_world",
                name, worldName(face));
    }

    boolean isOpen() {
        return this.open;
    }

    boolean isInside(final double mouseX, final double mouseY) {
        return this.open
                && mouseX >= this.left && mouseX < this.left + PANEL_WIDTH
                && mouseY >= this.top && mouseY < this.top + PANEL_HEIGHT;
    }

    void renderBackground(final GuiGraphics graphics) {
        if (!this.open) {
            return;
        }
        final int x0 = this.left;
        final int y0 = this.top;
        final int x1 = this.left + PANEL_WIDTH;
        final int y1 = this.top + PANEL_HEIGHT;
        graphics.fill(x0 - 2, y0 - 2, x1 + 2, y1 + 2, 0x55000000);
        graphics.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, 0xFF000000);
        graphics.fill(x0, y0, x1, y1, 0xFFC6C6C6);
        graphics.fill(x0, y0, x1 - 1, y0 + 1, 0xFFFFFFFF);
        graphics.fill(x0, y0, x0 + 1, y1 - 1, 0xFFFFFFFF);
        graphics.fill(x0 + 1, y1 - 1, x1, y1, 0xFF555555);
        graphics.fill(x1 - 1, y0 + 1, x1, y1, 0xFF555555);
    }

    void renderOverlay(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        if (!this.open) {
            return;
        }
        this.faceButtons.forEach((face, button) -> {
            if (this.isOn(face)) {
                drawSelected(graphics, button);
            }
        });
        final int centerX = this.left + PANEL_WIDTH / 2;
        graphics.drawCenteredString(this.screen.getFont(),
                Component.translatable("gui.exnihilosatellitium.output.title"),
                centerX, this.top + 8, 0x404040);
        graphics.drawCenteredString(this.screen.getFont(),
                Component.translatable("gui.exnihilosatellitium.output.hint.face"),
                centerX, this.top + 116, 0x404040);
        graphics.drawCenteredString(this.screen.getFont(),
                Component.translatable("gui.exnihilosatellitium.output.hint.pipe"),
                centerX, this.top + 126, 0x707070);
    }

    private static void drawSelected(final GuiGraphics graphics, final Button button) {
        final int x = button.getX();
        final int y = button.getY();
        final int width = button.getWidth();
        final int height = button.getHeight();
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x302E7D32);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFF2E7D32);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF2E7D32);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFF2E7D32);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF2E7D32);
    }
}
