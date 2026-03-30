package org.alku.catalyst.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.platform.Platform;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class KeybindManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path KEYBIND_PATH = Platform.getConfigFolder().resolve("catalyst_keys.json");
    private static final Map<String, Integer> keyBindings = new HashMap<>();
    
    private static final Map<String, Integer> DEFAULT_KEYS = new HashMap<>();
    
    static {
        DEFAULT_KEYS.put("toggle_auto_sprint", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_auto_swim", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_gamma_override", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_auto_tool", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_auto_weapon", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_auto_door", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_auto_water_bucket", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_trigger_bot", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_shield_breaker", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_fast_shield", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_entity_xray", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_mini_hud", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_inventory_sorter", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("toggle_mouse_gestures", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("open_config", GLFW.GLFW_KEY_J);
        DEFAULT_KEYS.put("sort_inventory", GLFW.GLFW_KEY_R);
        DEFAULT_KEYS.put("open_waypoints", GLFW.GLFW_KEY_UNKNOWN);
        DEFAULT_KEYS.put("add_waypoint", GLFW.GLFW_KEY_UNKNOWN);
        
        initDefaults();
        load();
    }
    
    private static void initDefaults() {
        keyBindings.putAll(DEFAULT_KEYS);
    }
    
    public static void load() {
        if (Files.exists(KEYBIND_PATH)) {
            try {
                String json = Files.readString(KEYBIND_PATH);
                JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                for (String key : DEFAULT_KEYS.keySet()) {
                    if (obj.has(key)) {
                        keyBindings.put(key, obj.get(key).getAsInt());
                    }
                }
            } catch (Exception e) {
                save();
            }
        } else {
            save();
        }
    }
    
    public static void save() {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Integer> entry : keyBindings.entrySet()) {
            obj.addProperty(entry.getKey(), entry.getValue());
        }
        try {
            Files.createDirectories(KEYBIND_PATH.getParent());
            Files.writeString(KEYBIND_PATH, GSON.toJson(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int getKeyCode(String name) {
        return keyBindings.getOrDefault(name, GLFW.GLFW_KEY_UNKNOWN);
    }
    
    public static InputConstants.Key getKey(String name) {
        int keyCode = getKeyCode(name);
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return InputConstants.UNKNOWN;
        }
        return InputConstants.Type.KEYSYM.getOrCreate(keyCode);
    }
    
    public static void setKey(String name, int keyCode) {
        keyBindings.put(name, keyCode);
        save();
    }
    
    public static String getBoundKey(String name) {
        InputConstants.Key key = getKey(name);
        if (key == InputConstants.UNKNOWN) {
            return "None";
        }
        return key.getDisplayName().getString();
    }
    
    public static boolean isKeyDown(String name) {
        int keyCode = keyBindings.getOrDefault(name, GLFW.GLFW_KEY_UNKNOWN);
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        long window = GLFW.glfwGetCurrentContext();
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }
}
