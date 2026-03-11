package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import org.alku.catalyst.config.CatalystConfig;

public class AutoSwim {
    
    public static void tick(Minecraft mc) {
        if (!CatalystConfig.getInstance().autoSwimEnabled) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        
        if (shouldSwim(player, mc)) {
            if (!player.isSwimming()) {
                player.setSwimming(true);
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                }
            }
        }
    }
    
    private static boolean shouldSwim(LocalPlayer player, Minecraft mc) {
        if (player.isSwimming()) {
            return true;
        }
        
        if (!player.isInWater()) {
            return false;
        }
        
        if (player.isShiftKeyDown()) {
            return false;
        }
        
        if (mc.options.keyUp.isDown() || mc.options.keyJump.isDown()) {
            return true;
        }
        
        return false;
    }
}
