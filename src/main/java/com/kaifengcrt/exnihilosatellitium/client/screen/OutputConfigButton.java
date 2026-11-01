package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

/**
 * Tab attached to the left edge of the machine GUI that opens the auto-output
 * configuration drawer.
 *
 * <p>The tab is drawn with plain fills (no extra texture): its right edge stays
 * flush with the machine panel and only the protruding 22 px are visible. The
 * 16x48 icon strip on top is idle / hovered / enabled, so the machine screen
 * shows at a glance whether auto output is running.</p>
 */
@OnlyIn(Dist.CLIENT)
public class OutputConfigButton extends Button {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExNihiloSatellitium.MOD_ID, "textures/gui/output_config.png");
    public static final int TAB_WIDTH = 22;
    public static final int TAB_HEIGHT = 28;

    private static final int ICON_SIZE = 16;
    private static final int ICON_X = 3;
    private static final int ICON_Y = 6;
    private static final int ENABLED_FRAME = 32;

    private final BooleanSupplier enabledState;

    public OutputConfigButton(final int x, final int y, final BooleanSupplier enabledState,
                              final Component tooltip, final Button.OnPress onPress) {
        super(x, y, TAB_WIDTH, TAB_HEIGHT, tooltip, onPress, DEFAULT_NARRATION);
        this.enabledState = enabledState;
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                final float partialTick) {
        final int x = this.getX();
        final int y = this.getY();
        // tab body: outer border, face, top/left light, bottom shadow
        graphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + TAB_WIDTH, y + TAB_HEIGHT - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + TAB_WIDTH - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + TAB_HEIGHT - 1, 0xFFFFFFFF);
        graphics.fill(x + 1, y + TAB_HEIGHT - 2, x + TAB_WIDTH, y + TAB_HEIGHT - 1, 0xFF555555);
        if (this.isHoveredOrFocused()) {
            graphics.fill(x + 1, y + 1, x + TAB_WIDTH, y + TAB_HEIGHT - 1, 0x30FFFFFF);
        }

        final int frame = this.enabledState.getAsBoolean() ? ENABLED_FRAME
                : (this.isHoveredOrFocused() ? ICON_SIZE : 0);
        graphics.blit(TEXTURE, x + ICON_X, y + ICON_Y, 0, frame, ICON_SIZE, ICON_SIZE,
                ICON_SIZE, ICON_SIZE * 3);
    }
}
