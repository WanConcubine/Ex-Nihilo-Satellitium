package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.AbstractMachineMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Shared drawing for the machine screens: ghost items in empty slots and the
 * running/blocked status line.
 */
public final class MachineGui {

    private static final int GHOST_SLOT_TINT = 0x88203040;
    private static final float GHOST_BRIGHTNESS = 0.72F;

    private MachineGui() {
    }

    public static void drawGhosts(final GuiGraphics graphics, final AbstractMachineMenu menu,
                                  final int left, final int top) {
        for (int index = 0; index < menu.machineSlotCount(); index++) {
            final Slot slot = menu.getSlot(index);
            if (slot.hasItem()) {
                continue;
            }
            final ItemStack ghost = menu.ghostFor(index);
            if (ghost.isEmpty()) {
                continue;
            }
            final int x = left + slot.x;
            final int y = top + slot.y;
            graphics.fill(x, y, x + 16, y + 16, GHOST_SLOT_TINT);
            RenderSystem.setShaderColor(GHOST_BRIGHTNESS, GHOST_BRIGHTNESS, GHOST_BRIGHTNESS, 1.0F);
            graphics.renderFakeItem(ghost, x, y);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static void drawStatus(final GuiGraphics graphics, final Font font,
                                  final AbstractMachineMenu menu, final int rightX, final int y) {
        final int status = menu.data().get(MachineBlockEntity.DATA_STATUS);
        final Component text = switch (status) {
            case 1 -> Component.translatable("gui.exnihilosatellitium.status.no_tool");
            case 2 -> Component.translatable("gui.exnihilosatellitium.status.no_input");
            case 3 -> Component.translatable("gui.exnihilosatellitium.status.no_energy");
            case 4 -> Component.translatable("gui.exnihilosatellitium.status.output_full");
            case 5 -> Component.translatable("gui.exnihilosatellitium.status.no_fuel");
            default -> Component.translatable("gui.exnihilosatellitium.status.running");
        };
        graphics.drawString(font, text, rightX - font.width(text), y,
                status == 0 ? 0xFF2E7D32 : 0xFFB3261E, false);
    }

    /** Opens JEI for the current input, or reports that JEI is not installed. */
    public static boolean openJei(final AbstractMachineMenu menu) {
        if (!ModList.get().isLoaded("jei")) {
            return false;
        }
        return com.kaifengcrt.exnihilosatellitium.compat.jei.MachineJeiPlugin.showFor(
                menu.getSlot(MachineBlockEntity.INPUT_SLOT).getItem(),
                menu.kind() == MachineKind.SIEVE);
    }

    public static void drawHoverHint(final GuiGraphics graphics, final Font font,
                                     final AbstractMachineMenu menu, final Slot hovered,
                                     final int mouseX, final int mouseY) {
        final Component hint = menu.hintFor(hovered);
        if (hint != null) {
            graphics.renderTooltip(font, hint.copy().withStyle(ChatFormatting.GRAY),
                    mouseX, mouseY);
        }
    }
}
