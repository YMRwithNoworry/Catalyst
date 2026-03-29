package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.alku.catalyst.config.CatalystConfig;

public class ShieldBreaker {
    
    private static int breakDelay = 0;
    private static int previousSlot = -1;
    private static boolean isBreaking = false;
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().shieldBreakerEnabled) {
            reset();
            return;
        }
        
        if (mc.player == null || mc.level == null || mc.screen != null) {
            reset();
            return;
        }
        
        if (breakDelay > 0) {
            breakDelay--;
            return;
        }
        
        if (isBreaking) {
            restoreSlot(mc);
            return;
        }
        
        if (!CatalystConfig.getInstance().shieldBreakerOnlyOnTarget) {
            return;
        }
        
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }
        
        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return;
        }
        
        if (!(entityHitResult.getEntity() instanceof Player targetPlayer)) {
            return;
        }
        
        if (targetPlayer == mc.player) {
            return;
        }
        
        if (!targetPlayer.isBlocking()) {
            return;
        }
        
        switchToAxe(mc);
    }
    
    public static void onAttack(Minecraft mc) {
        if (!CatalystConfig.getInstance().shieldBreakerEnabled) {
            return;
        }
        
        if (mc.player == null || mc.level == null) {
            return;
        }
        
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }
        
        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return;
        }
        
        if (!(entityHitResult.getEntity() instanceof Player targetPlayer)) {
            return;
        }
        
        if (targetPlayer == mc.player) {
            return;
        }
        
        if (!targetPlayer.isBlocking()) {
            return;
        }
        
        if (CatalystConfig.getInstance().shieldBreakerOnlyOnTarget) {
            return;
        }
        
        switchToAxe(mc);
    }
    
    private static void switchToAxe(Minecraft mc) {
        if (mc.player == null) return;
        
        int axeSlot = findAxeSlot(mc);
        if (axeSlot == -1) return;
        
        int currentSlot = mc.player.getInventory().selected;
        if (currentSlot == axeSlot) return;
        
        previousSlot = currentSlot;
        mc.player.getInventory().selected = axeSlot;
        isBreaking = true;
        breakDelay = CatalystConfig.getInstance().shieldBreakerDelay;
    }
    
    private static void restoreSlot(Minecraft mc) {
        if (mc.player == null || previousSlot == -1) {
            isBreaking = false;
            return;
        }
        
        mc.player.getInventory().selected = previousSlot;
        previousSlot = -1;
        isBreaking = false;
    }
    
    private static int findAxeSlot(Minecraft mc) {
        if (mc.player == null) return -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }
    
    private static void reset() {
        breakDelay = 0;
        previousSlot = -1;
        isBreaking = false;
    }
    
    public static boolean isBreaking() {
        return isBreaking;
    }
}
