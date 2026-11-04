package dev.areabreaker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AreaBreakerScreen extends Screen {
    private static final int[] SIZES = {1, 2, 3, 4, 5, 10};

    public AreaBreakerScreen() { super(Text.literal("AreaBreaker 1.1.0")); }

    @Override
    protected void init() {
        int buttonWidth = Math.min(190, (this.width - 36) / 2);
        int left = this.width / 2 - buttonWidth - 4;
        int right = this.width / 2 + 4;
        int y = Math.max(68, Math.min(105, (this.height - 320) / 2));

        for (int i = 0; i < SIZES.length; i++) {
            final int size = SIZES[i];
            int x = (i % 2 == 0) ? left : right;
            int rowY = y + (i / 2) * 27;
            this.addDrawableChild(ButtonWidget.builder(sizeLabel(size), button -> {
                AreaBreakerClient.setAreaSize(size);
                refreshButtons();
            }).dimensions(x, rowY, buttonWidth, 23).build());
        }

        int optionsY = y + 3 * 27 + 6;
        this.addDrawableChild(ButtonWidget.builder(modeLabel(), button -> { AreaBreakerSettings.cycleMode(); refreshButtons(); })
                .dimensions(left, optionsY, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(shapeLabel(), button -> { AreaBreakerSettings.cycleShape(); refreshButtons(); })
                .dimensions(right, optionsY, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(protectionLabel(), button -> { AreaBreakerSettings.toggleDurabilityProtection(); refreshButtons(); })
                .dimensions(left, optionsY + 27, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(pickupLabel(), button -> { AreaBreakerSettings.toggleAutoPickup(); refreshButtons(); })
                .dimensions(right, optionsY + 27, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(enabledLabel(), button -> { AreaBreakerSettings.toggleEnabled(); refreshButtons(); })
                .dimensions(left, optionsY + 54, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(smeltLabel(), button -> { AreaBreakerSettings.toggleAutoSmelt(); refreshButtons(); })
                .dimensions(right, optionsY + 54, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(filterLabel(), button -> { AreaBreakerSettings.cycleFilterMode(); refreshButtons(); })
                .dimensions(left, optionsY + 81, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить блок (K)"), button -> {
            AreaBreakerClient.addTargetBlockToFilter();
            refreshButtons();
        }).dimensions(right, optionsY + 81, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(safeModeLabel(), button -> {
            AreaBreakerSettings.toggleSafeMode();
            button.setMessage(safeModeLabel());
        }).dimensions(left, optionsY + 108, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Прокачка (U)"), button -> {
            if (this.client != null) this.client.setScreen(new AreaBreakerUpgradeScreen());
        }).dimensions(right, optionsY + 108, buttonWidth, 23).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), button -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(this.width / 2 - 70, optionsY + 138, 140, 23).build());
    }

    private void refreshButtons() {
        for (var child : this.children()) {
            if (child instanceof ButtonWidget button) {
                String old = button.getMessage().getString();
                if (old.startsWith("Режим:")) button.setMessage(modeLabel());
                else if (old.startsWith("Форма:")) button.setMessage(shapeLabel());
                else if (old.startsWith("Защита кирки:")) button.setMessage(protectionLabel());
                else if (old.startsWith("Магнит:")) button.setMessage(pickupLabel());
                else if (old.startsWith("AreaBreaker:")) button.setMessage(enabledLabel());
                else if (old.startsWith("Автоплавка:")) button.setMessage(smeltLabel());
                else if (old.startsWith("Фильтр:")) button.setMessage(filterLabel());
                else if (old.startsWith("Защита блоков:")) button.setMessage(safeModeLabel());
                else if (old.startsWith("Выкл") || old.contains(" × ")) {
                    for (int size : SIZES) {
                        String plain = size == 1 ? "Выкл (1×1)" : size + " × " + size;
                        if (old.contains(plain)) button.setMessage(sizeLabel(size));
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
            case SQUARE -> "Квадрат"; case WALL -> "Стена"; case STRIP -> "Полоса"; case TUNNEL -> "Туннель 3×3";
        };
        return Text.literal("Форма: " + name);
    }
    private static Text protectionLabel() {
        return Text.literal("Защита кирки: " + (AreaBreakerSettings.isDurabilityProtectionEnabled() ? "Вкл" : "Выкл"));
    }
    private static Text pickupLabel() {
        return Text.literal("Магнит: " + (AreaBreakerSettings.isAutoPickupEnabled() ? "Вкл" : "Выкл"));
    }
    private static Text enabledLabel() {
        return Text.literal("AreaBreaker: " + (AreaBreakerSettings.isEnabled() ? "Вкл" : "Выкл"));
    }
    private static Text smeltLabel() {
        return Text.literal("Автоплавка: " + (AreaBreakerSettings.isAutoSmeltEnabled() ? "Вкл" : "Выкл"));
    }
    private static Text filterLabel() {
        return Text.literal("Фильтр: " + AreaBreakerSettings.getFilterMode() + " (" + AreaBreakerSettings.getFilterBlockCount() + ")");
    }
    private static Text safeModeLabel() {
        return Text.literal("Защита блоков: " + (AreaBreakerSettings.isSafeModeEnabled() ? "Вкл" : "Выкл"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int panelWidth = Math.min(440, this.width - 16);
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = 8;
        int panelBottom = this.height - 8;
        int y = Math.max(68, Math.min(105, (this.height - 320) / 2));
        context.fill(panelLeft - 1, panelTop - 1, panelLeft + panelWidth + 1, panelBottom + 1, 0xFF566579);
        context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelBottom, 0xF0121822);
        context.fill(panelLeft + 1, panelTop + 1, panelLeft + panelWidth - 1, 39, 0xFF202D3C);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("ПАНЕЛЬ ШАХТЁРА"), this.width / 2, 29, 0xFF9FC7F5);
        context.drawTextWithShadow(this.textRenderer, Text.literal("РАЗМЕР ОБЛАСТИ"), panelLeft + 18, 48, 0xFFE4EDF8);
        context.drawTextWithShadow(this.textRenderer, Text.literal("ПОВЕДЕНИЕ И ФИЛЬТРЫ"), panelLeft + 18, y + 80, 0xFFE4EDF8);
        String stats = "Уровень " + AreaBreakerSettings.getMiningLevel() + "  •  XP " + AreaBreakerSettings.getMiningXp()
                + "  •  Блоков " + AreaBreakerSettings.getBlocksMined() + "  •  Руд " + AreaBreakerSettings.getOresMined();
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(stats), this.width / 2, panelBottom - 30, 0xFFB7D7B5);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("V меню • U улучшения • B вкл • G режим • N форма • H плавка • K фильтр • M защита • J HUD"),
                this.width / 2, panelBottom - 16, 0xFF9AAABD);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean shouldPause() { return false; }
}
