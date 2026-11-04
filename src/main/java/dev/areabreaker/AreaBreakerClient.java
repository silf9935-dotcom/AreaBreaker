package dev.areabreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
    private static float breakingProgress;
    private static int waitForServerTicks;
    private static boolean blockStarted;
    private static boolean wasAttackDown;
    private static boolean sessionStarted;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.areabreaker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                KeyBinding.Category.create(Identifier.of("areabreaker", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(AreaBreakerClient::tick);
    }

    private static void tick(MinecraftClient client) {
        while (menuKey.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(new AreaBreakerScreen());
        }
        if (client.player == null || client.world == null || client.getNetworkHandler() == null
                || client.interactionManager == null || !client.isInSingleplayer()) {
            clearQueue();
            wasAttackDown = false;
            return;
        }

        boolean attackDown = client.options.attackKey.isPressed();
        if (!attackDown) {
            if (wasAttackDown) {
                if (blockStarted && activePos != null)
                    sendAction(client, PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, activePos);
                clearQueue();
            }
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
            } else return;
        }

        if (activePos == null) {
            while (!queue.isEmpty()) {
                BlockPos candidate = queue.pollFirst();
                if (client.world.getBlockState(candidate).isAir()) continue;
                if (client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(candidate)) > 36.0) continue;
                activePos = candidate;
                breakingProgress = 0.0f;
                waitForServerTicks = 0;
                blockStarted = false;
                break;
            }
        }
        if (activePos == null) {
            if (queue.isEmpty()) clearQueue();
            return;
        }
        if (client.world.getBlockState(activePos).isAir()) {
            activePos = null;
            blockStarted = false;
            return;
        }
        if (!blockStarted) {
            sendAction(client, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, activePos);
            blockStarted = true;
        }
        if (waitForServerTicks > 0) {
            waitForServerTicks--;
            if (waitForServerTicks == 0) {
                activePos = null;
                blockStarted = false;
            }
            return;
        }
        float delta = client.world.getBlockState(activePos)
                .calcBlockBreakingDelta(client.player, client.world, activePos);
        if (delta <= 0.0f) {
            sendAction(client, PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, activePos);
            activePos = null;
            blockStarted = false;
            return;
        }
        breakingProgress += delta;
        if (breakingProgress >= 1.0f) {
            sendAction(client, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, activePos);
            waitForServerTicks = 3;
        }
    }

    private static void sendAction(MinecraftClient client, PlayerActionC2SPacket.Action action, BlockPos pos) {
        client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(action, pos, activeSide));
    }

    private static void fillQueue(BlockPos center, Direction side) {
        List<BlockPos> positions = new ArrayList<>();
        int low = -((areaSize - 1) / 2);
        for (int a = 0; a < areaSize; a++) for (int b = 0; b < areaSize; b++) {
            int u = low + a, v = low + b;
            BlockPos offset = switch (side.getAxis()) {
                case Y -> new BlockPos(u, 0, v);
                case X -> new BlockPos(0, u, v);
                case Z -> new BlockPos(u, v, 0);
            };
            positions.add(center.add(offset));
        }
        positions.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(center)));
        queue.addAll(positions);
    }

    static void setAreaSize(int size) {
        if (size == 2 || size == 3 || size == 4 || size == 5 || size == 10) {
            areaSize = size;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null)
                client.player.sendMessage(Text.literal("AreaBreaker: " + size + "x" + size), true);
        }
        clearQueue();
    }

    private static void clearQueue() {
        queue.clear();
        activePos = null;
        breakingProgress = 0.0f;
        waitForServerTicks = 0;
        blockStarted = false;
        sessionStarted = false;
    }
}