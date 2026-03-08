package org.alku.catalyst.client.waypoint;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class Waypoint {
    private final String id;
    private final String name;
    private final BlockPos pos;
    private final ResourceKey<Level> dimension;
    private final int color;
    
    public Waypoint(String name, BlockPos pos, ResourceKey<Level> dimension, int color) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.pos = pos;
        this.dimension = dimension;
        this.color = color;
    }
    
    public Waypoint(String id, String name, BlockPos pos, ResourceKey<Level> dimension, int color) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dimension = dimension;
        this.color = color;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public int getX() {
        return pos.getX();
    }
    
    public int getY() {
        return pos.getY();
    }
    
    public int getZ() {
        return pos.getZ();
    }
    
    public ResourceKey<Level> getDimension() {
        return dimension;
    }
    
    public int getColor() {
        return color;
    }
    
    public String getInitial() {
        return getInitial(name);
    }
    
    public static String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return String.valueOf(name.charAt(0)).toUpperCase();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return Objects.equals(id, waypoint.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
