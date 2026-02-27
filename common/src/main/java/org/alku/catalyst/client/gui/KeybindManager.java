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
        DEFAULT_KEYS.put("open_config", GLFW.GLFW_KEY_J);
        
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
    
    public static int getKey(String name) {
        return keyBindings.getOrDefault(name, GLFW.GLFW_KEY_UNKNOWN);
    }
    
    public static void setKey(String name, int keyCode) {
        keyBindings.put(name, keyCode);
        save();
    }
    
    public static String getBoundKey(String name) {
        int keyCode = getKey(name);
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "None";
        }
        return InputConstants.getKey(keyCode, 0).getDisplayName().getString();
    }
    
    public static boolean isKeyDown(String name) {
        int keyCode = getKey(name);
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        long window = GLFW.glfwGetCurrentContext();
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }
}
