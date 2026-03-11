package org.alku.catalyst.client.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path WAYPOINT_PATH = Platform.getConfigFolder().resolve("catalyst_waypoints.json");
    
    private static WaypointManager instance;
    
    private final List<Waypoint> waypoints = new ArrayList<>();
    private final Map<String, String> initialCache = new HashMap<>();
    
    public static WaypointManager getInstance() {
        if (instance == null) {
            instance = new WaypointManager();
            instance.load();
        }
        return instance;
    }
    
    public List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }
    
    public List<Waypoint> getWaypointsForDimension(ResourceKey<Level> dimension) {
        List<Waypoint> result = new ArrayList<>();
        for (Waypoint wp : waypoints) {
            if (wp.getDimension().equals(dimension)) {
                result.add(wp);
            }
        }
        return result;
    }
    
    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
        rebuildInitialCache();
        save();
    }
    
    public void removeWaypoint(String id) {
        waypoints.removeIf(wp -> wp.getId().equals(id));
        rebuildInitialCache();
        save();
    }
    
    public void removeWaypoint(Waypoint waypoint) {
        waypoints.remove(waypoint);
        rebuildInitialCache();
        save();
    }
    
    public Waypoint getById(String id) {
        for (Waypoint wp : waypoints) {
            if (wp.getId().equals(id)) {
                return wp;
            }
        }
        return null;
    }
    
    private void rebuildInitialCache() {
        initialCache.clear();
        Map<String, List<Waypoint>> byInitial = new HashMap<>();
        
        for (Waypoint wp : waypoints) {
            String initial = Waypoint.getInitial(wp.getName());
            byInitial.computeIfAbsent(initial, k -> new ArrayList<>()).add(wp);
        }
        
        for (Map.Entry<String, List<Waypoint>> entry : byInitial.entrySet()) {
            List<Waypoint> list = entry.getValue();
            if (list.size() == 1) {
                initialCache.put(list.get(0).getId(), entry.getKey());
            } else {
                for (int i = 0; i < list.size(); i++) {
                    Waypoint wp = list.get(i);
                    String uniqueInitial = calculateUniqueInitial(wp, list, i);
                    initialCache.put(wp.getId(), uniqueInitial);
                }
            }
        }
    }
    
    private String calculateUniqueInitial(Waypoint waypoint, List<Waypoint> sameInitial, int index) {
        String name = waypoint.getName();
        for (int len = 1; len <= name.length(); len++) {
            String candidate = name.substring(0, len).toUpperCase();
            boolean unique = true;
            for (int j = 0; j < sameInitial.size(); j++) {
                if (j != index) {
                    Waypoint other = sameInitial.get(j);
                    if (other.getName().length() >= len && 
                        other.getName().substring(0, len).toUpperCase().equals(candidate)) {
                        unique = false;
                        break;
                    }
                }
            }
            if (unique) {
                return candidate;
            }
        }
        return name.toUpperCase() + "_" + index;
    }
    
    public String getDisplayInitial(Waypoint waypoint) {
        return initialCache.getOrDefault(waypoint.getId(), Waypoint.getInitial(waypoint.getName()));
    }
    
    public void load() {
        waypoints.clear();
        
        if (Files.exists(WAYPOINT_PATH)) {
            try {
                String json = Files.readString(WAYPOINT_PATH);
                JsonElement element = JsonParser.parseString(json);
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement e : array) {
                        JsonObject obj = e.getAsJsonObject();
                        String id = obj.get("id").getAsString();
                        String name = obj.get("name").getAsString();
                        int x = obj.get("x").getAsInt();
                        int y = obj.get("y").getAsInt();
                        int z = obj.get("z").getAsInt();
                        String dimensionStr = obj.get("dimension").getAsString();
                        int color = obj.has("color") ? obj.get("color").getAsInt() : 0xFFFFFF;
                        
                        ResourceLocation dimLoc = new ResourceLocation(dimensionStr);
                        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
                        
                        waypoints.add(new Waypoint(id, name, new BlockPos(x, y, z), dimension, color));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        rebuildInitialCache();
    }
    
    public void save() {
        JsonArray array = new JsonArray();
        for (Waypoint wp : waypoints) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", wp.getId());
            obj.addProperty("name", wp.getName());
            obj.addProperty("x", wp.getX());
            obj.addProperty("y", wp.getY());
            obj.addProperty("z", wp.getZ());
            obj.addProperty("dimension", wp.getDimension().location().toString());
            obj.addProperty("color", wp.getColor());
            array.add(obj);
        }
        
        try {
            Files.createDirectories(WAYPOINT_PATH.getParent());
            Files.writeString(WAYPOINT_PATH, GSON.toJson(array));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
