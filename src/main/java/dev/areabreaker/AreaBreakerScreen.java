package dev.areabreaker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AreaBreakerScreen extends Screen {
    private static final int[] SIZES = {1, 2, 3, 4, 5, 10};

    public AreaBreakerScreen() {
        super(Text.literal("AreaBreaker — размер области"));
    }

    @Override
    protected void init() {
        int buttonWidth = 150;
        int buttonHeight = 24;
        int gap = 5;
        int totalHeight = SIZES.length * buttonHeight + (SIZES.length - 1) * gap;
        int startY = (this.height - totalHeight) / 2;
        for (int i = 0; i < SIZES.length; i++) {
            int size = SIZES[i];
            int y = startY + i * (buttonHeight + gap);
            String label = size == 1 ? "Выкл" : size + " x " + size;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                AreaBreakerClient.setAreaSize(size);
                if (this.client != null) this.client.setScreen(null);
            }).dimensions((this.width - buttonWidth) / 2, y, buttonWidth, buttonHeight).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, (this.height - 174) / 2 - 22, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Ломай блок обычным удержанием ЛКМ. Только одиночная игра."),
                this.width / 2, (this.height + 174) / 2 + 8, 0xCCCCCC);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}