package com.kaifengcrt.exnihilosatellitium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Version-neutral wrapper for the "overlay layer" screen stack.
 *
 * <p>1.21.1 NeoForge offers {@code Minecraft#pushGuiLayer}: the machine menu
 * stays open underneath the auto-output drawer, so configuring never closes a
 * container. 1.20.1 Forge has no GUI layer system, so the port re-implements
 * this pair (draw the drawer from the parent screen, or re-open the menu when
 * returning).</p>
 */
public final class GuiLayers {

    private GuiLayers() {
    }

    /** Pushes a screen as a new layer on top of the current (menu) screen. */
    public static void push(final Minecraft minecraft, final Screen layer) {
        minecraft.pushGuiLayer(layer);
    }

    /** Pops the top layer; {@code Screen#onClose} already does this on 1.21.1. */
    public static void pop(final Minecraft minecraft) {
        minecraft.popGuiLayer();
    }
}
