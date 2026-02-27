package org.alku.catalyst.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.List;

public class CatalystConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PANEL_HEADER_HEIGHT = 24;
    private static final int CORNER_RADIUS = 10;
    
    private final Screen parent;
    private List<ModulePanel> panels = new ArrayList<>();
    
    private ModulePanel draggingPanel = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    
    private boolean isDraggingSlider = false;
    private int sliderButtonIndex = -1;
    
    public CatalystConfigScreen(Screen parent) {
        super(Component.translatable("catalyst.gui.title"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        panels.clear();
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        int startX = 10;
        int startY = 10;
        int spacing = 12;
        
        ModulePanel movementPanel = new ModulePanel(
            config.getPanelX("Movement", startX),
            config.getPanelY("Movement", startY),
            "Movement", this);
        movementPanel.addModule("auto_sprint", "toggle_auto_sprint", false);
        movementPanel.addModule("auto_swim", "toggle_auto_swim", false);
        panels.add(movementPanel);
        
        ModulePanel combatPanel = new ModulePanel(
            config.getPanelX("Combat", startX + (PANEL_WIDTH + spacing)),
            config.getPanelY("Combat", startY),
            "Combat", this);
        combatPanel.addModule("auto_weapon", "toggle_auto_weapon", false);
        panels.add(combatPanel);
        
        ModulePanel playerPanel = new ModulePanel(
            config.getPanelX("Player", startX + (PANEL_WIDTH + spacing) * 2),
            config.getPanelY("Player", startY),
            "Player", this);
        playerPanel.addModule("gamma_override", "toggle_gamma_override", true);
        playerPanel.addModule("auto_door", "toggle_auto_door", false);
        playerPanel.addModule("auto_water_bucket", "toggle_auto_water_bucket", false);
        panels.add(playerPanel);
        
        ModulePanel toolsPanel = new ModulePanel(
            config.getPanelX("Tools", startX + (PANEL_WIDTH + spacing) * 3),
            config.getPanelY("Tools", startY),
            "Tools", this);
        toolsPanel.addModule("auto_tool", "toggle_auto_tool", false);
        panels.add(toolsPanel);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        float scale = getScale();
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        
        int scaledMouseX = (int)(mouseX / scale);
        int scaledMouseY = (int)(mouseY / scale);
        
        for (ModulePanel panel : panels) {
            panel.render(graphics, scaledMouseX, scaledMouseY, partialTick);
        }
        
        graphics.pose().popPose();
    }
    
    private int getScaledMouseX(double mouseX) {
        return (int)(mouseX / getScale());
    }
    
    private int getScaledMouseY(double mouseY) {
        return (int)(mouseY / getScale());
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scaledX = getScaledMouseX(mouseX);
        int scaledY = getScaledMouseY(mouseY);
        
        for (int i = panels.size() - 1; i >= 0; i--) {
            ModulePanel panel = panels.get(i);
            if (panel.isMouseOver(scaledX, scaledY)) {
                if (button == 0 && panel.isMouseOverHeader(scaledX, scaledY)) {
                    draggingPanel = panel;
                    dragOffsetX = scaledX - panel.getX();
                    dragOffsetY = scaledY - panel.getY();
                    panels.remove(i);
                    panels.add(panel);
                    return true;
                }
                if (panel.mouseClicked(scaledX, scaledY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (draggingPanel != null) {
                draggingPanel = null;
                savePanelPositions();
                return true;
            }
            if (isDraggingSlider) {
                isDraggingSlider = false;
                sliderButtonIndex = -1;
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int scaledX = getScaledMouseX(mouseX);
        int scaledY = getScaledMouseY(mouseY);
        
        if (isDraggingSlider && button == 0) {
            for (ModulePanel panel : panels) {
                int expandedIndex = panel.getExpandedButton();
                if (expandedIndex >= 0 && expandedIndex < panel.buttons.size()) {
                    ModuleButton btn = panel.buttons.get(expandedIndex);
                    if (btn.featureKey.equals("gamma_override")) {
                        updateSlider(panel, scaledX);
                        return true;
                    }
                }
            }
        }
        
        if (draggingPanel != null && button == 0) {
            draggingPanel.setPosition((int)(scaledX - dragOffsetX), (int)(scaledY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    private void updateSlider(ModulePanel panel, int mouseX) {
        int sliderX = panel.getX() + 8;
        int sliderWidth = PANEL_WIDTH - 16;
        double newValue = ((mouseX - sliderX) / (double)sliderWidth) * 100.0;
        newValue = Math.max(0.0, Math.min(100.0, newValue));
        CatalystConfig.getInstance().gammaValue = newValue;
        CatalystConfig.getInstance().save();
    }
    
    public void startSliderDrag(int buttonIndex) {
        isDraggingSlider = true;
        sliderButtonIndex = buttonIndex;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        CatalystConfig config = CatalystConfig.getInstance();
        config.guiScale += delta * 0.1f;
        config.guiScale = Math.max(0.5f, Math.min(2.0f, config.guiScale));
        config.save();
        return true;
    }
    
    private void savePanelPositions() {
        CatalystConfig config = CatalystConfig.getInstance();
        for (ModulePanel panel : panels) {
            config.setPanelPosition(panel.getName(), panel.getX(), panel.getY());
        }
        config.save();
    }
    
    @Override
    public void onClose() {
        savePanelPositions();
        CatalystConfig.getInstance().save();
        this.minecraft.setScreen(parent);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public float getScale() {
        return CatalystConfig.getInstance().guiScale;
    }
    
    public static class ModulePanel {
        private int x;
        private int y;
        private final String name;
        private final CatalystConfigScreen parent;
        private final List<ModuleButton> buttons = new ArrayList<>();
        private int height;
        private int expandedButton = -1;
        
        public ModulePanel(int x, int y, String name, CatalystConfigScreen parent) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.parent = parent;
            this.height = PANEL_HEADER_HEIGHT;
        }
        
        public void addModule(String featureKey, String keybindKey, boolean hasConfig) {
            buttons.add(new ModuleButton(featureKey, keybindKey, this, hasConfig));
            height += BUTTON_HEIGHT;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public String getName() { return name; }
        public void setPosition(int newX, int newY) { this.x = newX; this.y = newY; }
        public int getExpandedButton() { return expandedButton; }
        public void setExpandedButton(int index) { this.expandedButton = index; }
        
        public boolean isMouseOver(double mouseX, double mouseY) {
            int totalHeight = height;
            if (expandedButton >= 0 && expandedButton < buttons.size()) {
                totalHeight += buttons.get(expandedButton).getConfigHeight();
            }
            return mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + totalHeight;
        }
        
        public boolean isMouseOverHeader(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + PANEL_HEADER_HEIGHT;
        }
        
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            drawModernRoundedRect(graphics, x, y, x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT, CORNER_RADIUS, 0xE0222222, true, false);
            
            graphics.drawCenteredString(parent.minecraft.font, Component.literal(name), x + PANEL_WIDTH / 2, y + 7, 0xFFFFFFFF);
            
            int currentY = y + PANEL_HEADER_HEIGHT;
            for (int i = 0; i < buttons.size(); i++) {
                ModuleButton button = buttons.get(i);
                boolean isLast = (i == buttons.size() - 1) && expandedButton != i;
                boolean isExpanded = (i == expandedButton);
                
                button.render(graphics, x, currentY, mouseX, mouseY, isLast, isExpanded);
                
                if (isExpanded) {
                    currentY += button.getConfigHeight();
                }
                currentY += BUTTON_HEIGHT;
            }
        }
        
        private void drawModernRoundedRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color, boolean topRound, boolean bottomRound) {
            graphics.fill(x1 + radius, y1, x2 - radius, y2, color);
            graphics.fill(x1, y1 + radius, x2, y2 - radius, color);
            
            if (topRound) {
                for (int dy = 0; dy < radius; dy++) {
                    for (int dx = 0; dx < radius; dx++) {
                        double dist = Math.sqrt((radius - dx - 0.5) * (radius - dx - 0.5) + (radius - dy - 0.5) * (radius - dy - 0.5));
                        if (dist < radius) {
                            graphics.fill(x1 + dx, y1 + dy, x1 + dx + 1, y1 + dy + 1, color);
                            graphics.fill(x2 - dx - 1, y1 + dy, x2 - dx, y1 + dy + 1, color);
                        }
                    }
                }
            }
            if (bottomRound) {
                for (int dy = 0; dy < radius; dy++) {
                    for (int dx = 0; dx < radius; dx++) {
                        double dist = Math.sqrt((radius - dx - 0.5) * (radius - dx - 0.5) + (radius - dy - 0.5) * (radius - dy - 0.5));
                        if (dist < radius) {
                            graphics.fill(x1 + dx, y2 - dy - 1, x1 + dx + 1, y2 - dy, color);
                            graphics.fill(x2 - dx - 1, y2 - dy - 1, x2 - dx, y2 - dy, color);
                        }
                    }
                }
            }
        }
        
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int currentY = y + PANEL_HEADER_HEIGHT;
            for (int i = 0; i < buttons.size(); i++) {
                int btnHeight = BUTTON_HEIGHT;
                if (i == expandedButton) {
                    btnHeight += buttons.get(i).getConfigHeight();
                }
                
                if (mouseY >= currentY && mouseY <= currentY + btnHeight) {
                    if (buttons.get(i).mouseClicked(mouseX, mouseY, button, currentY)) {
                        return true;
                    }
                }
                currentY += btnHeight;
            }
            return false;
        }
        
        public CatalystConfigScreen getParentScreen() { return parent; }
    }
    
    public static class ModuleButton {
        private final String featureKey;
        private final String keybindKey;
        private final ModulePanel panel;
        private final boolean hasConfig;
        
        public ModuleButton(String featureKey, String keybindKey, ModulePanel panel, boolean hasConfig) {
            this.featureKey = featureKey;
            this.keybindKey = keybindKey;
            this.panel = panel;
            this.hasConfig = hasConfig;
        }
        
        public int getConfigHeight() {
            if (featureKey.equals("gamma_override")) {
                return 50;
            }
            return 0;
        }
        
        public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, boolean isLast, boolean isExpanded) {
            CatalystConfig config = CatalystConfig.getInstance();
            boolean isEnabled = getEnabledState(config);
            
            int bgColor = isEnabled ? 0xE02B4B2B : 0xE04B2B2B;
            int hoverColor = isEnabled ? 0xE03D6B3D : 0xE06B3D3D;
            
            boolean isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
            int drawColor = isHovered ? hoverColor : bgColor;
            
            drawModernButton(graphics, x, y, x + PANEL_WIDTH, y + BUTTON_HEIGHT, CORNER_RADIUS, drawColor, isLast && !isExpanded);
            
            int statusColor = isEnabled ? 0xFF55FF55 : 0xFFFF5555;
            graphics.fill(x + 4, y + 6, x + 8, y + BUTTON_HEIGHT - 6, statusColor);
            
            Component featureName = Component.translatable("catalyst.feature." + featureKey);
            graphics.drawString(panel.getParentScreen().minecraft.font, featureName, x + 12, y + 6, 0xFFFFFFFF);
            
            String keybindText = KeybindManager.getBoundKey(keybindKey);
            Component keybindComponent = Component.literal("[" + keybindText + "]");
            int textWidth = panel.getParentScreen().minecraft.font.width(keybindComponent);
            graphics.drawString(panel.getParentScreen().minecraft.font, keybindComponent, x + PANEL_WIDTH - 5 - textWidth, y + 6, 0xFFAAAAAA);
            
            if (isExpanded && featureKey.equals("gamma_override")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 50, 0xE01A1A1A);
                
                graphics.drawString(panel.getParentScreen().minecraft.font, "Gamma: " + String.format("%.1f", config.gammaValue), x + 8, configY + 6, 0xFFCCCCCC);
                
                int sliderX = x + 8;
                int sliderWidth = PANEL_WIDTH - 16;
                int sliderY = configY + 20;
                
                graphics.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 8, 0xFF333333);
                
                int filledWidth = (int)(sliderWidth * (config.gammaValue / 100.0));
                graphics.fill(sliderX, sliderY, sliderX + filledWidth, sliderY + 8, 0xFF44AA44);
                
                int handleX = sliderX + filledWidth - 4;
                graphics.fill(handleX, sliderY - 2, handleX + 8, sliderY + 10, 0xFFFFFFFF);
                
                String modeText = config.nightVisionMode ? "Mode: Full Bright" : "Mode: Custom";
                graphics.drawString(panel.getParentScreen().minecraft.font, modeText, x + 8, configY + 34, 0xFF55FF55);
            }
        }
        
        private boolean getEnabledState(CatalystConfig config) {
            if (featureKey.equals("auto_sprint")) return config.autoSprintEnabled;
            if (featureKey.equals("auto_swim")) return config.autoSwimEnabled;
            if (featureKey.equals("gamma_override")) return config.gammaOverrideEnabled;
            if (featureKey.equals("auto_tool")) return config.autoToolEnabled;
            if (featureKey.equals("auto_weapon")) return config.autoWeaponEnabled;
            if (featureKey.equals("auto_door")) return config.autoDoorEnabled;
            if (featureKey.equals("auto_water_bucket")) return config.autoWaterBucketEnabled;
            return false;
        }
        
        private void drawModernButton(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color, boolean bottomRound) {
            graphics.fill(x1, y1, x2, y2, color);
            
            if (bottomRound) {
                for (int dy = 0; dy < radius; dy++) {
                    for (int dx = 0; dx < radius; dx++) {
                        double dist = Math.sqrt((radius - dx - 0.5) * (radius - dx - 0.5) + (radius - dy - 0.5) * (radius - dy - 0.5));
                        if (dist < radius) {
                            graphics.fill(x1 + dx, y2 - dy - 1, x1 + dx + 1, y2 - dy, color);
                            graphics.fill(x2 - dx - 1, y2 - dy - 1, x2 - dx, y2 - dy, color);
                        }
                    }
                }
            }
        }
        
        public boolean mouseClicked(double mouseX, double mouseY, int button, int buttonY) {
            if (button == 0) {
                if (mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT) {
                    toggleFeature();
                    return true;
                }
                
                if (featureKey.equals("gamma_override") && panel.getExpandedButton() >= 0) {
                    int configY = buttonY + BUTTON_HEIGHT;
                    
                    if (mouseY >= configY + 18 && mouseY <= configY + 30) {
                        int sliderX = panel.getX() + 8;
                        int sliderWidth = PANEL_WIDTH - 16;
                        double newValue = ((mouseX - sliderX) / (double)sliderWidth) * 100.0;
                        newValue = Math.max(0.0, Math.min(100.0, newValue));
                        CatalystConfig.getInstance().gammaValue = newValue;
                        CatalystConfig.getInstance().save();
                        
                        int myIndex = -1;
                        for (int i = 0; i < panel.buttons.size(); i++) {
                            if (panel.buttons.get(i) == this) {
                                myIndex = i;
                                break;
                            }
                        }
                        panel.getParentScreen().startSliderDrag(myIndex);
                        return true;
                    }
                    
                    if (mouseY >= configY + 34 && mouseY <= configY + 46) {
                        CatalystConfig.getInstance().nightVisionMode = !CatalystConfig.getInstance().nightVisionMode;
                        if (CatalystConfig.getInstance().nightVisionMode) {
                            CatalystConfig.getInstance().gammaValue = 100.0;
                        }
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                }
            } else if (button == 1 && hasConfig) {
                int myIndex = -1;
                for (int i = 0; i < panel.buttons.size(); i++) {
                    if (panel.buttons.get(i) == this) {
                        myIndex = i;
                        break;
                    }
                }
                if (panel.getExpandedButton() == myIndex) {
                    panel.setExpandedButton(-1);
                } else {
                    panel.setExpandedButton(myIndex);
                }
                return true;
            }
            return false;
        }
        
        private void toggleFeature() {
            CatalystConfig config = CatalystConfig.getInstance();
            
            if (featureKey.equals("auto_sprint")) config.autoSprintEnabled = !config.autoSprintEnabled;
            else if (featureKey.equals("auto_swim")) config.autoSwimEnabled = !config.autoSwimEnabled;
            else if (featureKey.equals("gamma_override")) config.gammaOverrideEnabled = !config.gammaOverrideEnabled;
            else if (featureKey.equals("auto_tool")) config.autoToolEnabled = !config.autoToolEnabled;
            else if (featureKey.equals("auto_weapon")) config.autoWeaponEnabled = !config.autoWeaponEnabled;
            else if (featureKey.equals("auto_door")) config.autoDoorEnabled = !config.autoDoorEnabled;
            else if (featureKey.equals("auto_water_bucket")) config.autoWaterBucketEnabled = !config.autoWaterBucketEnabled;
            
            config.save();
        }
    }
}
