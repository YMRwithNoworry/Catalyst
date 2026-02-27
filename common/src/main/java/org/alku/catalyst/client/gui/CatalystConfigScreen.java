package org.alku.catalyst.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.List;

public class CatalystConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 130;
    private static final int BUTTON_HEIGHT = 16;
    private static final int PANEL_HEADER_HEIGHT = 18;
    
    private final Screen parent;
    private List<ModulePanel> panels = new ArrayList<>();
    
    private float scale = 1.0f;
    private ModulePanel draggingPanel = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    
    public CatalystConfigScreen(Screen parent) {
        super(Component.translatable("catalyst.gui.title"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        panels.clear();
        
        int startX = 10;
        int startY = 10;
        int spacing = 15;
        
        ModulePanel movementPanel = new ModulePanel(startX, startY, "Movement", this);
        movementPanel.addModule("auto_sprint", "toggle_auto_sprint");
        movementPanel.addModule("auto_swim", "toggle_auto_swim");
        panels.add(movementPanel);
        
        ModulePanel combatPanel = new ModulePanel(startX + (PANEL_WIDTH + spacing), startY, "Combat", this);
        combatPanel.addModule("auto_weapon", "toggle_auto_weapon");
        panels.add(combatPanel);
        
        ModulePanel playerPanel = new ModulePanel(startX + (PANEL_WIDTH + spacing) * 2, startY, "Player", this);
        playerPanel.addModule("gamma_override", "toggle_gamma_override");
        playerPanel.addModule("auto_door", "toggle_auto_door");
        playerPanel.addModule("auto_water_bucket", "toggle_auto_water_bucket");
        panels.add(playerPanel);
        
        ModulePanel toolsPanel = new ModulePanel(startX + (PANEL_WIDTH + spacing) * 3, startY, "Tools", this);
        toolsPanel.addModule("auto_tool", "toggle_auto_tool");
        panels.add(toolsPanel);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        for (ModulePanel panel : panels) {
            panel.render(graphics, mouseX, mouseY, partialTick);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            ModulePanel panel = panels.get(i);
            if (panel.isMouseOver(mouseX, mouseY)) {
                if (button == 0) {
                    if (panel.isMouseOverHeader(mouseX, mouseY)) {
                        draggingPanel = panel;
                        dragOffsetX = mouseX - panel.getX();
                        dragOffsetY = mouseY - panel.getY();
                        panels.remove(i);
                        panels.add(panel);
                        return true;
                    }
                }
                if (panel.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingPanel != null) {
            draggingPanel = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPanel != null && button == 0) {
            draggingPanel.setPosition((int)(mouseX - dragOffsetX), (int)(mouseY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scale += delta * 0.1f;
        scale = Math.max(0.5f, Math.min(2.0f, scale));
        return true;
    }
    
    @Override
    public void onClose() {
        CatalystConfig.getInstance().save();
        this.minecraft.setScreen(parent);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public float getScale() {
        return scale;
    }
    
    public static class ModulePanel {
        private int x;
        private int y;
        private final String name;
        private final CatalystConfigScreen parent;
        private final List<ModuleButton> buttons = new ArrayList<>();
        private int height;
        
        public ModulePanel(int x, int y, String name, CatalystConfigScreen parent) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.parent = parent;
            this.height = PANEL_HEADER_HEIGHT;
        }
        
        public void addModule(String featureKey, String keybindKey) {
            buttons.add(new ModuleButton(featureKey, keybindKey, this));
            height += BUTTON_HEIGHT + 2;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setPosition(int newX, int newY) {
            this.x = newX;
            this.y = newY;
        }
        
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + height;
        }
        
        public boolean isMouseOverHeader(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + PANEL_HEADER_HEIGHT;
        }
        
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            drawIosRoundedRect(graphics, x, y, x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT, 6, 0xFF4444AA);
            graphics.drawCenteredString(parent.minecraft.font, Component.literal(name), x + PANEL_WIDTH / 2, y + 3, 0xFFFFFF);
            
            drawIosRoundedRect(graphics, x, y + PANEL_HEADER_HEIGHT - 6, x + PANEL_WIDTH, y + height, 6, 0x99333333);
            
            int currentY = y + PANEL_HEADER_HEIGHT + 1;
            for (ModuleButton button : buttons) {
                button.render(graphics, x + 2, currentY, mouseX, mouseY);
                currentY += BUTTON_HEIGHT + 2;
            }
        }
        
        private void drawIosRoundedRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color) {
            graphics.fill(x1 + radius, y1, x2 - radius, y2, color);
            graphics.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
            graphics.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);
            
            for (int dy = 0; dy <= radius; dy++) {
                for (int dx = 0; dx <= radius; dx++) {
                    double dx2 = (dx + 0.5) * (dx + 0.5);
                    double dy2 = (dy + 0.5) * (dy + 0.5);
                    if (dx2 + dy2 <= radius * radius) {
                        graphics.fill(x1 + radius - dx, y1 + radius - dy, x1 + radius - dx + 1, y1 + radius - dy + 1, color);
                        graphics.fill(x2 - radius + dx - 1, y1 + radius - dy, x2 - radius + dx, y1 + radius - dy + 1, color);
                        graphics.fill(x1 + radius - dx, y2 - radius + dy - 1, x1 + radius - dx + 1, y2 - radius + dy, color);
                        graphics.fill(x2 - radius + dx - 1, y2 - radius + dy - 1, x2 - radius + dx, y2 - radius + dy, color);
                    }
                }
            }
        }
        
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + height) {
                int currentY = y + PANEL_HEADER_HEIGHT + 1;
                for (ModuleButton moduleButton : buttons) {
                    if (mouseY >= currentY && mouseY <= currentY + BUTTON_HEIGHT) {
                        moduleButton.onClick(button);
                        return true;
                    }
                    currentY += BUTTON_HEIGHT + 2;
                }
            }
            return false;
        }
        
        public CatalystConfigScreen getParentScreen() {
            return parent;
        }
    }
    
    public static class ModuleButton {
        private final String featureKey;
        private final String keybindKey;
        private final ModulePanel panel;
        
        public ModuleButton(String featureKey, String keybindKey, ModulePanel panel) {
            this.featureKey = featureKey;
            this.keybindKey = keybindKey;
            this.panel = panel;
        }
        
        public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
            CatalystConfig config = CatalystConfig.getInstance();
            boolean isEnabled = false;
            
            if (featureKey.equals("auto_sprint")) isEnabled = config.autoSprintEnabled;
            else if (featureKey.equals("auto_swim")) isEnabled = config.autoSwimEnabled;
            else if (featureKey.equals("gamma_override")) isEnabled = config.gammaOverrideEnabled;
            else if (featureKey.equals("auto_tool")) isEnabled = config.autoToolEnabled;
            else if (featureKey.equals("auto_weapon")) isEnabled = config.autoWeaponEnabled;
            else if (featureKey.equals("auto_door")) isEnabled = config.autoDoorEnabled;
            else if (featureKey.equals("auto_water_bucket")) isEnabled = config.autoWaterBucketEnabled;
            
            int color = isEnabled ? 0xFF55AA55 : 0xFFAA5555;
            int hoverColor = isEnabled ? 0xFF77CC77 : 0xFFCC7777;
            
            boolean isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH - 4 && 
                              mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
            
            drawIosButton(graphics, x, y, x + PANEL_WIDTH - 4, y + BUTTON_HEIGHT, 4, isHovered ? hoverColor : color);
            
            Component featureName = Component.translatable("catalyst.feature." + featureKey);
            graphics.drawString(panel.getParentScreen().minecraft.font, featureName, x + 3, y + 4, 0xFFFFFF);
            
            String keybindText = KeybindManager.getBoundKey(keybindKey);
            Component keybindComponent = Component.literal("[" + keybindText + "]");
            int textWidth = panel.getParentScreen().minecraft.font.width(keybindComponent);
            graphics.drawString(panel.getParentScreen().minecraft.font, keybindComponent, 
                               x + PANEL_WIDTH - 6 - textWidth, y + 4, 0xFFFFFF);
        }
        
        private void drawIosButton(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color) {
            graphics.fill(x1 + radius, y1, x2 - radius, y2, color);
            graphics.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
            graphics.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);
            
            for (int dy = 0; dy <= radius; dy++) {
                for (int dx = 0; dx <= radius; dx++) {
                    double dx2 = (dx + 0.5) * (dx + 0.5);
                    double dy2 = (dy + 0.5) * (dy + 0.5);
                    if (dx2 + dy2 <= radius * radius) {
                        graphics.fill(x1 + radius - dx, y1 + radius - dy, x1 + radius - dx + 1, y1 + radius - dy + 1, color);
                        graphics.fill(x2 - radius + dx - 1, y1 + radius - dy, x2 - radius + dx, y1 + radius - dy + 1, color);
                        graphics.fill(x1 + radius - dx, y2 - radius + dy - 1, x1 + radius - dx + 1, y2 - radius + dy, color);
                        graphics.fill(x2 - radius + dx - 1, y2 - radius + dy - 1, x2 - radius + dx, y2 - radius + dy, color);
                    }
                }
            }
        }
        
        public void onClick(int button) {
            if (button == 0) {
                CatalystConfig config = CatalystConfig.getInstance();
                
                if (featureKey.equals("auto_sprint")) config.autoSprintEnabled = !config.autoSprintEnabled;
                else if (featureKey.equals("auto_swim")) config.autoSwimEnabled = !config.autoSwimEnabled;
                else if (featureKey.equals("gamma_override")) config.gammaOverrideEnabled = !config.gammaOverrideEnabled;
                else if (featureKey.equals("auto_tool")) config.autoToolEnabled = !config.autoToolEnabled;
                else if (featureKey.equals("auto_weapon")) config.autoWeaponEnabled = !config.autoWeaponEnabled;
                else if (featureKey.equals("auto_door")) config.autoDoorEnabled = !config.autoDoorEnabled;
                else if (featureKey.equals("auto_water_bucket")) config.autoWaterBucketEnabled = !config.autoWaterBucketEnabled;
                
                config.save();
            } else if (button == 1) {
                panel.getParentScreen().minecraft.setScreen(new KeybindConfigScreen(panel.getParentScreen(), keybindKey));
            }
        }
    }
}
