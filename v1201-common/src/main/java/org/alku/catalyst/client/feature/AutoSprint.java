package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import org.alku.catalyst.config.CatalystConfig;

public class AutoSprint {
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoSprintEnabled) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        
        if (shouldSprint(player, mc)) {
            if (!player.isSprinting()) {
                player.setSprinting(true);
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                }
            }
        }
    }
    
    private static boolean shouldSprint(LocalPlayer player, Minecraft mc) {
        if (player.isSprinting()) {
            return true;
        }
        
        if (player.isShiftKeyDown()) {
            return false;
        }
        
        if (player.isSwimming()) {
            return false;
        }
        
        if (player.getFoodData().getFoodLevel() <= 6) {
            return false;
        }
        
        if (player.horizontalCollision) {
            return false;
        }
        
        if (mc.options.keyUp.isDown()) {
            return true;
        }
        
        return false;
    }
}
