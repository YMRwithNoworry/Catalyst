package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.alku.catalyst.config.CatalystConfig;

public class AutoWaterBucket {
    private enum State {
        IDLE,
        FALLING,
        NEED_PLACE_WATER,
        WATER_PLACED,
        COLLECTED
    }
    
    private static State state = State.IDLE;
    private static int waterBucketSlot = -1;
    private static int originalSelectedSlot = -1;
    private static ItemStack originalSlotItem = ItemStack.EMPTY;
    private static BlockPos waterPlacedPos = null;
    private static long lastActionTime = 0;
    private static boolean waterPlaced = false;
    private static float originalPitch = 0;
    private static float originalYaw = 0;
    private static double savedGroundHeight = 0;
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoWaterBucketEnabled) {
            reset();
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        
        switch (state) {
            case IDLE:
                handleIdle(mc, player);
                break;
            case FALLING:
                handleFalling(mc, player);
                break;
            case NEED_PLACE_WATER:
                handleNeedPlaceWater(mc, player);
                break;
            case WATER_PLACED:
                handleWaterPlaced(mc, player);
                break;
            case COLLECTED:
                handleCollected(mc, player);
                break;
        }
    }
    
    private static void handleIdle(Minecraft mc, LocalPlayer player) {
        if (player.onGround() || player.isInWater() || player.isShiftKeyDown()) {
            return;
        }
        
        boolean isFalling = player.getDeltaMovement().y < -0.1;
        boolean willTakeDamage = player.fallDistance > 3.0f;
        
        if (isFalling && willTakeDamage) {
            waterBucketSlot = findWaterBucket(player);
            if (waterBucketSlot >= 0) {
                originalSelectedSlot = player.getInventory().selected;
                originalSlotItem = player.getInventory().getItem(originalSelectedSlot).copy();
                originalPitch = player.getXRot();
                originalYaw = player.getYRot();
                
                player.getInventory().selected = waterBucketSlot;
                
                state = State.FALLING;
                waterPlaced = false;
            }
        }
    }
    
    private static void handleFalling(Minecraft mc, LocalPlayer player) {
        if (player.onGround() && !player.isInWater()) {
            if (!waterPlaced) {
                reset();
                return;
            }
            collectWaterAndRestore(mc, player);
            state = State.COLLECTED;
            lastActionTime = System.currentTimeMillis();
            return;
        }
        
        if (player.isInWater()) {
            collectWaterAndRestore(mc, player);
            state = State.COLLECTED;
            lastActionTime = System.currentTimeMillis();
            return;
        }
        
        if (!waterPlaced) {
            double groundHeight = findGroundHeight(mc);
            double currentHeight = player.getY();
            double fallDistance = currentHeight - groundHeight;
            
            if (fallDistance < 4 && fallDistance > 1) {
                savedGroundHeight = groundHeight;
                player.setXRot(90.0f);
                state = State.NEED_PLACE_WATER;
            }
        }
    }
    
    private static void handleNeedPlaceWater(Minecraft mc, LocalPlayer player) {
        if (player.onGround() && !player.isInWater()) {
            if (!waterPlaced) {
                reset();
                return;
            }
            collectWaterAndRestore(mc, player);
            state = State.COLLECTED;
            lastActionTime = System.currentTimeMillis();
            return;
        }
        
        if (player.isInWater()) {
            collectWaterAndRestore(mc, player);
            state = State.COLLECTED;
            lastActionTime = System.currentTimeMillis();
            return;
        }
        
        placeWaterAtGround(mc, player, savedGroundHeight);
        waterPlaced = true;
        waterPlacedPos = new BlockPos(player.getBlockX(), (int)savedGroundHeight - 1, player.getBlockZ());
        state = State.WATER_PLACED;
    }
    
    private static void handleWaterPlaced(Minecraft mc, LocalPlayer player) {
        if (player.isInWater() || player.onGround()) {
            collectWaterAndRestore(mc, player);
            state = State.COLLECTED;
            lastActionTime = System.currentTimeMillis();
        }
    }
    
    private static void handleCollected(Minecraft mc, LocalPlayer player) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime > 50) {
            restoreItems(player);
            reset();
        }
    }
    
    private static void placeWaterAtGround(Minecraft mc, LocalPlayer player, double groundHeight) {
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int y = (int) Math.floor(groundHeight) - 1;
        
        BlockPos placePos = new BlockPos(x, y, z);
        
        BlockHitResult hitResult = new BlockHitResult(
            new Vec3(x + 0.5, y + 1.0, z + 0.5),
            Direction.UP,
            placePos,
            false
        );
        
        InteractionResult result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        
        if (result == InteractionResult.PASS || result == InteractionResult.FAIL) {
            mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        }
    }
    
    private static void collectWaterAndRestore(Minecraft mc, LocalPlayer player) {
        int bucketSlot = findBucket(player);
        if (bucketSlot >= 0) {
            player.getInventory().selected = bucketSlot;
            
            BlockPos targetPos = waterPlacedPos != null ? waterPlacedPos : player.blockPosition().below();
            
            player.setXRot(90.0f);
            
            BlockHitResult hitResult = new BlockHitResult(
                new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5),
                Direction.UP,
                targetPos,
                false
            );
            
            mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        }
        
        player.setXRot(originalPitch);
        player.setYRot(originalYaw);
    }
    
    private static void restoreItems(LocalPlayer player) {
        if (waterBucketSlot < 0 || originalSelectedSlot < 0) {
            return;
        }
        
        int currentBucketSlot = findWaterBucket(player);
        
        if (currentBucketSlot >= 0 && currentBucketSlot != waterBucketSlot) {
            ItemStack bucketStack = player.getInventory().getItem(currentBucketSlot);
            ItemStack otherStack = player.getInventory().getItem(waterBucketSlot);
            
            player.getInventory().setItem(waterBucketSlot, bucketStack);
            player.getInventory().setItem(currentBucketSlot, otherStack);
        }
        
        player.getInventory().selected = originalSelectedSlot;
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
    
    private static int findBucket(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.BUCKET) {
                return i;
            }
        }
        return -1;
    }
    
    private static double findGroundHeight(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return player != null ? player.getY() : 0;
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
    
    private static void reset() {
        state = State.IDLE;
        waterBucketSlot = -1;
        originalSelectedSlot = -1;
        originalSlotItem = ItemStack.EMPTY;
        waterPlacedPos = null;
        waterPlaced = false;
        savedGroundHeight = 0;
    }
}
