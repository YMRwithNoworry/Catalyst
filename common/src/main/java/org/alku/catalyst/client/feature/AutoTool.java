package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.alku.catalyst.config.CatalystConfig;

public class AutoTool {
    private static int originalSlot = -1;
    private static BlockPos lastBlockPos = null;
    private static boolean wasBreaking = false;
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoToolEnabled) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        
        boolean isBreaking = mc.options.keyAttack.isDown();
        
        if (isBreaking) {
            if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = mc.level.getBlockState(pos);
                
                if (!state.isAir()) {
                    if (!wasBreaking) {
                        originalSlot = player.getInventory().selected;
                    }
                    
                    if (!pos.equals(lastBlockPos)) {
                        int bestSlot = findBestToolSlot(player, state);
                        if (bestSlot >= 0) {
                            player.getInventory().selected = bestSlot;
                        }
                        lastBlockPos = pos;
                    }
                }
            }
            wasBreaking = true;
        } else {
            if (wasBreaking && originalSlot >= 0) {
                player.getInventory().selected = originalSlot;
                originalSlot = -1;
                lastBlockPos = null;
            }
            wasBreaking = false;
        }
    }
    
    private static int findBestToolSlot(LocalPlayer player, BlockState state) {
        Inventory inventory = player.getInventory();
        int bestSlot = inventory.selected;
        float bestSpeed = player.getMainHandItem().getDestroySpeed(state);
        
        for (int i = 0; i < 9; i++) {
            if (i == inventory.selected) {
                continue;
            }
            
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
}
