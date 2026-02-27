package org.alku.catalyst.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.List;

public class CatalystConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 130;
    private static final int BUTTON_HEIGHT = 18;
    private static final int PANEL_HEADER_HEIGHT = 20;
    private static final int CORNER_RADIUS = 8;
    private static final ResourceLocation FONT_LIGHT = new ResourceLocation("catalyst", "alibaba_light");
    private static final ResourceLocation FONT_MEDIUM = new ResourceLocation("catalyst", "alibaba_medium");
    
    private final Screen parent;
    private List<ModulePanel> panels = new ArrayList<>();
    
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
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        int startX = 10;
        int startY = 10;
        int spacing = 15;
        
        ModulePanel movementPanel = new ModulePanel(
            config.getPanelX("Movement", startX),
            config.getPanelY("Movement", startY),
            "Movement", this);
        movementPanel.addModule("auto_sprint", "toggle_auto_sprint");
        movementPanel.addModule("auto_swim", "toggle_auto_swim");
        panels.add(movementPanel);
        
        ModulePanel combatPanel = new ModulePanel(
            config.getPanelX("Combat", startX + (PANEL_WIDTH + spacing)),
            config.getPanelY("Combat", startY),
            "Combat", this);
        combatPanel.addModule("auto_weapon", "toggle_auto_weapon");
        panels.add(combatPanel);
        
        ModulePanel playerPanel = new ModulePanel(
            config.getPanelX("Player", startX + (PANEL_WIDTH + spacing) * 2),
            config.getPanelY("Player", startY),
            "Player", this);
        playerPanel.addModule("gamma_override", "toggle_gamma_override");
        playerPanel.addModule("auto_door", "toggle_auto_door");
        playerPanel.addModule("auto_water_bucket", "toggle_auto_water_bucket");
        panels.add(playerPanel);
        
        ModulePanel toolsPanel = new ModulePanel(
            config.getPanelX("Tools", startX + (PANEL_WIDTH + spacing) * 3),
            config.getPanelY("Tools", startY),
            "Tools", this);
        toolsPanel.addModule("auto_tool", "toggle_auto_tool");
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
                if (button == 0) {
                    if (panel.isMouseOverHeader(scaledX, scaledY)) {
                        draggingPanel = panel;
                        dragOffsetX = scaledX - panel.getX();
                        dragOffsetY = scaledY - panel.getY();
                        panels.remove(i);
                        panels.add(panel);
                        return true;
                    }
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
        if (button == 0 && draggingPanel != null) {
            draggingPanel = null;
            savePanelPositions();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPanel != null && button == 0) {
            int scaledX = getScaledMouseX(mouseX);
            int scaledY = getScaledMouseY(mouseY);
            draggingPanel.setPosition((int)(scaledX - dragOffsetX), (int)(scaledY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
        
        public ModulePanel(int x, int y, String name, CatalystConfigScreen parent) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.parent = parent;
            this.height = PANEL_HEADER_HEIGHT;
        }
        
        public void addModule(String featureKey, String keybindKey) {
            buttons.add(new ModuleButton(featureKey, keybindKey, this));
            height += BUTTON_HEIGHT;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public String getName() {
            return name;
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
            drawRoundedTopRect(graphics, x, y, x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT, CORNER_RADIUS, 0xFF4444AA);
            
            Component title = Component.literal(name).withStyle(Style.EMPTY.withFont(FONT_MEDIUM));
            graphics.drawCenteredString(parent.minecraft.font, title, x + PANEL_WIDTH / 2, y + 5, 0xFFFFFF);
            
            int bodyHeight = height - PANEL_HEADER_HEIGHT;
            if (bodyHeight > 0) {
                graphics.fill(x, y + PANEL_HEADER_HEIGHT, x + PANEL_WIDTH, y + height - BUTTON_HEIGHT, 0x99333333);
            }
            
            int currentY = y + PANEL_HEADER_HEIGHT;
            for (int i = 0; i < buttons.size(); i++) {
                ModuleButton button = buttons.get(i);
                boolean isLast = (i == buttons.size() - 1);
                button.render(graphics, x, currentY, mouseX, mouseY, isLast);
                currentY += BUTTON_HEIGHT;
            }
        }
        
        private void drawRoundedTopRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color) {
            graphics.fill(x1 + radius, y1, x2 - radius, y2, color);
            graphics.fill(x1, y1 + radius, x1 + radius, y2, color);
            graphics.fill(x2 - radius, y1 + radius, x2, y2, color);
            
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
        
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + height) {
                int currentY = y + PANEL_HEADER_HEIGHT;
                for (ModuleButton moduleButton : buttons) {
                    if (mouseY >= currentY && mouseY <= currentY + BUTTON_HEIGHT) {
                        moduleButton.onClick(button);
                        return true;
                    }
                    currentY += BUTTON_HEIGHT;
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
        
        public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, boolean isLast) {
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
            
            boolean isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH && 
                              mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
            
            int drawColor = isHovered ? hoverColor : color;
            
            if (isLast) {
                drawRoundedBottomRect(graphics, x, y, x + PANEL_WIDTH, y + BUTTON_HEIGHT, CORNER_RADIUS, drawColor);
            } else {
                graphics.fill(x, y, x + PANEL_WIDTH, y + BUTTON_HEIGHT, drawColor);
            }
            
            Component featureName = Component.translatable("catalyst.feature." + featureKey)
                .withStyle(Style.EMPTY.withFont(FONT_LIGHT));
            graphics.drawString(panel.getParentScreen().minecraft.font, featureName, x + 5, y + 5, 0xFFFFFF);
            
            String keybindText = KeybindManager.getBoundKey(keybindKey);
            Component keybindComponent = Component.literal("[" + keybindText + "]")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA).withFont(FONT_LIGHT));
            int textWidth = panel.getParentScreen().minecraft.font.width(keybindComponent);
            graphics.drawString(panel.getParentScreen().minecraft.font, keybindComponent, 
                               x + PANEL_WIDTH - 5 - textWidth, y + 5, 0xFFFFFF);
        }
        
        private void drawRoundedBottomRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, int color) {
            graphics.fill(x1 + radius, y1, x2 - radius, y2, color);
            graphics.fill(x1, y1, x1 + radius, y2 - radius, color);
            graphics.fill(x2 - radius, y1, x2, y2 - radius, color);
            
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
