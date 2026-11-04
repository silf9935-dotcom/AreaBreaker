package dev.areabreaker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AreaBreakerScreen extends Screen {
    private static final int[] SIZES = {2, 3, 4, 5, 10};

    public AreaBreakerScreen() {
        super(Text.literal("AreaBreaker — размер области"));
    }

    @Override
    protected void init() {
        int buttonWidth = 120;
        int gap = 8;
        int total = SIZES.length * buttonWidth + (SIZES.length - 1) * gap;
        int startX = (this.width - total) / 2;
        int y = this.height / 2 - 10;
        for (int i = 0; i < SIZES.length; i++) {
            int size = SIZES[i];
            int x = startX + i * (buttonWidth + gap);
            this.addDrawableChild(ButtonWidget.builder(Text.literal(size + " x " + size), button -> {
                AreaBreakerClient.setAreaSize(size);
                if (this.client != null) this.client.setScreen(null);
            }).dimensions(x, y, buttonWidth, 24).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 42, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Выбери размер. Удерживай ЛКМ, глядя на блок (только одиночная игра)."),
                this.width / 2, this.height / 2 + 28, 0xCCCCCC);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
