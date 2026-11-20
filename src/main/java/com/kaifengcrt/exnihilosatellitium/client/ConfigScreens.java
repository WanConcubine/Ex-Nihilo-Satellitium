package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.platform.config.ExConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Client-only wiring for the Forge mod list's "Config" button.
 *
 * <p>Forge 1.20.1 has the {@code ConfigScreenFactory} extension point but no
 * generic ModConfigSpec screen (that is a NeoForge feature), so this screen is
 * hand-written. It only exposes the two {@code [ores]} options because those are
 * the ones players asked for; everything else stays a text-file edit.</p>
 *
 * <p>Both options are restart-only: the creative tab and the sift/hammer tables
 * are built once at startup, so the screen always shows the reminder and saving
 * only rewrites the file for the next start.</p>
 */
public final class ConfigScreens {

    /** Same shape materials.json uses for {@code id}; keeps typos out of {@code ores.keep}. */
    private static final Pattern ID = Pattern.compile("[a-z0-9_]{1,64}");

    private ConfigScreens() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new OreConfigScreen(parent)));
    }

    private static final class OreConfigScreen extends Screen {

        private final Screen parent;
        private boolean hideUnprovided;
        private Button toggle;
        private EditBox keepBox;
        private boolean saved;
        private String error;

        private OreConfigScreen(final Screen parent) {
            super(Component.translatable("exnihilosatellitium.config.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            this.hideUnprovided = ExConfig.oresHideUnprovided();

            final int left = this.width / 2 - 160;
            final int top = this.height / 2 - 60;

            this.toggle = Button.builder(toggleLabel(), button -> {
                this.hideUnprovided = !this.hideUnprovided;
                button.setMessage(toggleLabel());
                this.saved = false;
            }).bounds(left + 170, top - 2, 150, 20).build();
            addRenderableWidget(this.toggle);

            this.keepBox = new EditBox(this.font, left + 170, top + 28, 150, 20,
                    Component.translatable("exnihilosatellitium.config.keep"));
            this.keepBox.setValue(String.join(", ", ExConfig.oresKeep()));
            this.keepBox.setMaxLength(512);
            addRenderableWidget(this.keepBox);

            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> save())
                    .bounds(this.width / 2 - 154, this.height - 28, 150, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                    .bounds(this.width / 2 + 4, this.height - 28, 150, 20).build());
        }

        private Component toggleLabel() {
            return Component.translatable(this.hideUnprovided
                    ? "exnihilosatellitium.config.value.on"
                    : "exnihilosatellitium.config.value.off");
        }

        private void save() {
            final List<String> keep = new ArrayList<>();
            for (final String part : this.keepBox.getValue().split(",")) {
                final String id = part.trim();
                if (id.isEmpty()) {
                    continue;
                }
                if (!ID.matcher(id).matches()) {
                    this.error = id;
                    this.saved = false;
                    return;
                }
                keep.add(id);
            }
            ExConfig.ORES_HIDE_UNPROVIDED.set(this.hideUnprovided);
            ExConfig.ORES_KEEP.set(keep);
            ExConfig.SPEC.save();
            this.error = null;
            this.saved = true;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY,
                           final float partialTick) {
            renderBackground(graphics);
            super.render(graphics, mouseX, mouseY, partialTick);

            final int left = this.width / 2 - 160;
            final int top = this.height / 2 - 60;

            graphics.drawCenteredString(this.font, this.title.getString(),
                    this.width / 2, 20, 0xFFFFFF);
            graphics.drawString(this.font,
                    Component.translatable("exnihilosatellitium.config.hide_unprovided").getString(),
                    left, top + 4, 0xFFFFFF);
            graphics.drawString(this.font,
                    Component.translatable("exnihilosatellitium.config.keep").getString(),
                    left, top + 34, 0xFFFFFF);
            graphics.drawString(this.font,
                    Component.translatable("exnihilosatellitium.config.restart").getString(),
                    left, top + 66, 0xFFAA00);
            graphics.drawString(this.font,
                    Component.translatable("exnihilosatellitium.config.restart_detail").getString(),
                    left, top + 80, 0xFFAA00);

            if (this.saved) {
                graphics.drawString(this.font,
                        Component.translatable("exnihilosatellitium.config.saved").getString(),
                        left, top + 104, 0x55FF55);
            } else if (this.error != null) {
                graphics.drawString(this.font,
                        Component.translatable("exnihilosatellitium.config.bad_id", this.error).getString(),
                        left, top + 104, 0xFF5555);
            }
        }

        @Override
        public void onClose() {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }
    }
}
