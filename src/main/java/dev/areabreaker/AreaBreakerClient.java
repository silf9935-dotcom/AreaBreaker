package dev.areabreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AreaBreakerClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static int areaSize = 3;
    private static boolean wasAttackDown;
    private static boolean minedThisHold;

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

        if (client.player == null || client.world == null || !client.isInSingleplayer()) {
            wasAttackDown = false;
            minedThisHold = false;
            return;
        }

        boolean attackDown = client.options.attackKey.isPressed();
        if (!attackDown) {
            wasAttackDown = false;
            minedThisHold = false;
            return;
        }
        wasAttackDown = true;
        if (minedThisHold) return;

        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return;

        MinecraftServer server = client.getServer();
        if (server == null) return;

        BlockPos center = hit.getBlockPos().toImmutable();
        Direction side = hit.getSide();
        UUID playerId = client.player.getUuid();
        var dimension = client.world.getRegistryKey();
        List<BlockPos> positions = buildArea(center, side, areaSize);
        minedThisHold = true;

        server.execute(() -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(playerId);
            ServerWorld serverWorld = server.getWorld(dimension);
            if (serverPlayer == null || serverWorld == null) return;

            for (BlockPos pos : positions) {
                if (serverPlayer.isRemoved() || serverWorld.getBlockState(pos).isAir()) continue;
                if (serverPlayer.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > 36.0) continue;
                serverPlayer.interactionManager.tryBreakBlock(pos);
            }
        });
    }

    private static List<BlockPos> buildArea(BlockPos center, Direction side, int size) {
        List<BlockPos> positions = new ArrayList<>(size * size);
        int low = -(size / 2);
        for (int a = 0; a < size; a++) {
            for (int b = 0; b < size; b++) {
                int u = low + a;
                int v = low + b;
                BlockPos offset = switch (side.getAxis()) {
                    case Y -> new BlockPos(u, 0, v);
                    case X -> new BlockPos(0, u, v);
                    case Z -> new BlockPos(u, v, 0);
                };
                positions.add(center.add(offset));
            }
        }
        return positions;
    }

    static void setAreaSize(int size) {
        if (size == 2 || size == 3 || size == 4 || size == 5 || size == 10) {
            areaSize = size;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null)
                client.player.sendMessage(Text.literal("AreaBreaker: " + size + "x" + size), true);
        }
    }
}