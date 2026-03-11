package org.alku.catalyst.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CatalystConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("catalyst.json");
    
    private static CatalystConfig instance;
    
    public boolean autoSprintEnabled = true;
    public boolean autoSwimEnabled = true;
    public boolean gammaOverrideEnabled = true;
    public double gammaValue = 100.0;
    public boolean nightVisionMode = true;
    public boolean autoToolEnabled = true;
    public boolean autoToolRestore = true;
    public int autoToolLockedSlot = -1;
    public boolean autoWeaponEnabled = true;
    public boolean autoWeaponRestore = true;
    public int autoWeaponLockedSlot = -1;
    public boolean autoDoorEnabled = true;
    public boolean autoWaterBucketEnabled = true;
    public boolean chamsEnabled = false;
    public boolean chamsPlayers = true;
    public boolean chamsAnimals = true;
    public boolean chamsMonsters = true;
    
    public boolean inventorySorterEnabled = true;
    public boolean autoSortOnOpen = true;
    public boolean sortHotbar = false;
    public boolean sortPlayerInventoryInContainer = true;
    public int sortMode = 0;
    
    public boolean rmbTweak = true;
    public boolean lmbTweakWithItem = true;
    public boolean lmbTweakWithoutItem = true;
    public boolean wheelTweak = true;
    public int wheelSearchOrder = 1;
    public int wheelScrollDirection = 0;
    
    public float guiScale = 1.0f;
    public JsonObject panelPositions = new JsonObject();
    
    public static CatalystConfig getInstance() {
        if (instance == null) {
            instance = new CatalystConfig();
            instance.load();
        }
        return instance;
    }
    
    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                
                if (obj.has("autoSprintEnabled")) {
                    autoSprintEnabled = obj.get("autoSprintEnabled").getAsBoolean();
                }
                if (obj.has("autoSwimEnabled")) {
                    autoSwimEnabled = obj.get("autoSwimEnabled").getAsBoolean();
                }
                if (obj.has("gammaOverrideEnabled")) {
                    gammaOverrideEnabled = obj.get("gammaOverrideEnabled").getAsBoolean();
                }
                if (obj.has("gammaValue")) {
                    gammaValue = Math.max(0.0, Math.min(10000.0, obj.get("gammaValue").getAsDouble()));
                }
                if (obj.has("nightVisionMode")) {
                    nightVisionMode = obj.get("nightVisionMode").getAsBoolean();
                }
                if (obj.has("autoToolEnabled")) {
                    autoToolEnabled = obj.get("autoToolEnabled").getAsBoolean();
                }
                if (obj.has("autoToolRestore")) {
                    autoToolRestore = obj.get("autoToolRestore").getAsBoolean();
                }
                if (obj.has("autoToolLockedSlot")) {
                    autoToolLockedSlot = obj.get("autoToolLockedSlot").getAsInt();
                }
                if (obj.has("autoWeaponEnabled")) {
                    autoWeaponEnabled = obj.get("autoWeaponEnabled").getAsBoolean();
                }
                if (obj.has("autoWeaponRestore")) {
                    autoWeaponRestore = obj.get("autoWeaponRestore").getAsBoolean();
                }
                if (obj.has("autoWeaponLockedSlot")) {
                    autoWeaponLockedSlot = obj.get("autoWeaponLockedSlot").getAsInt();
                }
                if (obj.has("autoDoorEnabled")) {
                    autoDoorEnabled = obj.get("autoDoorEnabled").getAsBoolean();
                }
                if (obj.has("autoWaterBucketEnabled")) {
                    autoWaterBucketEnabled = obj.get("autoWaterBucketEnabled").getAsBoolean();
                }
                if (obj.has("chamsEnabled")) {
                    chamsEnabled = obj.get("chamsEnabled").getAsBoolean();
                }
                if (obj.has("chamsPlayers")) {
                    chamsPlayers = obj.get("chamsPlayers").getAsBoolean();
                }
                if (obj.has("chamsAnimals")) {
                    chamsAnimals = obj.get("chamsAnimals").getAsBoolean();
                }
                if (obj.has("chamsMonsters")) {
                    chamsMonsters = obj.get("chamsMonsters").getAsBoolean();
                }
                if (obj.has("inventorySorterEnabled")) {
                    inventorySorterEnabled = obj.get("inventorySorterEnabled").getAsBoolean();
                }
                if (obj.has("autoSortOnOpen")) {
                    autoSortOnOpen = obj.get("autoSortOnOpen").getAsBoolean();
                }
                if (obj.has("sortHotbar")) {
                    sortHotbar = obj.get("sortHotbar").getAsBoolean();
                }
                if (obj.has("sortPlayerInventoryInContainer")) {
                    sortPlayerInventoryInContainer = obj.get("sortPlayerInventoryInContainer").getAsBoolean();
                }
                if (obj.has("sortMode")) {
                    sortMode = Math.max(0, Math.min(2, obj.get("sortMode").getAsInt()));
                }
                if (obj.has("rmbTweak")) {
                    rmbTweak = obj.get("rmbTweak").getAsBoolean();
                }
                if (obj.has("lmbTweakWithItem")) {
                    lmbTweakWithItem = obj.get("lmbTweakWithItem").getAsBoolean();
                }
                if (obj.has("lmbTweakWithoutItem")) {
                    lmbTweakWithoutItem = obj.get("lmbTweakWithoutItem").getAsBoolean();
                }
                if (obj.has("wheelTweak")) {
                    wheelTweak = obj.get("wheelTweak").getAsBoolean();
                }
                if (obj.has("wheelSearchOrder")) {
                    wheelSearchOrder = Math.max(0, Math.min(1, obj.get("wheelSearchOrder").getAsInt()));
                }
                if (obj.has("wheelScrollDirection")) {
                    wheelScrollDirection = Math.max(0, Math.min(2, obj.get("wheelScrollDirection").getAsInt()));
                }
                if (obj.has("guiScale")) {
                    guiScale = (float) Math.max(0.5, Math.min(2.0, obj.get("guiScale").getAsDouble()));
                }
                if (obj.has("panelPositions")) {
                    panelPositions = obj.getAsJsonObject("panelPositions");
                }
            } catch (Exception e) {
                save();
            }
        } else {
            save();
        }
    }
    
    public void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("autoSprintEnabled", autoSprintEnabled);
        obj.addProperty("autoSwimEnabled", autoSwimEnabled);
        obj.addProperty("gammaOverrideEnabled", gammaOverrideEnabled);
        obj.addProperty("gammaValue", gammaValue);
        obj.addProperty("nightVisionMode", nightVisionMode);
        obj.addProperty("autoToolEnabled", autoToolEnabled);
        obj.addProperty("autoToolRestore", autoToolRestore);
        obj.addProperty("autoToolLockedSlot", autoToolLockedSlot);
        obj.addProperty("autoWeaponEnabled", autoWeaponEnabled);
        obj.addProperty("autoWeaponRestore", autoWeaponRestore);
        obj.addProperty("autoWeaponLockedSlot", autoWeaponLockedSlot);
        obj.addProperty("autoDoorEnabled", autoDoorEnabled);
        obj.addProperty("autoWaterBucketEnabled", autoWaterBucketEnabled);
        obj.addProperty("chamsEnabled", chamsEnabled);
        obj.addProperty("chamsPlayers", chamsPlayers);
        obj.addProperty("chamsAnimals", chamsAnimals);
        obj.addProperty("chamsMonsters", chamsMonsters);
        obj.addProperty("inventorySorterEnabled", inventorySorterEnabled);
        obj.addProperty("autoSortOnOpen", autoSortOnOpen);
        obj.addProperty("sortHotbar", sortHotbar);
        obj.addProperty("sortPlayerInventoryInContainer", sortPlayerInventoryInContainer);
        obj.addProperty("sortMode", sortMode);
        obj.addProperty("rmbTweak", rmbTweak);
        obj.addProperty("lmbTweakWithItem", lmbTweakWithItem);
        obj.addProperty("lmbTweakWithoutItem", lmbTweakWithoutItem);
        obj.addProperty("wheelTweak", wheelTweak);
        obj.addProperty("wheelSearchOrder", wheelSearchOrder);
        obj.addProperty("wheelScrollDirection", wheelScrollDirection);
        obj.addProperty("guiScale", guiScale);
        obj.add("panelPositions", panelPositions);
        
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setPanelPosition(String panelName, int x, int y) {
        JsonObject pos = new JsonObject();
        pos.addProperty("x", x);
        pos.addProperty("y", y);
        panelPositions.add(panelName, pos);
    }
    
    public int getPanelX(String panelName, int defaultX) {
        if (panelPositions.has(panelName)) {
            JsonObject pos = panelPositions.getAsJsonObject(panelName);
            if (pos.has("x")) {
                return pos.get("x").getAsInt();
            }
        }
        return defaultX;
    }
    
    public int getPanelY(String panelName, int defaultY) {
        if (panelPositions.has(panelName)) {
            JsonObject pos = panelPositions.getAsJsonObject(panelName);
            if (pos.has("y")) {
                return pos.get("y").getAsInt();
            }
        }
        return defaultY;
    }
}
