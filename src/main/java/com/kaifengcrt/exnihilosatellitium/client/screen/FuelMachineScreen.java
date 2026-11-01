package com.kaifengcrt.exnihilosatellitium.client.screen;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.FuelMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Fuel machine screen: flame gauge instead of the energy bar, one fuel slot and
 * no upgrade slots.
 */
public class FuelMachineScreen extends AbstractMachineScreen<FuelMachineMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/fuel_machine.png");
    public static final ResourceLocation FUEL_FILL = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/fuel_fill.png");
    public static final ResourceLocation FUEL_ICON = new ResourceLocation(
            ExNihiloSatellitium.MOD_ID, "textures/gui/fuel_icon.png");

    private static final int FLAME_X = 8;
    private static final int FLAME_Y = 19;
    private static final int FLAME_W = 10;
    private static final int FLAME_H = 44;
    private static final int ICON_X = 9;
    private static final int ICON_Y = 36;
    private static final int ICON_SIZE = 8;

    public FuelMachineScreen(final FuelMachineMenu menu, final Inventory playerInventory,
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
        final int burn = this.menu.data().get(MachineBlockEntity.DATA_BURN);
        final int burnMax = Math.max(1, this.menu.data().get(MachineBlockEntity.DATA_BURN_MAX));
        final int filled = Math.min(FLAME_H, burn * FLAME_H / burnMax);
        if (filled > 0) {
            graphics.blit(FUEL_FILL, this.leftPos + FLAME_X, this.topPos + FLAME_Y + FLAME_H - filled,
                    0, FLAME_H - filled, FLAME_W, filled);
        }
        graphics.blit(FUEL_ICON, this.leftPos + ICON_X, this.topPos + ICON_Y,
                0, 0, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void renderExtraTooltips(final GuiGraphics graphics, final int mouseX,
                                       final int mouseY) {
        if (this.isHovering(FLAME_X, FLAME_Y, FLAME_W, FLAME_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(
                            "gui.exnihilosatellitium.burn",
                            this.menu.data().get(MachineBlockEntity.DATA_BURN),
                            this.menu.data().get(MachineBlockEntity.DATA_BURN_MAX)),
                    mouseX, mouseY);
            return;
        }
        this.renderProgressTooltip(graphics, mouseX, mouseY);
    }
}
