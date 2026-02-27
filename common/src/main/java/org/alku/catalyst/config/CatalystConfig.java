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
    public double gammaValue = 1.0;
    public boolean autoToolEnabled = true;
    public boolean autoWeaponEnabled = true;
    public boolean autoDoorEnabled = true;
    public boolean autoWaterBucketEnabled = true;
    
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
                    gammaValue = Math.max(0.0, Math.min(1.0, obj.get("gammaValue").getAsDouble()));
                }
                if (obj.has("autoToolEnabled")) {
                    autoToolEnabled = obj.get("autoToolEnabled").getAsBoolean();
                }
                if (obj.has("autoWeaponEnabled")) {
                    autoWeaponEnabled = obj.get("autoWeaponEnabled").getAsBoolean();
                }
                if (obj.has("autoDoorEnabled")) {
                    autoDoorEnabled = obj.get("autoDoorEnabled").getAsBoolean();
                }
                if (obj.has("autoWaterBucketEnabled")) {
                    autoWaterBucketEnabled = obj.get("autoWaterBucketEnabled").getAsBoolean();
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
        obj.addProperty("autoToolEnabled", autoToolEnabled);
        obj.addProperty("autoWeaponEnabled", autoWeaponEnabled);
        obj.addProperty("autoDoorEnabled", autoDoorEnabled);
        obj.addProperty("autoWaterBucketEnabled", autoWaterBucketEnabled);
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
