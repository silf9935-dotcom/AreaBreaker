package dev.areabreaker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AreaBreakerScreen extends Screen {
    private static final int[] SIZES = {1, 2, 3, 4, 5, 10};

    public AreaBreakerScreen() {
        super(Text.literal("AreaBreaker"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(190, (this.width - 36) / 2);
        int gap = 8;
        int left = this.width / 2 - buttonWidth - gap / 2;
        int right = this.width / 2 + gap / 2;
        int y = Math.max(75, Math.min(120, (this.height - 270) / 2));

        for (int i = 0; i < SIZES.length; i++) {
            final int size = SIZES[i];
            int x = (i % 2 == 0) ? left : right;
            int rowY = y + (i / 2) * 29;
            this.addDrawableChild(ButtonWidget.builder(sizeLabel(size), button -> {
                AreaBreakerClient.setAreaSize(size);
                this.refreshButtons();
            }).dimensions(x, rowY, buttonWidth, 24).build());
        }

        int optionsY = y + 3 * 29 + 8;
        this.addDrawableChild(ButtonWidget.builder(modeLabel(), button -> {
            AreaBreakerSettings.cycleMode();
            this.refreshButtons();
        }).dimensions(left, optionsY, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(shapeLabel(), button -> {
            AreaBreakerSettings.cycleShape();
            this.refreshButtons();
        }).dimensions(right, optionsY, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(protectionLabel(), button -> {
            AreaBreakerSettings.toggleDurabilityProtection();
            this.refreshButtons();
        }).dimensions(left, optionsY + 29, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(pickupLabel(), button -> {
            AreaBreakerSettings.toggleAutoPickup();
            this.refreshButtons();
        }).dimensions(right, optionsY + 29, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), button -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(this.width / 2 - 70, optionsY + 66, 140, 24).build());
    }

    private void refreshButtons() {
        // Rebuild labels in place so the selected values are always visible.
        for (var child : this.children()) {
            if (child instanceof ButtonWidget button) {
                String old = button.getMessage().getString();
                if (old.contains("Режим:")) button.setMessage(modeLabel());
                else if (old.contains("Форма:")) button.setMessage(shapeLabel());
                else if (old.contains("Защита кирки:")) button.setMessage(protectionLabel());
                else if (old.contains("Автоподбор:")) button.setMessage(pickupLabel());
                else if (old.contains("Выкл (1×1)") || old.matches(".*[0-9]+ × [0-9]+.*")
                        || old.startsWith("✓ ")) {
                    for (int size : SIZES) {
                        String label = size == 1 ? "Выкл (1×1)" : size + " × " + size;
                        if (old.contains(label) || old.contains(label.replace("✓ ", ""))) {
                            button.setMessage(sizeLabel(size));
                            break;
                        }
                    }
                }
            }
        }
    }

    private static Text sizeLabel(int size) {
        String label = size == 1 ? "Выкл (1×1)" : size + " × " + size;
        return Text.literal((AreaBreakerClient.getAreaSize() == size ? "✓ " : "") + label);
    }

    private static Text modeLabel() {
        return Text.literal("Режим: " + (AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.AREA ? "Область" : "Жила"));
    }

    private static Text shapeLabel() {
        String name = switch (AreaBreakerSettings.getShape()) {
            case SQUARE -> "Квадрат";
            case WALL -> "Стена";
            case STRIP -> "Полоса";
            case TUNNEL -> "Туннель";
        };
        return Text.literal("Форма: " + name);
    }

    private static Text protectionLabel() {
        return Text.literal("Защита кирки: " + (AreaBreakerSettings.isDurabilityProtectionEnabled() ? "Вкл" : "Выкл"));
    }

    private static Text pickupLabel() {
        return Text.literal("Автоподбор: " + (AreaBreakerSettings.isAutoPickupEnabled() ? "Вкл" : "Выкл"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int panelWidth = Math.min(440, this.width - 16);
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = 12;
        int panelBottom = this.height - 12;

        context.fill(panelLeft - 1, panelTop - 1, panelLeft + panelWidth + 1, panelBottom + 1, 0xFF566579);
        context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelBottom, 0xF0121822);
        context.fill(panelLeft + 1, panelTop + 1, panelLeft + panelWidth - 1, 43, 0xFF202D3C);
        context.fill(panelLeft + 12, 48, panelLeft + panelWidth - 12, 49, 0xFF3C526B);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("НАСТРОЙКА ДОБЫЧИ"), this.width / 2, 34, 0xFF9FC7F5);
        context.drawTextWithShadow(this.textRenderer, Text.literal("РАЗМЕР ОБЛАСТИ"), panelLeft + 18, 53, 0xFFE4EDF8);
        context.drawTextWithShadow(this.textRenderer, Text.literal("ПОВЕДЕНИЕ"), panelLeft + 18,
                y + 82, 0xFFE4EDF8);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("V — открыть меню  •  Изменения применяются сразу"),
                this.width / 2, panelBottom - 17, 0xFF9AAABD);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
