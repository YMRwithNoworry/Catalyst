package org.alku.catalyst.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeybindConfigScreen extends Screen {
    private final Screen parent;
    private final String keyName;
    
    public KeybindConfigScreen(Screen parent, String keyName) {
        super(Component.translatable("catalyst.gui.keybind_config"));
        this.parent = parent;
        this.keyName = keyName;
    }
    
    @Override
    protected void init() {
        super.init();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            KeybindManager.setKey(keyName, GLFW.GLFW_KEY_UNKNOWN);
            this.minecraft.setScreen(parent);
            return true;
        }
        
        KeybindManager.setKey(keyName, keyCode);
        this.minecraft.setScreen(parent);
        return true;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button >= 0 && button <= 2) {
            int keyCode = button + 100;
            KeybindManager.setKey(keyName, keyCode);
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        String currentKey = KeybindManager.getBoundKey(keyName);
        
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFF);
        
        Component keyNameText = Component.translatable("key.catalyst." + keyName);
        graphics.drawCenteredString(this.font, keyNameText, this.width / 2, this.height / 2 - 30, 0xAAAAAA);
        
        Component currentKeyText = Component.literal(currentKey).withStyle(style -> style.withColor(0xFFFF55));
        graphics.drawCenteredString(this.font, currentKeyText, this.width / 2, this.height / 2, 0xFFFFFF);
        
        graphics.drawCenteredString(this.font, Component.translatable("catalyst.gui.press_key"), this.width / 2, this.height / 2 + 40, 0x55FF55);
        graphics.drawCenteredString(this.font, Component.translatable("catalyst.gui.escape_to_clear"), this.width / 2, this.height / 2 + 60, 0xFF5555);
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
