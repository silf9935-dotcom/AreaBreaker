package dev.areabreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class AreaBreakerClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static KeyBinding toggleKey;
    private static KeyBinding modeKey;
    private static KeyBinding smeltKey;
    private static KeyBinding addFilterKey;
    private static KeyBinding upgradeKey;
    private static KeyBinding hudKey;
    private static KeyBinding shapeKey;
    private static KeyBinding safeModeKey;

    @Override
    public void onInitializeClient() {
        AreaBreakerSettings.load();
        AreaBreakerItems.initialize();
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("areabreaker", "main"));
        menuKey = register("key.areabreaker.menu", GLFW.GLFW_KEY_V, category);
        toggleKey = register("key.areabreaker.toggle", GLFW.GLFW_KEY_B, category);
        modeKey = register("key.areabreaker.mode", GLFW.GLFW_KEY_G, category);
        smeltKey = register("key.areabreaker.smelt", GLFW.GLFW_KEY_H, category);
        addFilterKey = register("key.areabreaker.filter_add", GLFW.GLFW_KEY_K, category);
        upgradeKey = register("key.areabreaker.upgrades", GLFW.GLFW_KEY_U, category);
        hudKey = register("key.areabreaker.hud", GLFW.GLFW_KEY_J, category);
        shapeKey = register("key.areabreaker.shape", GLFW.GLFW_KEY_N, category);
        safeModeKey = register("key.areabreaker.safe_mode", GLFW.GLFW_KEY_M, category);

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || !AreaBreakerSettings.isHudEnabled()) return;

            int x = 8;
            int y = 8;
            drawContext.fill(x - 3, y - 3, x + 214, y + 67, 0xB8101722);
            drawContext.fill(x - 3, y - 3, x + 214, y + 12, 0xE02B4058);
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal("AREA BREAKER  •  УР. " + AreaBreakerSettings.getMiningLevel()), x + 3, y + 1, 0xFFFFFFFF);

            String mode = AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.VEIN ? "ЖИЛА" : "ОБЛАСТЬ";
            String shape = switch (AreaBreakerSettings.getShape()) {
                case SQUARE -> "КВАДРАТ";
                case WALL -> "СТЕНА";
                case STRIP -> "ПОЛОСА";
                case TUNNEL -> "ТУННЕЛЬ " + AreaBreakerSettings.getTunnelDepth();
            };
            String dimensions = AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.VEIN
                    ? "лимит " + AreaBreakerSettings.getVeinLimit()
                    : AreaBreakerSettings.getAreaSize() + "×" + AreaBreakerSettings.getAreaSize() + " • " + shape;
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(mode + "  •  " + dimensions), x + 3, y + 17, 0xFFB7D7F5);

            String target = "Цель: —";
            int targetColor = 0xFFBFC9D5;
            if (client.crosshairTarget instanceof BlockHitResult hit && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                String id = Registries.BLOCK.getId(client.world.getBlockState(hit.getBlockPos()).getBlock()).toString();
                target = "Цель: " + id;
                if (id.toLowerCase(Locale.ROOT).contains("ore")) {
                    target += "  •  РУДА";
                    targetColor = 0xFFFFD36A;
                }
            }
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(target), x + 3, y + 29, targetColor);

            int barWidth = 100;
            drawContext.fill(x + 3, y + 43, x + 3 + barWidth, y + 49, 0xFF343E4A);
            int progress = (int)(barWidth * (AreaBreakerSettings.getMiningXp() % 250) / 250.0);
            drawContext.fill(x + 3, y + 43, x + 3 + progress, y + 49, 0xFF70C68B);
            drawContext.drawTextWithShadow(client.textRenderer,
                    Text.literal("XP до уровня: " + AreaBreakerSettings.getXpToNextLevel()), x + 110, y + 42, 0xFFE3EAF2);
            drawContext.drawTextWithShadow(client.textRenderer,
                    Text.literal("Сессия: " + AreaBreakerSettings.getSessionBlocks() + " блоков • " + AreaBreakerSettings.getSessionOres() + " руд"),
                    x + 3, y + 54, 0xFFB7D7B5);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) if (client.currentScreen == null) client.setScreen(new AreaBreakerScreen());
            while (upgradeKey.wasPressed()) if (client.currentScreen == null) client.setScreen(new AreaBreakerUpgradeScreen());
            while (toggleKey.wasPressed()) {
                AreaBreakerSettings.toggleEnabled();
                notify(client, "AreaBreaker: " + (AreaBreakerSettings.isEnabled() ? "включён" : "выключен"));
            }
            while (modeKey.wasPressed()) {
                AreaBreakerSettings.cycleMode();
                notify(client, "Режим: " + (AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.AREA ? "область" : "жила"));
            }
            while (shapeKey.wasPressed()) {
                AreaBreakerSettings.cycleShape();
                notify(client, "Форма: " + AreaBreakerSettings.getShape());
            }
            while (smeltKey.wasPressed()) {
                AreaBreakerSettings.toggleAutoSmelt();
                notify(client, "Автоплавка: " + (AreaBreakerSettings.isAutoSmeltEnabled() ? "вкл" : "выкл"));
            }
            while (safeModeKey.wasPressed()) {
                AreaBreakerSettings.toggleSafeMode();
                notify(client, "Защита важных блоков: " + (AreaBreakerSettings.isSafeModeEnabled() ? "вкл" : "выкл"));
            }
            while (hudKey.wasPressed()) {
                AreaBreakerSettings.toggleHud();
                notify(client, "HUD: " + (AreaBreakerSettings.isHudEnabled() ? "вкл" : "выкл"));
            }
            while (addFilterKey.wasPressed()) addTargetBlockToFilter();
        });
    }

    private static KeyBinding register(String translationKey, int key, KeyBinding.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, InputUtil.Type.KEYSYM, key, category));
    }

    private static void notify(MinecraftClient client, String message) {
        if (client.player != null) client.player.sendMessage(Text.literal(message), true);
    }

    public static void addTargetBlockToFilter() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !(client.crosshairTarget instanceof BlockHitResult hit)
                || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            notify(client, "Наведи прицел на блок, чтобы добавить его в список");
            return;
        }
        String id = Registries.BLOCK.getId(client.world.getBlockState(hit.getBlockPos()).getBlock()).toString();
        boolean added = AreaBreakerSettings.addFilterBlock(id);
        notify(client, added ? "Добавлен в список: " + id : "Удалён из списка: " + id);
    }

    public static int getAreaSize() { return AreaBreakerSettings.getAreaSize(); }
    static void setAreaSize(int size) {
        AreaBreakerSettings.setAreaSize(size);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal("AreaBreaker: " + (size == 1 ? "Выкл (1x1)" : size + "x" + size)), true);
    }
}
