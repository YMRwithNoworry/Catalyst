package org.alku.catalyst.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.client.waypoint.Waypoint;
import org.alku.catalyst.client.waypoint.WaypointManager;

import java.util.Random;

public class WaypointScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;
    
    private int scrollOffset = 0;
    private int maxVisible = 10;
    
    public WaypointScreen() {
        super(Component.translatable("catalyst.gui.waypoints.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        
        addRenderableWidget(Button.builder(
            Component.translatable("catalyst.gui.waypoints.add_current"),
            button -> addCurrentPosition()
        ).bounds(centerX - BUTTON_WIDTH / 2, this.height - 50, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> this.onClose()
        ).bounds(centerX - BUTTON_WIDTH / 2, this.height - 25, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        rebuildWaypointButtons();
    }
    
    private void rebuildWaypointButtons() {
        clearWidgets();
        
        int centerX = this.width / 2;
        int startY = 50;
        
        int index = 0;
        for (Waypoint wp : WaypointManager.getInstance().getWaypoints()) {
            if (index >= scrollOffset && index < scrollOffset + maxVisible) {
                int y = startY + (index - scrollOffset) * BUTTON_SPACING;
                addRenderableWidget(new WaypointButton(
                    centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT, wp
                ));
            }
            index++;
        }
        
        addRenderableWidget(Button.builder(
            Component.translatable("catalyst.gui.waypoints.add_current"),
            button -> addCurrentPosition()
        ).bounds(centerX - BUTTON_WIDTH / 2, this.height - 50, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> this.onClose()
        ).bounds(centerX - BUTTON_WIDTH / 2, this.height - 25, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }
    
    private void addCurrentPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            BlockPos pos = mc.player.blockPosition();
            int color = generateRandomColor();
            
            mc.setScreen(new AddWaypointScreen(pos, mc.level.dimension(), color));
        }
    }
    
    private int generateRandomColor() {
        Random random = new Random();
        return random.nextInt(0xFFFFFF + 1);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        if (WaypointManager.getInstance().getWaypoints().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, 
                Component.translatable("catalyst.gui.waypoints.empty"), 
                this.width / 2, this.height / 2, 0x888888);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private static class WaypointButton extends Button {
        private final Waypoint waypoint;
        
        public WaypointButton(int x, int y, int width, int height, Waypoint waypoint) {
            super(x, y, width, height, 
                Component.literal(waypoint.getName() + " [" + waypoint.getX() + ", " + waypoint.getY() + ", " + waypoint.getZ() + "]"),
                button -> {},
                Button.DEFAULT_NARRATION);
            this.waypoint = waypoint;
        }
        
        @Override
        public void onPress() {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new WaypointDetailScreen(waypoint));
        }
    }
}
