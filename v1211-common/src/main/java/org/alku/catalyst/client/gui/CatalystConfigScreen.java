package org.alku.catalyst.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.client.feature.GammaOverride;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.List;

public class CatalystConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PANEL_HEADER_HEIGHT = 24;
    private static final double MAX_GAMMA = 1500.0;
    
    private static final int COLOR_BG_HEADER = 0xB0181818;
    private static final int COLOR_BG_ENABLED = 0xB01A2A1A;
    private static final int COLOR_BG_DISABLED = 0xB0141414;
    private static final int COLOR_BG_HOVER_ENABLED = 0xB0223622;
    private static final int COLOR_BG_HOVER_DISABLED = 0xB01E1E1E;
    private static final int COLOR_BG_CONFIG = 0xB0101010;
    private static final int COLOR_STATUS_ON = 0xFF44DD44;
    private static final int COLOR_STATUS_OFF = 0xFF444444;
    private static final int COLOR_BORDER = 0xFF2A2A2A;
    
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
        combatPanel.addModule("auto_weapon", "toggle_auto_weapon", true);
        panels.add(combatPanel);
        
        ModulePanel playerPanel = new ModulePanel(
            config.getPanelX("Player", startX + (PANEL_WIDTH + spacing) * 2),
            config.getPanelY("Player", startY),
            "Player", this);
        playerPanel.addModule("gamma_override", "toggle_gamma_override", true);
        playerPanel.addModule("auto_door", "toggle_auto_door", false);
        playerPanel.addModule("auto_water_bucket", "toggle_auto_water_bucket", false);
        playerPanel.addModule("entity_xray", "toggle_entity_xray", true);
        playerPanel.addModule("mini_hud", "toggle_mini_hud", true);
        panels.add(playerPanel);
        
        ModulePanel toolsPanel = new ModulePanel(
            config.getPanelX("Tools", startX + (PANEL_WIDTH + spacing) * 3),
            config.getPanelY("Tools", startY),
            "Tools", this);
        toolsPanel.addModule("auto_tool", "toggle_auto_tool", true);
        toolsPanel.addModule("inventory_sorter", "sort_inventory", true);
        toolsPanel.addModule("mouse_gestures", "toggle_mouse_gestures", true);
        panels.add(toolsPanel);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        
        float scale = getScale();
        float centerX = this.width / 2.0f;
        float centerY = this.height / 2.0f;
        
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-centerX, -centerY, 0);
        
        int scaledMouseX = getScaledMouseX(mouseX);
        int scaledMouseY = getScaledMouseY(mouseY);
        
        for (ModulePanel panel : panels) {
            panel.render(graphics, scaledMouseX, scaledMouseY, partialTick);
        }
        
        graphics.pose().popPose();
    }
    
    private int getScaledMouseX(double mouseX) {
        float scale = getScale();
        float centerX = this.width / 2.0f;
        return (int)(centerX + (mouseX - centerX) / scale);
    }
    
    private int getScaledMouseY(double mouseY) {
        float scale = getScale();
        float centerY = this.height / 2.0f;
        return (int)(centerY + (mouseY - centerY) / scale);
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
        double newValue = ((mouseX - sliderX) / (double)sliderWidth) * MAX_GAMMA;
        newValue = Math.max(0.0, Math.min(MAX_GAMMA, newValue));
        CatalystConfig.getInstance().gammaValue = newValue;
        CatalystConfig.getInstance().save();
        if (CatalystConfig.getInstance().gammaOverrideEnabled) {
            GammaOverride.setGamma(minecraft, newValue);
        }
    }
    
    public void startSliderDrag(int buttonIndex) {
        isDraggingSlider = true;
        sliderButtonIndex = buttonIndex;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
        System.out.println("[Catalyst] CatalystConfigScreen.mouseScrolled called: delta=" + delta + ", delta2=" + delta2);
        CatalystConfig config = CatalystConfig.getInstance();
        float oldScale = config.guiScale;
        config.guiScale += (float) delta * 0.1f;
        config.guiScale = Math.max(0.5f, Math.min(2.0f, config.guiScale));
        
        if (oldScale != config.guiScale) {
            System.out.println("[Catalyst] GUI scale changed: " + oldScale + " -> " + config.guiScale);
            config.save();
            init();
        }
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
            graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT, CatalystConfigScreen.COLOR_BG_HEADER);
            graphics.fill(x, y, x + 1, y + PANEL_HEADER_HEIGHT, CatalystConfigScreen.COLOR_BORDER);
            graphics.fill(x + PANEL_WIDTH - 1, y, x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT, CatalystConfigScreen.COLOR_BORDER);
            graphics.fill(x, y, x + PANEL_WIDTH, y + 1, CatalystConfigScreen.COLOR_BORDER);
            
            graphics.drawCenteredString(parent.minecraft.font, Component.literal(name), x + PANEL_WIDTH / 2, y + 7, 0xFFFFFFFF);
            
            int currentY = y + PANEL_HEADER_HEIGHT;
            for (int i = 0; i < buttons.size(); i++) {
                ModuleButton button = buttons.get(i);
                boolean isLast = (i == buttons.size() - 1);
                
                button.render(graphics, x, currentY, mouseX, mouseY, isLast);
                
                if (i == expandedButton) {
                    currentY += button.getConfigHeight();
                }
                currentY += BUTTON_HEIGHT;
            }
            
            int totalHeight = currentY - y;
            graphics.fill(x, y + totalHeight - 1, x + PANEL_WIDTH, y + totalHeight, CatalystConfigScreen.COLOR_BORDER);
            graphics.fill(x, y + PANEL_HEADER_HEIGHT, x + 1, y + totalHeight, CatalystConfigScreen.COLOR_BORDER);
            graphics.fill(x + PANEL_WIDTH - 1, y + PANEL_HEADER_HEIGHT, x + PANEL_WIDTH, y + totalHeight, CatalystConfigScreen.COLOR_BORDER);
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
            if (featureKey.equals("auto_weapon")) {
                return 70;
            }
            if (featureKey.equals("auto_tool")) {
                return 70;
            }
            if (featureKey.equals("entity_xray")) {
                return 0;
            }
            if (featureKey.equals("mini_hud")) {
                return 80;
            }
            if (featureKey.equals("inventory_sorter")) {
                return 90;
            }
            if (featureKey.equals("mouse_gestures")) {
                return 100;
            }
            return 0;
        }
        
        public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, boolean isLast) {
            CatalystConfig config = CatalystConfig.getInstance();
            boolean isEnabled = getEnabledState(config);
            boolean isExpanded = (panel.getExpandedButton() >= 0 && panel.buttons.get(panel.getExpandedButton()) == this);
            
            int bgColor = isEnabled ? CatalystConfigScreen.COLOR_BG_ENABLED : CatalystConfigScreen.COLOR_BG_DISABLED;
            int hoverColor = isEnabled ? CatalystConfigScreen.COLOR_BG_HOVER_ENABLED : CatalystConfigScreen.COLOR_BG_HOVER_DISABLED;
            
            boolean isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
            int drawColor = isHovered ? hoverColor : bgColor;
            
            graphics.fill(x, y, x + PANEL_WIDTH, y + BUTTON_HEIGHT, drawColor);
            
            int statusColor = isEnabled ? CatalystConfigScreen.COLOR_STATUS_ON : CatalystConfigScreen.COLOR_STATUS_OFF;
            graphics.fill(x + 4, y + 6, x + 8, y + BUTTON_HEIGHT - 6, statusColor);
            
            Component featureName = Component.translatable("catalyst.feature." + featureKey);
            graphics.drawString(panel.getParentScreen().minecraft.font, featureName, x + 12, y + 6, 0xFFFFFFFF);
            
            String keybindText = KeybindManager.getBoundKey(keybindKey);
            Component keybindComponent = Component.literal("[" + keybindText + "]");
            int textWidth = panel.getParentScreen().minecraft.font.width(keybindComponent);
            graphics.drawString(panel.getParentScreen().minecraft.font, keybindComponent, x + PANEL_WIDTH - 5 - textWidth, y + 6, 0xFFAAAAAA);
            
            if (isExpanded && featureKey.equals("gamma_override")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 50, CatalystConfigScreen.COLOR_BG_CONFIG);
                
                graphics.drawString(panel.getParentScreen().minecraft.font, "Gamma: " + String.format("%.1f", config.gammaValue), x + 8, configY + 6, 0xFFCCCCCC);
                
                int sliderX = x + 8;
                int sliderWidth = PANEL_WIDTH - 16;
                int sliderY = configY + 20;
                
                graphics.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 8, 0xFF333333);
                
                int filledWidth = (int)(sliderWidth * (config.gammaValue / MAX_GAMMA));
                graphics.fill(sliderX, sliderY, sliderX + filledWidth, sliderY + 8, 0xFF44AA44);
                
                int handleX = sliderX + filledWidth - 4;
                graphics.fill(handleX, sliderY - 2, handleX + 8, sliderY + 10, 0xFFFFFFFF);
                
                String modeText = config.nightVisionMode ? "Mode: Full Bright" : "Mode: Custom";
                graphics.drawString(panel.getParentScreen().minecraft.font, modeText, x + 8, configY + 34, 0xFF55FF55);
            }
            
            if (isExpanded && featureKey.equals("auto_weapon")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 70, CatalystConfigScreen.COLOR_BG_CONFIG);
                
                String restoreText = Component.translatable("catalyst.gui.restore_slot", 
                    config.autoWeaponRestore ? Component.translatable("catalyst.gui.on").getString() : Component.translatable("catalyst.gui.off").getString()).getString();
                int restoreColor = config.autoWeaponRestore ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, restoreText, x + 8, configY + 6, restoreColor);
                
                int lockedSlot = config.autoWeaponLockedSlot;
                String slotText = lockedSlot >= 0 ? 
                    Component.translatable("catalyst.gui.locked_slot", lockedSlot + 1).getString() :
                    Component.translatable("catalyst.gui.no_locked_slot").getString();
                graphics.drawString(panel.getParentScreen().minecraft.font, slotText, x + 8, configY + 20, 0xFFCCCCCC);
                
                int slotStartX = x + 8;
                int slotY = configY + 34;
                int slotSize = 12;
                int slotSpacing = 2;
                
                for (int i = 0; i < 9; i++) {
                    int slotX = slotStartX + i * (slotSize + slotSpacing);
                    boolean isSelected = (i == lockedSlot);
                    int slotColor = isSelected ? 0xFF44AA44 : 0xFF333333;
                    int borderColor = isSelected ? 0xFF55FF55 : 0xFF555555;
                    
                    graphics.fill(slotX - 1, slotY - 1, slotX + slotSize + 1, slotY + slotSize + 1, borderColor);
                    graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, slotColor);
                    
                    String slotNum = String.valueOf(i + 1);
                    graphics.drawCenteredString(panel.getParentScreen().minecraft.font, slotNum, slotX + slotSize / 2, slotY + 2, 0xFFFFFFFF);
                }
                
                String hint = Component.translatable("catalyst.gui.click_to_lock").getString();
                graphics.drawString(panel.getParentScreen().minecraft.font, hint, x + 8, configY + 52, 0xFF888888);
            }
            
            if (isExpanded && featureKey.equals("auto_tool")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 70, CatalystConfigScreen.COLOR_BG_CONFIG);
                
                String restoreText = Component.translatable("catalyst.gui.restore_slot", 
                    config.autoToolRestore ? Component.translatable("catalyst.gui.on").getString() : Component.translatable("catalyst.gui.off").getString()).getString();
                int restoreColor = config.autoToolRestore ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, restoreText, x + 8, configY + 6, restoreColor);
                
                int lockedSlot = config.autoToolLockedSlot;
                String slotText = lockedSlot >= 0 ? 
                    Component.translatable("catalyst.gui.locked_slot", lockedSlot + 1).getString() :
                    Component.translatable("catalyst.gui.no_locked_slot").getString();
                graphics.drawString(panel.getParentScreen().minecraft.font, slotText, x + 8, configY + 20, 0xFFCCCCCC);
                
                int slotStartX = x + 8;
                int slotY = configY + 34;
                int slotSize = 12;
                int slotSpacing = 2;
                
                for (int i = 0; i < 9; i++) {
                    int slotX = slotStartX + i * (slotSize + slotSpacing);
                    boolean isSelected = (i == lockedSlot);
                    int slotColor = isSelected ? 0xFF44AA44 : 0xFF333333;
                    int borderColor = isSelected ? 0xFF55FF55 : 0xFF555555;
                    
                    graphics.fill(slotX - 1, slotY - 1, slotX + slotSize + 1, slotY + slotSize + 1, borderColor);
                    graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, slotColor);
                    
                    String slotNum = String.valueOf(i + 1);
                    graphics.drawCenteredString(panel.getParentScreen().minecraft.font, slotNum, slotX + slotSize / 2, slotY + 2, 0xFFFFFFFF);
                }
                
                String hint = Component.translatable("catalyst.gui.click_to_lock").getString();
                graphics.drawString(panel.getParentScreen().minecraft.font, hint, x + 8, configY + 52, 0xFF888888);
            }

            if (isExpanded && featureKey.equals("mini_hud")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 80, CatalystConfigScreen.COLOR_BG_CONFIG);
                
                String onText = Component.translatable("catalyst.gui.on").getString();
                String offText = Component.translatable("catalyst.gui.off").getString();
                
                String coordsText = Component.translatable("catalyst.gui.mini_hud_coords").getString() + ": " + (config.miniHudShowCoords ? onText : offText);
                int coordsColor = config.miniHudShowCoords ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, coordsText, x + 8, configY + 6, coordsColor);
                
                String biomeText = Component.translatable("catalyst.gui.mini_hud_biome").getString() + ": " + (config.miniHudShowBiome ? onText : offText);
                int biomeColor = config.miniHudShowBiome ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, biomeText, x + 8, configY + 20, biomeColor);
                
                String timeText = Component.translatable("catalyst.gui.mini_hud_time").getString() + ": " + (config.miniHudShowTime ? onText : offText);
                int timeColor = config.miniHudShowTime ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, timeText, x + 8, configY + 34, timeColor);
                
                String dayText = Component.translatable("catalyst.gui.mini_hud_day").getString() + ": " + (config.miniHudShowDayCount ? onText : offText);
                int dayColor = config.miniHudShowDayCount ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, dayText, x + 8, configY + 48, dayColor);
                
                String entityText = Component.translatable("catalyst.gui.mini_hud_entities").getString() + ": " + (config.miniHudShowEntityCount ? onText : offText);
                int entityColor = config.miniHudShowEntityCount ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, entityText, x + 8, configY + 62, entityColor);
            }

            if (isExpanded && featureKey.equals("inventory_sorter")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 90, CatalystConfigScreen.COLOR_BG_CONFIG);
                
                String autoSortText = Component.translatable("catalyst.gui.auto_sort_on_open", 
                    config.autoSortOnOpen ? Component.translatable("catalyst.gui.on").getString() : Component.translatable("catalyst.gui.off").getString()).getString();
                int autoSortColor = config.autoSortOnOpen ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, autoSortText, x + 8, configY + 6, autoSortColor);
                
                String sortHotbarText = Component.translatable("catalyst.gui.sort_hotbar", 
                    config.sortHotbar ? Component.translatable("catalyst.gui.on").getString() : Component.translatable("catalyst.gui.off").getString()).getString();
                int sortHotbarColor = config.sortHotbar ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, sortHotbarText, x + 8, configY + 20, sortHotbarColor);
                
                String sortPlayerText = Component.translatable("catalyst.gui.sort_player_inv", 
                    config.sortPlayerInventoryInContainer ? Component.translatable("catalyst.gui.on").getString() : Component.translatable("catalyst.gui.off").getString()).getString();
                int sortPlayerColor = config.sortPlayerInventoryInContainer ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, sortPlayerText, x + 8, configY + 34, sortPlayerColor);
                
                String[] modes = {"Category", "Name", "ID"};
                String modeText = Component.translatable("catalyst.gui.sort_mode", modes[config.sortMode]).getString();
                graphics.drawString(panel.getParentScreen().minecraft.font, modeText, x + 8, configY + 48, 0xFFCCCCCC);
                
                int modeBtnX = x + 8;
                int modeBtnY = configY + 62;
                int modeBtnWidth = (PANEL_WIDTH - 20) / 3;
                for (int i = 0; i < 3; i++) {
                    int btnX = modeBtnX + i * (modeBtnWidth + 4);
                    boolean isSelected = (config.sortMode == i);
                    int btnColor = isSelected ? 0xFF44AA44 : 0xFF333333;
                    int borderColor = isSelected ? 0xFF55FF55 : 0xFF555555;
                    
                    graphics.fill(btnX - 1, modeBtnY - 1, btnX + modeBtnWidth + 1, modeBtnY + 18, borderColor);
                    graphics.fill(btnX, modeBtnY, btnX + modeBtnWidth, modeBtnY + 17, btnColor);
                    
                    graphics.drawCenteredString(panel.getParentScreen().minecraft.font, modes[i], btnX + modeBtnWidth / 2, modeBtnY + 4, 0xFFFFFFFF);
                }
            }
            
            if (isExpanded && featureKey.equals("mouse_gestures")) {
                int configY = y + BUTTON_HEIGHT;
                graphics.fill(x, configY, x + PANEL_WIDTH, configY + 100, CatalystConfigScreen.COLOR_BG_CONFIG);
                
                String onText = Component.translatable("catalyst.gui.on").getString();
                String offText = Component.translatable("catalyst.gui.off").getString();
                
                String rmbText = Component.translatable("catalyst.gui.rmb_tweak").getString() + ": " + (config.rmbTweak ? onText : offText);
                int rmbColor = config.rmbTweak ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, rmbText, x + 8, configY + 6, rmbColor);
                
                String lmbWithItemText = Component.translatable("catalyst.gui.lmb_tweak_with_item").getString() + ": " + (config.lmbTweakWithItem ? onText : offText);
                int lmbWithItemColor = config.lmbTweakWithItem ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, lmbWithItemText, x + 8, configY + 20, lmbWithItemColor);
                
                String lmbNoItemText = Component.translatable("catalyst.gui.lmb_tweak_without_item").getString() + ": " + (config.lmbTweakWithoutItem ? onText : offText);
                int lmbNoItemColor = config.lmbTweakWithoutItem ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, lmbNoItemText, x + 8, configY + 34, lmbNoItemColor);
                
                String wheelText = Component.translatable("catalyst.gui.wheel_tweak").getString() + ": " + (config.wheelTweak ? onText : offText);
                int wheelColor = config.wheelTweak ? 0xFF55FF55 : 0xFF555555;
                graphics.drawString(panel.getParentScreen().minecraft.font, wheelText, x + 8, configY + 48, wheelColor);
                
                String[] directions = {
                    Component.translatable("catalyst.gui.scroll_normal").getString(),
                    Component.translatable("catalyst.gui.scroll_inverted").getString(),
                    Component.translatable("catalyst.gui.scroll_inventory").getString()
                };
                String dirText = Component.translatable("catalyst.gui.scroll_direction", directions[config.wheelScrollDirection]).getString();
                graphics.drawString(panel.getParentScreen().minecraft.font, dirText, x + 8, configY + 62, 0xFFCCCCCC);
                
                int dirBtnX = x + 8;
                int dirBtnY = configY + 76;
                int dirBtnWidth = (PANEL_WIDTH - 20) / 3;
                for (int i = 0; i < 3; i++) {
                    int btnX = dirBtnX + i * (dirBtnWidth + 4);
                    boolean isSelected = (config.wheelScrollDirection == i);
                    int btnColor = isSelected ? 0xFF44AA44 : 0xFF333333;
                    int borderColor = isSelected ? 0xFF55FF55 : 0xFF555555;
                    
                    graphics.fill(btnX - 1, dirBtnY - 1, btnX + dirBtnWidth + 1, dirBtnY + 18, borderColor);
                    graphics.fill(btnX, dirBtnY, btnX + dirBtnWidth, dirBtnY + 17, btnColor);
                    
                    graphics.drawCenteredString(panel.getParentScreen().minecraft.font, directions[i], btnX + dirBtnWidth / 2, dirBtnY + 4, 0xFFFFFFFF);
                }
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
            if (featureKey.equals("entity_xray")) return config.entityXrayEnabled;
            if (featureKey.equals("mini_hud")) return config.miniHudEnabled;
            if (featureKey.equals("inventory_sorter")) return config.inventorySorterEnabled;
            if (featureKey.equals("mouse_gestures")) return config.rmbTweak;
            return false;
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
                        double newValue = ((mouseX - sliderX) / (double)sliderWidth) * MAX_GAMMA;
                        newValue = Math.max(0.0, Math.min(MAX_GAMMA, newValue));
                        CatalystConfig.getInstance().gammaValue = newValue;
                        CatalystConfig.getInstance().save();
                        if (CatalystConfig.getInstance().gammaOverrideEnabled) {
                            GammaOverride.setGamma(panel.getParentScreen().minecraft, newValue);
                        }
                        
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
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                }
                
                if (featureKey.equals("auto_weapon") && panel.getExpandedButton() >= 0) {
                    int configY = buttonY + BUTTON_HEIGHT;
                    
                    if (mouseY >= configY + 4 && mouseY <= configY + 16) {
                        CatalystConfig.getInstance().autoWeaponRestore = !CatalystConfig.getInstance().autoWeaponRestore;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    int slotStartX = panel.getX() + 8;
                    int slotY = configY + 34;
                    int slotSize = 12;
                    int slotSpacing = 2;
                    
                    if (mouseY >= slotY - 1 && mouseY <= slotY + slotSize + 1) {
                        for (int i = 0; i < 9; i++) {
                            int slotX = slotStartX + i * (slotSize + slotSpacing);
                            if (mouseX >= slotX - 1 && mouseX <= slotX + slotSize + 1) {
                                int currentLocked = CatalystConfig.getInstance().autoWeaponLockedSlot;
                                if (currentLocked == i) {
                                    CatalystConfig.getInstance().autoWeaponLockedSlot = -1;
                                } else {
                                    CatalystConfig.getInstance().autoWeaponLockedSlot = i;
                                }
                                CatalystConfig.getInstance().save();
                                return true;
                            }
                        }
                    }
                }
                
                if (featureKey.equals("auto_tool") && panel.getExpandedButton() >= 0) {
                    int configY = buttonY + BUTTON_HEIGHT;
                    
                    if (mouseY >= configY + 4 && mouseY <= configY + 16) {
                        CatalystConfig.getInstance().autoToolRestore = !CatalystConfig.getInstance().autoToolRestore;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    int slotStartX = panel.getX() + 8;
                    int slotY = configY + 34;
                    int slotSize = 12;
                    int slotSpacing = 2;
                    
                    if (mouseY >= slotY - 1 && mouseY <= slotY + slotSize + 1) {
                        for (int i = 0; i < 9; i++) {
                            int slotX = slotStartX + i * (slotSize + slotSpacing);
                            if (mouseX >= slotX - 1 && mouseX <= slotX + slotSize + 1) {
                                int currentLocked = CatalystConfig.getInstance().autoToolLockedSlot;
                                if (currentLocked == i) {
                                    CatalystConfig.getInstance().autoToolLockedSlot = -1;
                                } else {
                                    CatalystConfig.getInstance().autoToolLockedSlot = i;
                                }
                                CatalystConfig.getInstance().save();
                                return true;
                            }
                        }
                    }
                }

                if (featureKey.equals("mini_hud") && panel.getExpandedButton() >= 0) {
                    int configY = buttonY + BUTTON_HEIGHT;
                    
                    if (mouseY >= configY + 4 && mouseY <= configY + 16) {
                        CatalystConfig.getInstance().miniHudShowCoords = !CatalystConfig.getInstance().miniHudShowCoords;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 18 && mouseY <= configY + 30) {
                        CatalystConfig.getInstance().miniHudShowBiome = !CatalystConfig.getInstance().miniHudShowBiome;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 32 && mouseY <= configY + 44) {
                        CatalystConfig.getInstance().miniHudShowTime = !CatalystConfig.getInstance().miniHudShowTime;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 46 && mouseY <= configY + 58) {
                        CatalystConfig.getInstance().miniHudShowDayCount = !CatalystConfig.getInstance().miniHudShowDayCount;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 60 && mouseY <= configY + 72) {
                        CatalystConfig.getInstance().miniHudShowEntityCount = !CatalystConfig.getInstance().miniHudShowEntityCount;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                }

                if (featureKey.equals("inventory_sorter") && panel.getExpandedButton() >= 0) {
                    int configY = buttonY + BUTTON_HEIGHT;
                    
                    if (mouseY >= configY + 4 && mouseY <= configY + 16) {
                        CatalystConfig.getInstance().autoSortOnOpen = !CatalystConfig.getInstance().autoSortOnOpen;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 18 && mouseY <= configY + 30) {
                        CatalystConfig.getInstance().sortHotbar = !CatalystConfig.getInstance().sortHotbar;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 32 && mouseY <= configY + 44) {
                        CatalystConfig.getInstance().sortPlayerInventoryInContainer = !CatalystConfig.getInstance().sortPlayerInventoryInContainer;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    int modeBtnX = panel.getX() + 8;
                    int modeBtnY = configY + 62;
                    int modeBtnWidth = (PANEL_WIDTH - 20) / 3;
                    
                    if (mouseY >= modeBtnY - 1 && mouseY <= modeBtnY + 18) {
                        for (int i = 0; i < 3; i++) {
                            int btnX = modeBtnX + i * (modeBtnWidth + 4);
                            if (mouseX >= btnX - 1 && mouseX <= btnX + modeBtnWidth + 1) {
                                CatalystConfig.getInstance().sortMode = i;
                                CatalystConfig.getInstance().save();
                                return true;
                            }
                        }
                    }
                }
                
                if (featureKey.equals("mouse_gestures") && panel.getExpandedButton() >= 0) {
                    int configY = buttonY + BUTTON_HEIGHT;
                    
                    if (mouseY >= configY + 4 && mouseY <= configY + 16) {
                        CatalystConfig.getInstance().rmbTweak = !CatalystConfig.getInstance().rmbTweak;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 18 && mouseY <= configY + 30) {
                        CatalystConfig.getInstance().lmbTweakWithItem = !CatalystConfig.getInstance().lmbTweakWithItem;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 32 && mouseY <= configY + 44) {
                        CatalystConfig.getInstance().lmbTweakWithoutItem = !CatalystConfig.getInstance().lmbTweakWithoutItem;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    if (mouseY >= configY + 46 && mouseY <= configY + 58) {
                        CatalystConfig.getInstance().wheelTweak = !CatalystConfig.getInstance().wheelTweak;
                        CatalystConfig.getInstance().save();
                        return true;
                    }
                    
                    int dirBtnX = panel.getX() + 8;
                    int dirBtnY = configY + 76;
                    int dirBtnWidth = (PANEL_WIDTH - 20) / 3;
                    
                    if (mouseY >= dirBtnY - 1 && mouseY <= dirBtnY + 18) {
                        for (int i = 0; i < 3; i++) {
                            int btnX = dirBtnX + i * (dirBtnWidth + 4);
                            if (mouseX >= btnX - 1 && mouseX <= btnX + dirBtnWidth + 1) {
                                CatalystConfig.getInstance().wheelScrollDirection = i;
                                CatalystConfig.getInstance().save();
                                return true;
                            }
                        }
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
            else if (featureKey.equals("entity_xray")) config.entityXrayEnabled = !config.entityXrayEnabled;
            else if (featureKey.equals("mini_hud")) config.miniHudEnabled = !config.miniHudEnabled;
            else if (featureKey.equals("inventory_sorter")) config.inventorySorterEnabled = !config.inventorySorterEnabled;
            else if (featureKey.equals("mouse_gestures")) config.rmbTweak = !config.rmbTweak;
            
            config.save();
        }
    }
}
