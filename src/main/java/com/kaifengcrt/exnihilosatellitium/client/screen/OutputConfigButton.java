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
 * Small 16x16 icon button in the machine GUI that opens the auto-output
 * configuration screen.
 *
 * <p>The texture is a three frame strip (16x48): idle, hovered, and enabled.
 * The enabled frame is drawn whenever the machine currently auto-ejects, so the
 * machine screen hints at the setting without opening the config.</p>
 */
@OnlyIn(Dist.CLIENT)
public class OutputConfigButton extends Button {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExNihiloSatellitium.MOD_ID, "textures/gui/output_config.png");
    private static final int SIZE = 16;
    private static final int FRAME_HEIGHT = 16;
    private static final int HOVER_FRAME = 16;
    private static final int ENABLED_FRAME = 32;

    private final BooleanSupplier enabledState;

    public OutputConfigButton(final int x, final int y, final BooleanSupplier enabledState,
                              final Component tooltip, final Button.OnPress onPress) {
        super(x, y, SIZE, SIZE, tooltip, onPress, DEFAULT_NARRATION);
        this.enabledState = enabledState;
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                final float partialTick) {
        final int frame = this.enabledState.getAsBoolean() ? ENABLED_FRAME
                : (this.isHoveredOrFocused() ? HOVER_FRAME : 0);
        graphics.blit(TEXTURE, this.getX(), this.getY(), 0, frame, SIZE, SIZE, SIZE,
                FRAME_HEIGHT * 3);
    }
}
