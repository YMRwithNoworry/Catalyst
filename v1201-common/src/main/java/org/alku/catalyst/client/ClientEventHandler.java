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
import org.alku.catalyst.client.gui.AddWaypointScreen;
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
            ShieldBreaker.onAttack(mc);
            AutoWeapon.checkAndSwitch(mc);
            AutoTool.checkAndSwitch(mc);
        }
        return EventResult.pass();
    }
    
    private static EventResult onMouseScrolled(Minecraft mc, double amount) {
        if (mc.screen instanceof CatalystConfigScreen) {
            CatalystConfig config = CatalystConfig.getInstance();
            float oldScale = config.guiScale;
            config.guiScale += (float) amount * 0.1f;
            config.guiScale = Math.max(0.5f, Math.min(2.0f, config.guiScale));
            
            if (oldScale != config.guiScale) {
                config.save();
                mc.setScreen(new CatalystConfigScreen(null));
            }
            return EventResult.interruptDefault();
        }
        return MouseTweaks.onMouseScrolled(mc, amount);
    }
    
    private static void onClientTick(Minecraft mc) {
        handleKeyInputs(mc);
        
        AutoSprint.tick(mc);
        AutoSwim.tick(mc);
        GammaOverride.tick(mc);
        TriggerBot.tick(mc);
        ShieldBreaker.tick(mc);
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

        if (CatalystKeys.TOGGLE_TRIGGER_BOT.consumeClick()) {
            config.triggerBotEnabled = !config.triggerBotEnabled;
            config.save();
            sendFeedback(mc, "trigger_bot", config.triggerBotEnabled);
        }

        if (CatalystKeys.TOGGLE_SHIELD_BREAKER.consumeClick()) {
            config.shieldBreakerEnabled = !config.shieldBreakerEnabled;
            config.save();
            sendFeedback(mc, "shield_breaker", config.shieldBreakerEnabled);
        }

        if (CatalystKeys.TOGGLE_FAST_SHIELD.consumeClick()) {
            config.fastShieldEnabled = !config.fastShieldEnabled;
            config.save();
            sendFeedback(mc, "fast_shield", config.fastShieldEnabled);
        }

        if (CatalystKeys.TOGGLE_AUTO_DOOR.consumeClick()) {
            config.autoDoorEnabled = !config.autoDoorEnabled;
            config.save();
            sendFeedback(mc, "auto_door", config.autoDoorEnabled);
        }

        if (CatalystKeys.TOGGLE_AUTO_WATER_BUCKET.consumeClick()) {
            config.autoWaterBucketEnabled = !config.autoWaterBucketEnabled;
            config.save();
            sendFeedback(mc, "auto_water_bucket", config.autoWaterBucketEnabled);
        }
        
        if (CatalystKeys.SORT_INVENTORY.consumeClick()) {
            InventorySorter.sortCurrentContainer(mc);
        }
        
        if (CatalystKeys.TOGGLE_ENTITY_XRAY.consumeClick()) {
            config.entityXrayEnabled = !config.entityXrayEnabled;
            config.save();
            sendFeedback(mc, "entity_xray", config.entityXrayEnabled);
        }
        
        if (CatalystKeys.TOGGLE_MINI_HUD.consumeClick()) {
            config.miniHudEnabled = !config.miniHudEnabled;
            config.save();
            sendFeedback(mc, "mini_hud", config.miniHudEnabled);
        }

        if (CatalystKeys.TOGGLE_INVENTORY_SORTER.consumeClick()) {
            config.inventorySorterEnabled = !config.inventorySorterEnabled;
            config.save();
            sendFeedback(mc, "inventory_sorter", config.inventorySorterEnabled);
        }

        if (CatalystKeys.TOGGLE_MOUSE_GESTURES.consumeClick()) {
            config.rmbTweak = !config.rmbTweak;
            config.save();
            sendFeedback(mc, "mouse_gestures", config.rmbTweak);
        }

        if (CatalystKeys.OPEN_WAYPOINTS.consumeClick()) {
            mc.setScreen(new WaypointScreen());
        }

        if (CatalystKeys.ADD_WAYPOINT.consumeClick()) {
            if (mc.player != null && mc.level != null) {
                BlockPos pos = mc.player.blockPosition();
                int color = (int)(Math.random() * 0xFFFFFF);
                mc.setScreen(new AddWaypointScreen(pos, mc.level.dimension(), color));
            }
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
