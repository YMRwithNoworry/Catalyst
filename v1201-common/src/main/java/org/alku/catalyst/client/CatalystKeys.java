package org.alku.catalyst.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.alku.catalyst.client.gui.KeybindManager;
import org.lwjgl.glfw.GLFW;

public class CatalystKeys {
    public static final String KEY_CATEGORY = "key.categories.catalyst";

    public static KeyMapping TOGGLE_AUTO_SPRINT;
    public static KeyMapping TOGGLE_AUTO_SWIM;
    public static KeyMapping TOGGLE_GAMMA_OVERRIDE;
    public static KeyMapping TOGGLE_AUTO_TOOL;
    public static KeyMapping TOGGLE_AUTO_WEAPON;
    public static KeyMapping TOGGLE_TRIGGER_BOT;
    public static KeyMapping TOGGLE_SHIELD_BREAKER;
    public static KeyMapping TOGGLE_FAST_SHIELD;
    public static KeyMapping TOGGLE_AUTO_DOOR;
    public static KeyMapping TOGGLE_AUTO_WATER_BUCKET;
    public static KeyMapping TOGGLE_ENTITY_XRAY;
    public static KeyMapping TOGGLE_MINI_HUD;
    public static KeyMapping TOGGLE_INVENTORY_SORTER;
    public static KeyMapping TOGGLE_MOUSE_GESTURES;
    public static KeyMapping OPEN_CONFIG;
    public static KeyMapping SORT_INVENTORY;
    public static KeyMapping OPEN_WAYPOINTS;
    public static KeyMapping ADD_WAYPOINT;

    public static void register() {
        TOGGLE_AUTO_SPRINT = new KeyMapping(
            "key.catalyst.toggle_auto_sprint",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_auto_sprint"),
            KEY_CATEGORY
        );

        TOGGLE_AUTO_SWIM = new KeyMapping(
            "key.catalyst.toggle_auto_swim",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_auto_swim"),
            KEY_CATEGORY
        );

        TOGGLE_GAMMA_OVERRIDE = new KeyMapping(
            "key.catalyst.toggle_gamma_override",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_gamma_override"),
            KEY_CATEGORY
        );

        TOGGLE_AUTO_TOOL = new KeyMapping(
            "key.catalyst.toggle_auto_tool",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_auto_tool"),
            KEY_CATEGORY
        );

        TOGGLE_AUTO_WEAPON = new KeyMapping(
            "key.catalyst.toggle_auto_weapon",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_auto_weapon"),
            KEY_CATEGORY
        );

        TOGGLE_TRIGGER_BOT = new KeyMapping(
            "key.catalyst.toggle_trigger_bot",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_trigger_bot"),
            KEY_CATEGORY
        );

        TOGGLE_SHIELD_BREAKER = new KeyMapping(
            "key.catalyst.toggle_shield_breaker",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_shield_breaker"),
            KEY_CATEGORY
        );

        TOGGLE_FAST_SHIELD = new KeyMapping(
            "key.catalyst.toggle_fast_shield",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_fast_shield"),
            KEY_CATEGORY
        );

        TOGGLE_AUTO_DOOR = new KeyMapping(
            "key.catalyst.toggle_auto_door",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_auto_door"),
            KEY_CATEGORY
        );

        TOGGLE_AUTO_WATER_BUCKET = new KeyMapping(
            "key.catalyst.toggle_auto_water_bucket",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_auto_water_bucket"),
            KEY_CATEGORY
        );

        TOGGLE_ENTITY_XRAY = new KeyMapping(
            "key.catalyst.toggle_entity_xray",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_entity_xray"),
            KEY_CATEGORY
        );

        TOGGLE_MINI_HUD = new KeyMapping(
            "key.catalyst.toggle_mini_hud",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_mini_hud"),
            KEY_CATEGORY
        );

        TOGGLE_INVENTORY_SORTER = new KeyMapping(
            "key.catalyst.toggle_inventory_sorter",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_inventory_sorter"),
            KEY_CATEGORY
        );

        TOGGLE_MOUSE_GESTURES = new KeyMapping(
            "key.catalyst.toggle_mouse_gestures",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("toggle_mouse_gestures"),
            KEY_CATEGORY
        );

        OPEN_CONFIG = new KeyMapping(
            "key.catalyst.open_config",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("open_config"),
            KEY_CATEGORY
        );

        SORT_INVENTORY = new KeyMapping(
            "key.catalyst.sort_inventory",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("sort_inventory"),
            KEY_CATEGORY
        );

        OPEN_WAYPOINTS = new KeyMapping(
            "key.catalyst.open_waypoints",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("open_waypoints"),
            KEY_CATEGORY
        );

        ADD_WAYPOINT = new KeyMapping(
            "key.catalyst.add_waypoint",
            InputConstants.Type.KEYSYM,
            KeybindManager.getKeyCode("add_waypoint"),
            KEY_CATEGORY
        );

        KeyMappingRegistry.register(TOGGLE_AUTO_SPRINT);
        KeyMappingRegistry.register(TOGGLE_AUTO_SWIM);
        KeyMappingRegistry.register(TOGGLE_GAMMA_OVERRIDE);
        KeyMappingRegistry.register(TOGGLE_AUTO_TOOL);
        KeyMappingRegistry.register(TOGGLE_AUTO_WEAPON);
        KeyMappingRegistry.register(TOGGLE_TRIGGER_BOT);
        KeyMappingRegistry.register(TOGGLE_SHIELD_BREAKER);
        KeyMappingRegistry.register(TOGGLE_FAST_SHIELD);
        KeyMappingRegistry.register(TOGGLE_AUTO_DOOR);
        KeyMappingRegistry.register(TOGGLE_AUTO_WATER_BUCKET);
        KeyMappingRegistry.register(TOGGLE_ENTITY_XRAY);
        KeyMappingRegistry.register(TOGGLE_MINI_HUD);
        KeyMappingRegistry.register(TOGGLE_INVENTORY_SORTER);
        KeyMappingRegistry.register(TOGGLE_MOUSE_GESTURES);
        KeyMappingRegistry.register(OPEN_CONFIG);
        KeyMappingRegistry.register(SORT_INVENTORY);
        KeyMappingRegistry.register(OPEN_WAYPOINTS);
        KeyMappingRegistry.register(ADD_WAYPOINT);
    }
    
    public static void updateKeyMapping(String keyName, int keyCode) {
        KeyMapping mapping = getKeyMappingByName(keyName);
        if (mapping != null) {
            InputConstants.Key key = keyCode == GLFW.GLFW_KEY_UNKNOWN 
                ? InputConstants.UNKNOWN 
                : InputConstants.Type.KEYSYM.getOrCreate(keyCode);
            mapping.setKey(key);
            KeyMapping.resetMapping();
        }
    }
    
    private static KeyMapping getKeyMappingByName(String name) {
        if ("toggle_auto_sprint".equals(name)) return TOGGLE_AUTO_SPRINT;
        if ("toggle_auto_swim".equals(name)) return TOGGLE_AUTO_SWIM;
        if ("toggle_gamma_override".equals(name)) return TOGGLE_GAMMA_OVERRIDE;
        if ("toggle_auto_tool".equals(name)) return TOGGLE_AUTO_TOOL;
        if ("toggle_auto_weapon".equals(name)) return TOGGLE_AUTO_WEAPON;
        if ("toggle_trigger_bot".equals(name)) return TOGGLE_TRIGGER_BOT;
        if ("toggle_shield_breaker".equals(name)) return TOGGLE_SHIELD_BREAKER;
        if ("toggle_fast_shield".equals(name)) return TOGGLE_FAST_SHIELD;
        if ("toggle_auto_door".equals(name)) return TOGGLE_AUTO_DOOR;
        if ("toggle_auto_water_bucket".equals(name)) return TOGGLE_AUTO_WATER_BUCKET;
        if ("toggle_entity_xray".equals(name)) return TOGGLE_ENTITY_XRAY;
        if ("toggle_mini_hud".equals(name)) return TOGGLE_MINI_HUD;
        if ("toggle_inventory_sorter".equals(name)) return TOGGLE_INVENTORY_SORTER;
        if ("toggle_mouse_gestures".equals(name)) return TOGGLE_MOUSE_GESTURES;
        if ("open_config".equals(name)) return OPEN_CONFIG;
        if ("sort_inventory".equals(name)) return SORT_INVENTORY;
        if ("open_waypoints".equals(name)) return OPEN_WAYPOINTS;
        if ("add_waypoint".equals(name)) return ADD_WAYPOINT;
        return null;
    }
}
