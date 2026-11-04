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
    private static int areaSize = 3;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.areabreaker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                KeyBinding.Category.create(Identifier.of("areabreaker", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new AreaBreakerScreen());
            }
        });
    }

    static int getAreaSize() {
        return areaSize;
    }

    static void setAreaSize(int size) {
        if (size == 1 || size == 2 || size == 3 || size == 4 || size == 5 || size == 10) {
            areaSize = size;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                String label = size == 1 ? "Выкл (1x1)" : size + "x" + size;
                client.player.sendMessage(Text.literal("AreaBreaker: " + label), true);
            }
        }
    }
}