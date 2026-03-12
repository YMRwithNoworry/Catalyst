package org.alku.catalyst.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.client.waypoint.Waypoint;
import org.alku.catalyst.client.waypoint.WaypointManager;

public class WaypointDetailScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    
    private final Waypoint waypoint;
    
    public WaypointDetailScreen(Waypoint waypoint) {
        super(Component.literal(waypoint.getName()));
        this.waypoint = waypoint;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 80;
        
        addRenderableWidget(Button.builder(
            Component.translatable("catalyst.gui.waypoints.teleport"),
            button -> teleportToWaypoint()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("catalyst.gui.waypoints.delete"),
            button -> deleteWaypoint()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 30, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> this.onClose()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 60, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }
    
    private void teleportToWaypoint() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand("tp " + waypoint.getX() + " " + waypoint.getY() + " " + waypoint.getZ());
        }
        this.onClose();
    }
    
    private void deleteWaypoint() {
        WaypointManager.getInstance().removeWaypoint(waypoint);
        this.onClose();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        
        String posText = String.format("X: %d, Y: %d, Z: %d", waypoint.getX(), waypoint.getY(), waypoint.getZ());
        guiGraphics.drawCenteredString(this.font, posText, this.width / 2, 45, 0xAAAAAA);
        
        String dimText = "Dimension: " + waypoint.getDimension().location().toString();
        guiGraphics.drawCenteredString(this.font, dimText, this.width / 2, 60, 0xAAAAAA);
        
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
