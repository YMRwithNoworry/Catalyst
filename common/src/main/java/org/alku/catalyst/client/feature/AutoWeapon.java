package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import org.alku.catalyst.config.CatalystConfig;

public class AutoWeapon {
    private static int originalSlot = -1;
    private static boolean wasAttacking = false;
    private static long lastSwitchTime = 0;
    private static final long RESTORE_DELAY = 500;
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoWeaponEnabled) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        
        boolean isAttacking = mc.options.keyAttack.isDown();
        
        if (isAttacking) {
            if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                Entity target = entityHitResult.getEntity();
                if (target instanceof LivingEntity) {
                    if (!wasAttacking) {
                        originalSlot = player.getInventory().selected;
                    }
                    
                    int bestSlot = findBestWeaponSlot(player);
                    if (bestSlot >= 0 && bestSlot != player.getInventory().selected) {
                        player.getInventory().selected = bestSlot;
                        lastSwitchTime = System.currentTimeMillis();
                    }
                }
            }
            wasAttacking = true;
        } else {
            if (wasAttacking && originalSlot >= 0) {
                player.getInventory().selected = originalSlot;
                originalSlot = -1;
            }
            wasAttacking = false;
        }
    }
    
    private static int findBestWeaponSlot(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        int bestSlot = inventory.selected;
        double bestDamage = getWeaponDamage(player.getMainHandItem());
        
        for (int i = 0; i < 9; i++) {
            if (i == inventory.selected) {
                continue;
            }
            
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            
            double damage = getWeaponDamage(stack);
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
    
    private static double getWeaponDamage(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem sword) {
            return sword.getDamage();
        }
        if (stack.getItem() instanceof TieredItem tiered) {
            return tiered.getTier().getAttackDamageBonus();
        }
        return 0.0;
    }
}
