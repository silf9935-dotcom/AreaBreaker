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

public final class AreaBreakerClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static KeyBinding toggleKey;
    private static KeyBinding modeKey;
    private static KeyBinding smeltKey;
    private static KeyBinding addFilterKey;

    @Override
    public void onInitializeClient() {
        AreaBreakerSettings.load();
        AreaBreakerItems.initialize();
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("areabreaker", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.areabreaker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, category));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.areabreaker.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, category));
        modeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.areabreaker.mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, category));
        smeltKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.areabreaker.smelt", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
        addFilterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.areabreaker.filter_add", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, category));

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || !AreaBreakerSettings.isEnabled()) return;
            if (client.crosshairTarget instanceof BlockHitResult hit && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                String mode = AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.VEIN ? "Жила" : "Область";
                String size = AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.VEIN
                        ? "лимит " + AreaBreakerSettings.getVeinLimit()
                        : AreaBreakerSettings.getAreaSize() + "×" + AreaBreakerSettings.getAreaSize()
                        + (AreaBreakerSettings.getShape() == AreaBreakerSettings.Shape.TUNNEL
                        ? "×" + AreaBreakerSettings.getTunnelDepth() : "");
                String line = "AreaBreaker • " + mode + " • " + size + " • Ур. " + AreaBreakerSettings.getMiningLevel();
                drawContext.drawTextWithShadow(client.textRenderer, Text.literal(line), 8, 8, 0xFFB7D7B5);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) if (client.currentScreen == null) client.setScreen(new AreaBreakerScreen());
            while (toggleKey.wasPressed()) {
                AreaBreakerSettings.toggleEnabled();
                notify(client, "AreaBreaker: " + (AreaBreakerSettings.isEnabled() ? "включён" : "выключен"));
            }
            while (modeKey.wasPressed()) {
                AreaBreakerSettings.cycleMode();
                notify(client, "Режим: " + (AreaBreakerSettings.getMode() == AreaBreakerSettings.Mode.AREA ? "область" : "жила"));
            }
            while (smeltKey.wasPressed()) {
                AreaBreakerSettings.toggleAutoSmelt();
                notify(client, "Автоплавка: " + (AreaBreakerSettings.isAutoSmeltEnabled() ? "вкл" : "выкл"));
            }
            while (addFilterKey.wasPressed()) addTargetBlockToFilter();
        });
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
