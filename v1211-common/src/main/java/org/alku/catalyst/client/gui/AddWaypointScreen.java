package org.alku.catalyst.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.alku.catalyst.client.waypoint.Waypoint;
import org.alku.catalyst.client.waypoint.WaypointManager;

public class AddWaypointScreen extends Screen {
    
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    
    private final BlockPos pos;
    private final ResourceKey<Level> dimension;
    private final int color;
    
    private EditBox nameBox;
    
    public AddWaypointScreen(BlockPos pos, ResourceKey<Level> dimension, int color) {
        super(Component.translatable("catalyst.gui.waypoints.add"));
        this.pos = pos;
        this.dimension = dimension;
        this.color = color;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 60;
        
        nameBox = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.translatable("catalyst.gui.waypoints.name"));
        nameBox.setMaxLength(50);
        nameBox.setValue("Waypoint");
        nameBox.setBordered(false);
        nameBox.setTextColor(0xFFFFFF);
        nameBox.setFocused(true);
        addRenderableWidget(nameBox);
        setInitialFocus(nameBox);
        
        addRenderableWidget(Button.builder(
            Component.translatable("catalyst.gui.waypoints.create"),
            button -> createWaypoint()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 30, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> this.onClose()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 58, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }
    
    private void createWaypoint() {
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) {
            name = "Waypoint";
        }
        
        Waypoint waypoint = new Waypoint(name, pos, dimension, color);
        WaypointManager.getInstance().addWaypoint(waypoint);
        
        this.onClose();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xCC000000);
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        String posText = String.format("X: %d, Y: %d, Z: %d", pos.getX(), pos.getY(), pos.getZ());
        guiGraphics.drawCenteredString(this.font, posText, this.width / 2, 45, 0xAAAAAA);
        
        int boxX = this.width / 2 - 100;
        int boxY = 60;
        guiGraphics.fill(boxX - 1, boxY - 1, boxX + 201, boxY + 21, 0xFF555555);
        guiGraphics.fill(boxX, boxY, boxX + 200, boxY + 20, 0xFF000000);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }
}
