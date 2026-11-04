package dev.areabreaker.mixin;

import dev.areabreaker.AreaBreakerClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow @Final protected ServerWorld world;

    @Unique private boolean areabreaker$breakingArea;

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void areabreaker$afterBlockBroken(BlockPos center, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || areabreaker$breakingArea) return;
        int size = AreaBreakerClient.getAreaSize();
        if (size <= 1 || player.isRemoved()) return;

        HitResult result = player.raycast(6.0, 1.0F, false);
        if (!(result instanceof BlockHitResult hit) || result.getType() != HitResult.Type.BLOCK) return;
        Direction side = hit.getSide();
        int low = -(size / 2);
        areabreaker$breakingArea = true;
        try {
            for (int a = 0; a < size; a++) {
                for (int b = 0; b < size; b++) {
                    int u = low + a;
                    int v = low + b;
                    BlockPos offset = switch (side.getAxis()) {
                        case Y -> new BlockPos(u, 0, v);
                        case X -> new BlockPos(0, u, v);
                        case Z -> new BlockPos(u, v, 0);
                    };
                    BlockPos target = center.add(offset);
                    if (target.equals(center) || world.getBlockState(target).isAir()) continue;
                    if (player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(target)) > 36.0) continue;
                    ((ServerPlayerInteractionManager)(Object)this).tryBreakBlock(target);
                }
            }
        } finally {
            areabreaker$breakingArea = false;
        }
    }
}