package dev.areabreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

public final class AreaBreakerClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static int areaSize = 3;
    private static final Deque<BlockPos> queue = new ArrayDeque<>();
    private static BlockPos activePos;
    private static Direction activeSide = Direction.UP;
    private static int activeTicks;
    private static boolean wasAttackDown;
    private static boolean sessionStarted;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.areabreaker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                "category.areabreaker"));
        ClientTickEvents.END_CLIENT_TICK.register(AreaBreakerClient::tick);
    }

    private static void tick(MinecraftClient client) {
        while (menuKey.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(new AreaBreakerScreen());
        }
        if (client.player == null || client.world == null || client.interactionManager == null
                || !client.isInSingleplayer()) {
            clearQueue();
            wasAttackDown = false;
            return;
        }

        boolean attackDown = client.options.attackKey.isPressed();
        if (!attackDown) {
            if (wasAttackDown) clearQueue();
            wasAttackDown = false;
            return;
        }
        wasAttackDown = true;

        if (!sessionStarted) {
            if (client.crosshairTarget instanceof BlockHitResult hit
                    && hit.getType() == HitResult.Type.BLOCK) {
                activeSide = hit.getSide();
                fillQueue(hit.getBlockPos(), activeSide);
                sessionStarted = true;
            } else {
                return;
            }
        }

        // Vanilla's interaction manager handles each block individually, preserving
        // normal mining speed, tool durability, drops, and block-breaking checks.
        if ( (activePos == null) && !queue.isEmpty()) {
            activePos = queue.pollFirst();
            activeTicks = 0;
            if (!client.interactionManager.attackBlock(activePos, activeSide)) {
                activePos = null;
                return;
            }
        }
        if (activePos == null) return;

        if (client.world.getBlockState(activePos).isAir()) {
            activePos = null;
            return;
        }
        if (++activeTicks > 2400) {
            // Give up on a block that cannot be mined (for example, an unreachable block).
            activePos = null;
            return;
        }
        client.interactionManager.updateBlockBreakingProgress(activePos, activeSide);
    }

    private static void fillQueue(BlockPos center, Direction side) {
        List<BlockPos> positions = new ArrayList<>();
        int low = -(areaSize / 2);
        for (int a = 0; a < areaSize; a++) {
            for (int b = 0; b < areaSize; b++) {
                int u = low + a;
                int v = low + b;
                Vec3i offset = switch (side.getAxis()) {
                    case Y -> new Vec3i(u, 0, v);
                    case X -> new Vec3i(0, u, v);
                    case Z -> new Vec3i(u, v, 0);
                };
                positions.add(center.add(offset));
            }
        }
        positions.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(center)));
        queue.addAll(positions);
    }

    static void setAreaSize(int size) {
        if (size == 2 || size == 3 || size == 4 || size == 5 || size == 10) {
            areaSize = size;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("AreaBreaker: " + size + "x" + size), true);
            }
        }
        clearQueue();
    }

    private static void clearQueue() {
        queue.clear();
        activePos = null;
        activeTicks = 0;
        sessionStarted = false;
    }
}
