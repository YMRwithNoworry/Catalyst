package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.alku.catalyst.config.CatalystConfig;

public class AutoDoor {
    private static long lastToggleTime = 0;
    private static final long COOLDOWN = 500;
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoDoorEnabled) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime < COOLDOWN) {
            return;
        }
        
        BlockPos playerPos = player.blockPosition();
        
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos checkPos = playerPos.relative(direction);
            BlockState state = mc.level.getBlockState(checkPos);
            
            if (state.getBlock() instanceof DoorBlock) {
                boolean isOpen = state.getValue(DoorBlock.OPEN);
                Vec3 playerEyePos = player.getEyePosition();
                Vec3 doorCenter = Vec3.atCenterOf(checkPos);
                
                double distance = playerEyePos.distanceTo(doorCenter);
                
                if (distance < 2.5) {
                    boolean shouldOpen = isPlayerApproachingDoor(player, checkPos, direction);
                    
                    if (shouldOpen != isOpen) {
                        BlockHitResult hitResult = new BlockHitResult(
                            Vec3.atCenterOf(checkPos),
                            direction.getOpposite(),
                            checkPos,
                            false
                        );
                        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
                        lastToggleTime = currentTime;
                        return;
                    }
                }
            }
        }
    }
    
    private static boolean isPlayerApproachingDoor(LocalPlayer player, BlockPos doorPos, Direction doorDirection) {
        Vec3 playerPos = player.getEyePosition();
        Vec3 doorCenter = Vec3.atCenterOf(doorPos);
        
        Vec3 toDoor = doorCenter.subtract(playerPos).normalize();
        Vec3 playerLook = player.getLookAngle();
        
        double dot = toDoor.dot(playerLook);
        
        return dot > 0.5;
    }
}
