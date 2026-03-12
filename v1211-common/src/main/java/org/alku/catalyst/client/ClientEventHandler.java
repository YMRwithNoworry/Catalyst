package org.alku.catalyst.client;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.client.feature.*;
import org.alku.catalyst.client.gui.CatalystConfigScreen;
import org.alku.catalyst.client.gui.WaypointScreen;
import org.alku.catalyst.client.waypoint.Waypoint;
import org.alku.catalyst.client.waypoint.WaypointManager;
import org.alku.catalyst.config.CatalystConfig;
import org.lwjgl.glfw.GLFW;

public class ClientEventHandler {
    
    public static void init() {
        CatalystKeys.register();
        
        ClientTickEvent.CLIENT_PRE.register(ClientEventHandler::onClientTick);
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register(ClientEventHandler::onMouseClicked);
        ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEventHandler::onMouseScrolled);
    }
    
    private static EventResult onMouseClicked(Minecraft mc, int button, int action, int mods) {
        EventResult mouseTweaksResult = MouseTweaks.onMouseClicked(mc, button, action, mods);
        if (mouseTweaksResult == EventResult.interruptDefault()) {
            return mouseTweaksResult;
        }
        
        if (button == 0 && action == 1) {
            AutoWeapon.checkAndSwitch(mc);
            AutoTool.checkAndSwitch(mc);
        }
        return EventResult.pass();
    }
    
    private static EventResult onMouseScrolled(Minecraft mc, double amount, double amount2) {
        return MouseTweaks.onMouseScrolled(mc, amount);
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
        InventorySorter.tick(mc);
        MouseTweaks.tick(mc);
    }
    
    private static void handleKeyInputs(Minecraft mc) {
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (CatalystKeys.OPEN_CONFIG.consumeClick()) {
            mc.setScreen(new CatalystConfigScreen(null));
        }
        
        if (CatalystKeys.TOGGLE_AUTO_SPRINT.consumeClick()) {
            config.autoSprintEnabled = !config.autoSprintEnabled;
            config.save();
            sendFeedback(mc, "auto_sprint", config.autoSprintEnabled);
        }
        
        if (CatalystKeys.TOGGLE_AUTO_SWIM.consumeClick()) {
            config.autoSwimEnabled = !config.autoSwimEnabled;
            config.save();
            sendFeedback(mc, "auto_swim", config.autoSwimEnabled);
        }
        
        if (CatalystKeys.TOGGLE_GAMMA_OVERRIDE.consumeClick()) {
            config.gammaOverrideEnabled = !config.gammaOverrideEnabled;
            config.save();
            if (!config.gammaOverrideEnabled) {
                GammaOverride.onDisable(mc);
            }
            sendFeedback(mc, "gamma_override", config.gammaOverrideEnabled);
        }
        
        if (CatalystKeys.TOGGLE_AUTO_TOOL.consumeClick()) {
            config.autoToolEnabled = !config.autoToolEnabled;
            config.save();
            sendFeedback(mc, "auto_tool", config.autoToolEnabled);
        }
        
        if (CatalystKeys.TOGGLE_AUTO_WEAPON.consumeClick()) {
            config.autoWeaponEnabled = !config.autoWeaponEnabled;
            config.save();
            sendFeedback(mc, "auto_weapon", config.autoWeaponEnabled);
        }
        
        if (CatalystKeys.SORT_INVENTORY.consumeClick()) {
            InventorySorter.sortCurrentContainer(mc);
        }
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
