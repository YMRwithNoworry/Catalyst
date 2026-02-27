package org.alku.catalyst.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.client.feature.*;
import org.alku.catalyst.client.gui.CatalystConfigScreen;
import org.alku.catalyst.client.gui.KeybindManager;
import org.alku.catalyst.config.CatalystConfig;
import org.lwjgl.glfw.GLFW;

public class ClientEventHandler {
    private static int[] prevKeyStates = new int[8];
    
    public static void init() {
        CatalystKeys.register();
        
        for (int i = 0; i < prevKeyStates.length; i++) {
            prevKeyStates[i] = GLFW.GLFW_RELEASE;
        }
        
        ClientTickEvent.CLIENT_PRE.register(ClientEventHandler::onClientTick);
    }
    
    private static void onClientTick(Minecraft mc) {
        handleKeyInputs(mc);
        
        AutoSprint.tick(mc);
        AutoSwim.tick(mc);
        GammaOverride.tick(mc);
        AutoTool.tick(mc);
        AutoWeapon.tick(mc);
        AutoDoor.tick(mc);
        AutoWaterBucket.tick(mc);
    }
    
    private static void handleKeyInputs(Minecraft mc) {
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (checkKeybind(mc, "open_config", 0)) {
            mc.setScreen(new CatalystConfigScreen(null));
        }
        
        if (checkKeybind(mc, "toggle_auto_sprint", 1)) {
            config.autoSprintEnabled = !config.autoSprintEnabled;
            config.save();
            sendFeedback(mc, "auto_sprint", config.autoSprintEnabled);
        }
        
        if (checkKeybind(mc, "toggle_auto_swim", 2)) {
            config.autoSwimEnabled = !config.autoSwimEnabled;
            config.save();
            sendFeedback(mc, "auto_swim", config.autoSwimEnabled);
        }
        
        if (checkKeybind(mc, "toggle_gamma_override", 3)) {
            config.gammaOverrideEnabled = !config.gammaOverrideEnabled;
            config.save();
            if (!config.gammaOverrideEnabled) {
                GammaOverride.onDisable(mc);
            }
            sendFeedback(mc, "gamma_override", config.gammaOverrideEnabled);
        }
        
        if (checkKeybind(mc, "toggle_auto_tool", 4)) {
            config.autoToolEnabled = !config.autoToolEnabled;
            config.save();
            sendFeedback(mc, "auto_tool", config.autoToolEnabled);
        }
        
        if (checkKeybind(mc, "toggle_auto_weapon", 5)) {
            config.autoWeaponEnabled = !config.autoWeaponEnabled;
            config.save();
            sendFeedback(mc, "auto_weapon", config.autoWeaponEnabled);
        }
        
        if (checkKeybind(mc, "toggle_auto_door", 6)) {
            config.autoDoorEnabled = !config.autoDoorEnabled;
            config.save();
            sendFeedback(mc, "auto_door", config.autoDoorEnabled);
        }
        
        if (checkKeybind(mc, "toggle_auto_water_bucket", 7)) {
            config.autoWaterBucketEnabled = !config.autoWaterBucketEnabled;
            config.save();
            sendFeedback(mc, "auto_water_bucket", config.autoWaterBucketEnabled);
        }
    }
    
    private static boolean checkKeybind(Minecraft mc, String keyName, int index) {
        int keyCode = KeybindManager.getKey(keyName);
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        
        long window = mc.getWindow().getWindow();
        int currentState = GLFW.glfwGetKey(window, keyCode);
        
        boolean pressed = (currentState == GLFW.GLFW_PRESS) && (prevKeyStates[index] == GLFW.GLFW_RELEASE);
        prevKeyStates[index] = currentState;
        
        return pressed;
    }
    
    private static void sendFeedback(Minecraft mc, String feature, boolean enabled) {
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        
        String status = enabled ? "enabled" : "disabled";
        String message = String.format("§6[Catalyst] §f%s §%s", 
            Component.translatable("catalyst.feature." + feature).getString(),
            enabled ? "a" + status : "c" + status);
        player.displayClientMessage(Component.literal(message), true);
    }
}
