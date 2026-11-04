package dev.areabreaker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AreaBreakerUpgradeScreen extends Screen {
    private static final String[] UPGRADES = {"vein", "magnet", "reinforced", "geologist", "tunneler", "reach"};

    public AreaBreakerUpgradeScreen() {
        super(Text.literal("AreaBreaker • Прокачка"));
    }

    @Override
    protected void init() {
        int width = Math.min(300, this.width - 24);
        int left = (this.width - width) / 2;
        int top = Math.max(48, (this.height - 300) / 2);
        for (int i = 0; i < UPGRADES.length; i++) {
            String id = UPGRADES[i];
            int y = top + i * 31;
            this.addDrawableChild(ButtonWidget.builder(upgradeLabel(id), button -> {
                if (AreaBreakerSettings.purchaseUpgrade(id)) {
                    button.setMessage(upgradeLabel(id));
                } else if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(Text.literal("Недостаточно очков прокачки или улучшение достигло максимума"), true);
                }
            }).dimensions(left, y, width, 25).build());
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(new AreaBreakerScreen());
        }).dimensions(this.width / 2 - 60, top + 6 * 31 + 7, 120, 23).build());
    }

    private static Text upgradeLabel(String id) {
        int rank = AreaBreakerSettings.getUpgradeRank(id);
        int max = AreaBreakerSettings.getUpgradeMax(id);
        String label = switch (id) {
            case "vein" -> "Жилы: +" + (rank * 16) + " к лимиту";
            case "magnet" -> "Магнит: радиус " + AreaBreakerSettings.getMagnetRadius();
            case "reinforced" -> "Укрепление: резерв " + AreaBreakerSettings.getDurabilityReserve();
            case "geologist" -> "Геолог: +" + rank + " XP за руду";
            case "tunneler" -> "Бур: глубина " + AreaBreakerSettings.getTunnelDepth();
            case "reach" -> "Дальность: " + String.format(java.util.Locale.ROOT, "%.1f", AreaBreakerSettings.getMiningReach());
            default -> id;
        };
        return Text.literal(label + "  [" + rank + "/" + max + "]");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Avoid the 1.21.11 screen blur being requested twice in one frame.
        context.fill(0, 0, this.width, this.height, 0xC0101722);
        int panelWidth = Math.min(340, this.width - 16);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(35, (this.height - 310) / 2);
        context.fill(left - 1, top - 1, left + panelWidth + 1, top + 300, 0xFF566579);
        context.fill(left, top, left + panelWidth, top + 299, 0xF0121822);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, top + 10, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Очки улучшений: " + AreaBreakerSettings.getAvailableUpgradePoints()
                        + "  •  Уровень " + AreaBreakerSettings.getMiningLevel()),
                this.width / 2, top + 28, 0xFFB7D7B5);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("1 очко за каждые 5 уровней шахтёра"),
                this.width / 2, top + 279, 0xFF9AAABD);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
