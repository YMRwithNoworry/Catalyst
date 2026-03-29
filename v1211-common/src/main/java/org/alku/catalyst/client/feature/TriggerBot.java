package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.alku.catalyst.config.CatalystConfig;

public class TriggerBot {
    
    private static boolean preparingCrit = false;
    private static long lastHurtTime = 0;
    
    public static boolean isPreparingCrit() {
        return preparingCrit;
    }
    
    public static boolean wasAttackedRecently() {
        return System.currentTimeMillis() - lastHurtTime < 1000;
    }
    
    public static void onPlayerHurt(Player player) {
        if (player.isHurt() && player.hurtTime > 0) {
            lastHurtTime = System.currentTimeMillis();
        }
    }
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().triggerBotEnabled) {
            preparingCrit = false;
            return;
        }
        
        if (mc.player == null || mc.level == null || mc.screen != null) {
            preparingCrit = false;
            return;
        }

        onPlayerHurt(mc.player);

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            preparingCrit = false;
            return;
        }

        Entity target = ((EntityHitResult) hitResult).getEntity();
        boolean validLivingTarget = target instanceof LivingEntity && isValidTarget(mc.player, (LivingEntity) target);
        boolean validArmorStand = target instanceof ArmorStand && CatalystConfig.getInstance().triggerBotAttackArmorStands;
        
        if (!validLivingTarget && !validArmorStand) {
            preparingCrit = false;
            return;
        }

        CatalystConfig config = CatalystConfig.getInstance();
        
        boolean onCooldown = mc.player.getAttackStrengthScale(0.0F) < 1.0F;
        boolean wasHurt = wasAttackedRecently();
        
        if (onCooldown && config.triggerBotMode == 1 && wasHurt) {
            preparingCrit = false;
            performAttack(mc, target);
            return;
        }
        
        if (onCooldown) {
            return;
        }
        
        if (config.triggerBotMode == 1) {
            if (mc.player.onGround()) {
                if (mc.player.isSprinting()) {
                    mc.player.setSprinting(false);
                    preparingCrit = true;
                } else {
                    preparingCrit = false;
                    performAttack(mc, target);
                }
            } else if (canCritPhysics(mc.player)) {
                if (mc.player.isSprinting()) {
                    mc.player.setSprinting(false);
                    preparingCrit = true;
                } else {
                    preparingCrit = false;
                    performAttack(mc, target);
                }
            } else {
                preparingCrit = false;
            }
        } else {
            preparingCrit = false;
            performAttack(mc, target);
        }
    }

    private static boolean canCritPhysics(Player player) {
        return !player.onGround()
            && !player.onClimbable()
            && !player.isInWater()
            && !player.isPassenger()
            && player.getDeltaMovement().y < 0;
    }

    private static void performAttack(Minecraft mc, Entity target) {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        mc.player.resetAttackStrengthTicker();
    }

    private static boolean isValidTarget(Player player, LivingEntity target) {
        if (target == player) return false;
        if (!target.isAlive()) return false;
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (target.isInvisible() && !config.triggerBotAttackInvisible) {
            return false;
        }
        
        if (target instanceof Player) return config.triggerBotAttackPlayers;
        if (target instanceof Enemy) return config.triggerBotAttackMonsters;
        if (target instanceof Animal) return config.triggerBotAttackAnimals;
        
        return false;
    }
}
