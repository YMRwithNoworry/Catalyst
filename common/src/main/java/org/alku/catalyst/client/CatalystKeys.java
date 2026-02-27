package org.alku.catalyst.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.alku.catalyst.client.gui.KeybindManager;

public class CatalystKeys {
    public static final String KEY_CATEGORY = "key.categories.catalyst";
    
    public static KeyMapping TOGGLE_AUTO_SPRINT;
    public static KeyMapping TOGGLE_AUTO_SWIM;
    public static KeyMapping TOGGLE_GAMMA_OVERRIDE;
    public static KeyMapping TOGGLE_AUTO_TOOL;
    public static KeyMapping TOGGLE_AUTO_WEAPON;
    public static KeyMapping OPEN_CONFIG;
    
    public static void register() {
        TOGGLE_AUTO_SPRINT = createKey("toggle_auto_sprint");
        TOGGLE_AUTO_SWIM = createKey("toggle_auto_swim");
        TOGGLE_GAMMA_OVERRIDE = createKey("toggle_gamma_override");
        TOGGLE_AUTO_TOOL = createKey("toggle_auto_tool");
        TOGGLE_AUTO_WEAPON = createKey("toggle_auto_weapon");
        OPEN_CONFIG = createKey("open_config");
        
        KeyMappingRegistry.register(TOGGLE_AUTO_SPRINT);
        KeyMappingRegistry.register(TOGGLE_AUTO_SWIM);
        KeyMappingRegistry.register(TOGGLE_GAMMA_OVERRIDE);
        KeyMappingRegistry.register(TOGGLE_AUTO_TOOL);
        KeyMappingRegistry.register(TOGGLE_AUTO_WEAPON);
        KeyMappingRegistry.register(OPEN_CONFIG);
    }
    
    private static KeyMapping createKey(String name) {
        int defaultKey = KeybindManager.getKey(name);
        return new KeyMapping(
            "key.catalyst." + name,
            InputConstants.Type.KEYSYM,
            defaultKey,
            KEY_CATEGORY
        );
    }
}
