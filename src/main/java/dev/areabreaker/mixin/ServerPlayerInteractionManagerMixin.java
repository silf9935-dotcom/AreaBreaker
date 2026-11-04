package dev.areabreaker.mixin;

import dev.areabreaker.AreaBreakerSettings;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    @Unique private boolean areabreaker$breakingArea;
    @Unique private Block areabreaker$centerBlock;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void areabreaker$captureOriginalBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!areabreaker$breakingArea) areabreaker$centerBlock = world.getBlockState(pos).getBlock();
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void areabreaker$afterBlockBroken(BlockPos center, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || areabreakingInvalid()) return;
        AreaBreakerSettings.recordMined(areabreaker$centerBlock);
        if (areabreaker$breakingArea || !AreaBreakerSettings.isEnabled() || player.isRemoved()) return;

        int size = AreaBreakerSettings.getAreaSize();
        AreaBreakerSettings.Mode mode = AreaBreakerSettings.getMode();
        if (mode == AreaBreakerSettings.Mode.AREA && size <= 1) return;

        HitResult result = player.raycast(6.0, 1.0F, false);
        Direction side = result instanceof BlockHitResult hit && result.getType() == HitResult.Type.BLOCK
                ? hit.getSide() : player.getHorizontalFacing().getOpposite();

        areabreaker$breakingArea = true;
        try {
            if (mode == AreaBreakerSettings.Mode.VEIN) areabreaker$breakVein(center, areabreaker$centerBlock);
            else areabreaker$breakShape(center, side, size);
            if (AreaBreakerSettings.isAutoPickupEnabled()) areabreaker$collectNearbyDrops(center, size + 3);
        } finally {
            areabreaker$breakingArea = false;
            AreaBreakerSettings.saveNow();
        }
    }

    @Unique
    private boolean areabreakingInvalid() {
        return areabreaker$centerBlock == null;
    }

    @Unique
    private void areabreaker$breakVein(BlockPos center, Block targetBlock) {
        if (targetBlock == null || !AreaBreakerSettings.isBlockAllowed(targetBlock)) return;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(center);
        visited.add(center);
        int broken = 0;
        while (!queue.isEmpty() && broken < 64) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.offset(direction);
                if (!visited.add(next) || next.getManhattanDistance(center) > 6) continue;
                if (world.getBlockState(next).getBlock() != targetBlock || !areabreaker$inReach(next)
                        || !AreaBreakerSettings.isBlockAllowed(targetBlock) || !areabreaker$hasToolDurability()) continue;
                if (((ServerPlayerInteractionManager)(Object)this).tryBreakBlock(next)) {
                    broken++;
                    AreaBreakerSettings.recordMined(targetBlock);
                }
                if (broken >= 64) break;
                queue.addLast(next);
            }
        }
    }

    @Unique
    private void areabreaker$breakShape(BlockPos center, Direction side, int size) {
        int low = -(size / 2);
        Direction facing = player.getHorizontalFacing();
        Direction widthDirection = facing.rotateYClockwise();
        int depth = AreaBreakerSettings.getShape() == AreaBreakerSettings.Shape.TUNNEL ? 3 : 1;
        for (int d = 0; d < depth; d++) {
            for (int a = 0; a < size; a++) {
                for (int b = 0; b < size; b++) {
                    int u = low + a;
                    int v = low + b;
                    BlockPos offset;
                    switch (AreaBreakerSettings.getShape()) {
                        case WALL -> offset = new BlockPos(widthDirection.getOffsetX() * u, v, widthDirection.getOffsetZ() * u);
                        case STRIP -> offset = switch (side.getAxis()) {
                            case Y -> new BlockPos(u, 0, 0);
                            case X -> new BlockPos(0, 0, u);
                            case Z -> new BlockPos(u, 0, 0);
                        };
                        case TUNNEL -> {
                            BlockPos plane = areabreaker$squareOffset(side, u, v);
                            offset = plane.add(facing.getOffsetX() * d, 0, facing.getOffsetZ() * d);
                        }
                        default -> offset = areabreaker$squareOffset(side, u, v);
                    }
                    BlockPos target = center.add(offset);
                    if (target.equals(center) || world.getBlockState(target).isAir()) continue;
                    Block block = world.getBlockState(target).getBlock();
                    if (!AreaBreakerSettings.isBlockAllowed(block) || !areabreaker$inReach(target) || !areabreaker$hasToolDurability()) continue;
                    if (((ServerPlayerInteractionManager)(Object)this).tryBreakBlock(target)) AreaBreakerSettings.recordMined(block);
                }
            }
        }
    }

    @Unique
    private BlockPos areabreaker$squareOffset(Direction side, int u, int v) {
        return switch (side.getAxis()) {
            case Y -> new BlockPos(u, 0, v);
            case X -> new BlockPos(0, u, v);
            case Z -> new BlockPos(u, v, 0);
        };
    }

    @Unique
    private boolean areabreaker$inReach(BlockPos pos) {
        return player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) <= 36.0;
    }

    @Unique
    private boolean areabreaker$hasToolDurability() {
        if (!AreaBreakerSettings.isDurabilityProtectionEnabled()) return true;
        ItemStack tool = player.getMainHandStack();
        return !tool.isDamageable() || tool.getDamage() < tool.getMaxDamage() - 1;
    }

    @Unique
    private void areabreaker$collectNearbyDrops(BlockPos center, int radius) {
        Box area = new Box(center).expand(radius);
        for (ItemEntity entity : world.getEntitiesByClass(ItemEntity.class, area, ItemEntity::isAlive)) {
            ItemStack stack = entity.getStack();
            if (stack.isEmpty()) continue;
            if (AreaBreakerSettings.isAutoSmeltEnabled()) {
                if (stack.isOf(Items.RAW_IRON)) stack = new ItemStack(Items.IRON_INGOT, stack.getCount());
                else if (stack.isOf(Items.RAW_GOLD)) stack = new ItemStack(Items.GOLD_INGOT, stack.getCount());
                else if (stack.isOf(Items.RAW_COPPER)) stack = new ItemStack(Items.COPPER_INGOT, stack.getCount());
                entity.setStack(stack);
            }
            player.getInventory().insertStack(stack);
            if (stack.isEmpty()) entity.discard();
            else entity.setStack(stack);
        }
    }
}
