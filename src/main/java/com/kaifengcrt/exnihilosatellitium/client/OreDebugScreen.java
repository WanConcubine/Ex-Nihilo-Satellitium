package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterial;
import com.kaifengcrt.exnihilosatellitium.common.material.OreMaterials;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreBlocks;
import com.kaifengcrt.exnihilosatellitium.registry.ModOreItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Debug-only contact sheet of every ore item icon, used to eyeball tinting. */
public final class OreDebugScreen extends Screen {

    public OreDebugScreen() {
        super(Component.literal("ore icons"));
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        int index = 0;
        for (final OreMaterial material : OreMaterials.all()) {
            final var entries = ModOreItems.byMaterial().get(material.id());
            for (final String kind : ModOreBlocks.KINDS) {
                graphics.renderItem(new ItemStack(entries.get("ore_" + kind).get()),
                        20 + (index % 18) * 22, 24 + (index / 18) * 22);
                index++;
            }
            for (final String stage : List.of("broken", "crushed", "powdered")) {
                graphics.renderItem(new ItemStack(entries.get(stage).get()),
                        20 + (index % 18) * 22, 24 + (index / 18) * 22);
                index++;
            }
        }
        graphics.drawString(this.font, "ore icon sheet (debug)", 20, 10, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
