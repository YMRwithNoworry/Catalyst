package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.alku.catalyst.config.CatalystConfig;

public class AutoWaterBucket {
    private static boolean wasFalling = false;
    private static int lastWaterSlot = -1;
    private static long lastPlaceTime = 0;
    private static final long COOLDOWN = 1000;
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoWaterBucketEnabled) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        
        boolean isFalling = player.getDeltaMovement().y < -0.5;
        boolean isHighFall = player.fallDistance > 3.0f;
        
        if (isFalling && isHighFall) {
            if (!wasFalling) {
                lastWaterSlot = findWaterBucket(player);
            }
            
            if (lastWaterSlot >= 0) {
                double groundHeight = findGroundHeight(mc);
                double currentHeight = player.getY();
                double fallDistance = currentHeight - groundHeight;
                
                if (fallDistance < 5 && fallDistance > 1) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPlaceTime > COOLDOWN) {
                        placeWaterBucket(mc, player);
                        lastPlaceTime = currentTime;
                    }
                }
            }
            
            wasFalling = true;
        } else {
            wasFalling = false;
        }
    }
    
    private static int findWaterBucket(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                return i;
            }
        }
        return -1;
    }
    
    private static double findGroundHeight(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return player.getY();
        }
        
        BlockPos pos = player.blockPosition();
        
        for (int y = pos.getY(); y > mc.level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = mc.level.getBlockState(checkPos);
            
            if (!state.isAir() && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA)) {
                return y + 1;
            }
        }
        
        return mc.level.getMinBuildHeight();
    }
    
    private static void placeWaterBucket(Minecraft mc, LocalPlayer player) {
        int originalSlot = player.getInventory().selected;
        
        player.getInventory().selected = lastWaterSlot;
        
        BlockPos placePos = player.blockPosition().below();
        
        BlockHitResult hitResult = new BlockHitResult(
            Vec3.atCenterOf(placePos),
            Direction.UP,
            placePos,
            false
        );
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        
        player.getInventory().selected = originalSlot;
    }
}
