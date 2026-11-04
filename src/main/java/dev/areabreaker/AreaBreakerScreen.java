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
        int y = Math.max(48, this.height / 2 - 92);

        for (int i = 0; i < SIZES.length; i++) {
            final int size = SIZES[i];
            int x = (i % 2 == 0) ? left : right;
            int rowY = y + (i / 2) * 30;
            String label = size == 1 ? "Выкл (1×1)" : size + " × " + size;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                AreaBreakerClient.setAreaSize(size);
                button.setMessage(Text.literal("✓ " + label));
            }).dimensions(x, rowY, buttonWidth, 24).build());
        }

        int optionsY = y + 3 * 30 + 5;
        this.addDrawableChild(ButtonWidget.builder(modeLabel(), button -> {
            AreaBreakerSettings.cycleMode();
            button.setMessage(modeLabel());
        }).dimensions(left, optionsY, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(shapeLabel(), button -> {
            AreaBreakerSettings.cycleShape();
            button.setMessage(shapeLabel());
        }).dimensions(right, optionsY, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(protectionLabel(), button -> {
            AreaBreakerSettings.toggleDurabilityProtection();
            button.setMessage(protectionLabel());
        }).dimensions(left, optionsY + 30, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(pickupLabel(), button -> {
            AreaBreakerSettings.toggleAutoPickup();
            button.setMessage(pickupLabel());
        }).dimensions(right, optionsY + 30, buttonWidth, 24).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), button -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(this.width / 2 - 70, optionsY + 66, 140, 24).build());
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
        return Text.literal("Подбор: " + (AreaBreakerSettings.isAutoPickupEnabled() ? "Вкл" : "Выкл"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int panelWidth = Math.min(430, this.width - 16);
        int panelLeft = (this.width - panelWidth) / 2;
        context.fill(panelLeft, 12, panelLeft + panelWidth, this.height - 12, 0xD91B2028);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 22, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Размер области · режим · форма · защита · подбор"),
                this.width / 2, 36, 0xB8C7D9);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}