package org.alku.catalyst.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class CatalystKeys {
    public static final String KEY_CATEGORY = "key.categories.catalyst";

    public static KeyMapping TOGGLE_AUTO_SPRINT;
    public static KeyMapping TOGGLE_AUTO_SWIM;
    public static KeyMapping TOGGLE_GAMMA_OVERRIDE;
    public static KeyMapping TOGGLE_AUTO_TOOL;
    public static KeyMapping TOGGLE_AUTO_WEAPON;
    public static KeyMapping TOGGLE_ENTITY_XRAY;
    public static KeyMapping TOGGLE_MINI_HUD;
    public static KeyMapping OPEN_CONFIG;
    public static KeyMapping SORT_INVENTORY;
    public static KeyMapping OPEN_WAYPOINTS;
    public static KeyMapping ADD_WAYPOINT;

    public static void register() {
        TOGGLE_AUTO_SPRINT = new KeyMapping(
            "key.catalyst.toggle_auto_sprint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        TOGGLE_AUTO_SWIM = new KeyMapping(
            "key.catalyst.toggle_auto_swim",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        TOGGLE_GAMMA_OVERRIDE = new KeyMapping(
            "key.catalyst.toggle_gamma_override",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        TOGGLE_AUTO_TOOL = new KeyMapping(
            "key.catalyst.toggle_auto_tool",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        TOGGLE_AUTO_WEAPON = new KeyMapping(
            "key.catalyst.toggle_auto_weapon",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        TOGGLE_ENTITY_XRAY = new KeyMapping(
            "key.catalyst.toggle_entity_xray",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        TOGGLE_MINI_HUD = new KeyMapping(
            "key.catalyst.toggle_mini_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
        );

        OPEN_CONFIG = new KeyMapping(
            "key.catalyst.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KEY_CATEGORY
        );

        SORT_INVENTORY = new KeyMapping(
            "key.catalyst.sort_inventory",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY
        );

        OPEN_WAYPOINTS = new KeyMapping(
            "key.catalyst.open_waypoints",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY
        );

        ADD_WAYPOINT = new KeyMapping(
            "key.catalyst.add_waypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            KEY_CATEGORY
        );

        KeyMappingRegistry.register(TOGGLE_AUTO_SPRINT);
        KeyMappingRegistry.register(TOGGLE_AUTO_SWIM);
        KeyMappingRegistry.register(TOGGLE_GAMMA_OVERRIDE);
        KeyMappingRegistry.register(TOGGLE_AUTO_TOOL);
        KeyMappingRegistry.register(TOGGLE_AUTO_WEAPON);
        KeyMappingRegistry.register(TOGGLE_ENTITY_XRAY);
        KeyMappingRegistry.register(TOGGLE_MINI_HUD);
        KeyMappingRegistry.register(OPEN_CONFIG);
        KeyMappingRegistry.register(SORT_INVENTORY);
        KeyMappingRegistry.register(OPEN_WAYPOINTS);
        KeyMappingRegistry.register(ADD_WAYPOINT);
    }
}
