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
        addWidget(nameBox);
        
        addRenderableWidget(Button.builder(
            Component.translatable("catalyst.gui.waypoints.create"),
            button -> createWaypoint()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 50, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.cancel"),
            button -> this.onClose()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 80, BUTTON_WIDTH, BUTTON_HEIGHT).build());
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
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        String posText = String.format("Position: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
        guiGraphics.drawString(this.font, posText, this.width / 2 - 100, 45, 0xAAAAAA);
        
        nameBox.render(guiGraphics, mouseX, mouseY, partialTick);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(new WaypointScreen());
        }
    }
}
