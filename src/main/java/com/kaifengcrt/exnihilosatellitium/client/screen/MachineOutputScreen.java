package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.AbstractMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.network.MachineOutputPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Auto-output configuration drawer, opened from the tab on the left edge of the
 * machine GUI.
 *
 * <p>Pushed as a NeoForge GUI layer ({@code Minecraft#pushGuiLayer}) instead of
 * a new screen: the machine menu stays open underneath, so configuring never
 * closes the GUI or drops the player's slot actions. The drawer is drawn flush
 * against the left border of the machine panel and does not dim the screen.</p>
 *
 * <p>Faces are labelled relative to the player (<em>front / back / left /
 * right</em>) because a machine has no facing of its own and most players do not
 * map north/south/east/west to a machine; the world direction is still in the
 * tooltip. The mapping is frozen when the drawer opens — the camera cannot turn
 * while a container screen is up.</p>
 */
@OnlyIn(Dist.CLIENT)
public class MachineOutputScreen extends Screen {

    private static final int PANEL_WIDTH = 132;
    private static final int PANEL_HEIGHT = 168;
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 186;
    private static final int FACE_WIDTH = 58;
    private static final int FACE_HEIGHT = 18;

    private final AbstractMachineMenu menu;
    private final BlockPos pos;
    private final Direction front;
    private final Direction[] faces = new Direction[6];
    private final Map<Direction, Button> faceButtons = new EnumMap<>(Direction.class);

    private Button autoButton;
    private int left;
    private int top;

    /** Local copy so rapid clicks never compute from a stale synced value. */
    private int localMask;
    private boolean localAuto;
    private int syncedMask = Integer.MIN_VALUE;
    private boolean syncedAuto;

    public MachineOutputScreen(final AbstractMachineMenu menu, final BlockPos pos,
                               final Direction front) {
        super(Component.translatable("gui.exnihilosatellitium.output.title"));
        this.menu = menu;
        this.pos = pos;
        this.front = front;
        // three pairs: vertical, depth, lateral
        this.faces[0] = Direction.UP;
        this.faces[1] = Direction.DOWN;
        this.faces[2] = front;
        this.faces[3] = front.getOpposite();
        this.faces[4] = front.getCounterClockWise();
        this.faces[5] = front.getClockWise();
    }

    @Override
    protected void init() {
        final int guiLeft = (this.width - GUI_WIDTH) / 2;
        final int guiTop = (this.height - GUI_HEIGHT) / 2;
        this.left = Math.max(2, guiLeft - PANEL_WIDTH);
        this.top = Math.max(2, Math.min(guiTop + 9, this.height - PANEL_HEIGHT - 4));

        this.localMask = this.menu.data().get(MachineBlockEntity.DATA_OUTPUT_MASK);
        this.localAuto = this.menu.data().get(MachineBlockEntity.DATA_AUTO_OUTPUT) != 0;
        this.syncedMask = this.localMask;
        this.syncedAuto = this.localAuto;
        this.faceButtons.clear();

        this.autoButton = this.addRenderableWidget(Button
                .builder(Component.empty(), button -> this.toggleAuto())
                .pos(this.left + 6, this.top + 24)
                .width(120)
                .build());

        for (int index = 0; index < this.faces.length; index++) {
            final Direction face = this.faces[index];
            final Button button = this.addRenderableWidget(Button
                    .builder(Component.empty(), pressed -> this.toggleFace(face))
                    .pos(this.left + 6 + (index % 2) * (FACE_WIDTH + 4),
                            this.top + 48 + (index / 2) * (FACE_HEIGHT + 4))
                    .width(FACE_WIDTH)
                    .tooltip(Tooltip.create(this.tooltipFor(face)))
                    .build());
            this.faceButtons.put(face, button);
        }

        this.addRenderableWidget(Button
                .builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .pos(this.left + (PANEL_WIDTH - 60) / 2, this.top + 140)
                .width(60)
                .build());

        this.refreshLabels();
    }

    @Override
    public void tick() {
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
        PacketDistributor.sendToServer(new MachineOutputPayload(this.pos, this.localAuto, this.localMask));
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

    /** Player-relative name of a face: up/down plus front/back/left/right. */
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

    // ------------------------------------------------------------- rendering

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                 final float partialTick) {
        // Drawer, not a modal dialog: no screen dim, just a shadow behind the
        // panel so it reads as sitting on top of the world next to the GUI.
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

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY,
                       final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        // Selected faces get a tinted border on top of the vanilla button: the
        // label stays a single word, so no translation can overflow the button.
        this.faceButtons.forEach((face, button) -> {
            if (this.isOn(face)) {
                drawSelected(graphics, button);
            }
        });
        final int centerX = this.left + PANEL_WIDTH / 2;
        graphics.drawCenteredString(this.font, this.title, centerX, this.top + 8, 0x404040);
        graphics.drawCenteredString(this.font,
                Component.translatable("gui.exnihilosatellitium.output.hint.face"),
                centerX, this.top + 116, 0x404040);
        graphics.drawCenteredString(this.font,
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

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        // E closes the overlay like Esc instead of doing nothing, which is what
        // players expect from a config layer on top of a container screen.
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
