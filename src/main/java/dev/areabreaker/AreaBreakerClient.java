package dev.areabreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class AreaBreakerClient implements ClientModInitializer {
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        AreaBreakerItems.initialize();
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.areabreaker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                KeyBinding.Category.create(Identifier.of("areabreaker", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new AreaBreakerScreen());
            }
        });
    }

    public static int getAreaSize() {
        return AreaBreakerSettings.getAreaSize();
    }

    static void setAreaSize(int size) {
        AreaBreakerSettings.setAreaSize(size);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("AreaBreaker: " +
                    (size == 1 ? "Выкл (1x1)" : size + "x" + size)), true);
        }
    }
}