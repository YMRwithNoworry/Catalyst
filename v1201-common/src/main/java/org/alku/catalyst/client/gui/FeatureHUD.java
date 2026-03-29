package org.alku.catalyst.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureHUD {
    
    private static final Map<String, AnimationState> animations = new HashMap<>();
    private static final Map<String, Boolean> previousStates = new HashMap<>();
    private static final Map<String, Long> toggleTimes = new HashMap<>();
    
    private static final int SLIDE_DURATION = 300;
    private static final int TOGGLE_FLASH_DURATION = 500;
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 12;
    
    private static class AnimationState {
        long startTime;
        boolean showing;
        
        AnimationState(boolean showing) {
            this.showing = showing;
            this.startTime = System.currentTimeMillis();
        }
    }
    
    private static class FeatureInfo {
        String key;
        String name;
        boolean enabled;
        
        FeatureInfo(String key, String name, boolean enabled) {
            this.key = key;
            this.name = name;
            this.enabled = enabled;
        }
    }
    
    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        
        List<FeatureInfo> features = getEnabledFeatures();
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int y = 8;
        
        for (FeatureInfo feature : features) {
            AnimationState anim = animations.computeIfAbsent(feature.key, k -> new AnimationState(true));
            
            Boolean prevState = previousStates.get(feature.key);
            if (prevState == null) prevState = false;
            
            if (feature.enabled != prevState) {
                toggleTimes.put(feature.key, System.currentTimeMillis());
                previousStates.put(feature.key, feature.enabled);
            }
            
            if (feature.enabled) {
                if (!anim.showing) {
                    anim.showing = true;
                    anim.startTime = System.currentTimeMillis();
                }
            } else {
                if (anim.showing) {
                    anim.showing = false;
                    anim.startTime = System.currentTimeMillis();
                }
            }
            
            long elapsed = System.currentTimeMillis() - anim.startTime;
            double progress = Math.min(1.0, (double) elapsed / SLIDE_DURATION);
            
            double easedProgress;
            if (anim.showing) {
                easedProgress = easeOutBack(progress);
            } else {
                easedProgress = 1.0 - easeInBack(progress);
            }
            
            int textWidth = mc.font.width(feature.name);
            int totalWidth = textWidth + PADDING * 2;
            double offsetX = totalWidth * (1.0 - easedProgress);
            
            int x = (int) (screenWidth - totalWidth + offsetX);
            
            int alpha = (int) (255 * easedProgress);
            
            Long toggleTime = toggleTimes.get(feature.key);
            long toggleElapsed = toggleTime != null ? System.currentTimeMillis() - toggleTime : TOGGLE_FLASH_DURATION + 1;
            
            int bgColor;
            int textColor;
            
            if (toggleElapsed < TOGGLE_FLASH_DURATION) {
                double flashProgress = (double) toggleElapsed / TOGGLE_FLASH_DURATION;
                int flashAlpha = (int) (255 * (1.0 - flashProgress));
                int greenFlash = (0x44AA44 & 0xFFFFFF) | (flashAlpha << 24);
                bgColor = blendColors(0x80000000, greenFlash, 1.0 - flashProgress);
                textColor = blendColors(0xFFFFFFFF, 0xFF44FF44, 1.0 - flashProgress);
            } else {
                bgColor = (0x80000000) | ((alpha * 128 / 255) << 24);
                textColor = 0xFFFFFFFF;
            }
            
            graphics.fill(x, y, x + totalWidth, y + LINE_HEIGHT, bgColor);
            graphics.drawString(mc.font, feature.name, x + PADDING, y + 2, textColor);
            
            y += LINE_HEIGHT;
        }
        
        animations.entrySet().removeIf(entry -> {
            AnimationState anim = entry.getValue();
            if (!anim.showing) {
                long elapsed = System.currentTimeMillis() - anim.startTime;
                return elapsed > SLIDE_DURATION;
            }
            return false;
        });
        
        toggleTimes.entrySet().removeIf(entry -> {
            long elapsed = System.currentTimeMillis() - entry.getValue();
            return elapsed > TOGGLE_FLASH_DURATION;
        });
    }
    
    private static int blendColors(int color1, int color2, double factor) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int) (a1 * factor + a2 * (1.0 - factor));
        int r = (int) (r1 * factor + r2 * (1.0 - factor));
        int g = (int) (g1 * factor + g2 * (1.0 - factor));
        int b = (int) (b1 * factor + b2 * (1.0 - factor));
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private static List<FeatureInfo> getEnabledFeatures() {
        List<FeatureInfo> features = new ArrayList<>();
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (config.autoSprintEnabled) {
            features.add(new FeatureInfo("auto_sprint", 
                Component.translatable("catalyst.feature.auto_sprint").getString(), true));
        }
        if (config.autoSwimEnabled) {
            features.add(new FeatureInfo("auto_swim", 
                Component.translatable("catalyst.feature.auto_swim").getString(), true));
        }
        if (config.gammaOverrideEnabled) {
            features.add(new FeatureInfo("gamma_override", 
                Component.translatable("catalyst.feature.gamma_override").getString(), true));
        }
        if (config.autoToolEnabled) {
            features.add(new FeatureInfo("auto_tool", 
                Component.translatable("catalyst.feature.auto_tool").getString(), true));
        }
        if (config.autoWeaponEnabled) {
            features.add(new FeatureInfo("auto_weapon", 
                Component.translatable("catalyst.feature.auto_weapon").getString(), true));
        }
        if (config.triggerBotEnabled) {
            features.add(new FeatureInfo("trigger_bot", 
                Component.translatable("catalyst.feature.trigger_bot").getString(), true));
        }
        if (config.shieldBreakerEnabled) {
            features.add(new FeatureInfo("shield_breaker", 
                Component.translatable("catalyst.feature.shield_breaker").getString(), true));
        }
        if (config.fastShieldEnabled) {
            features.add(new FeatureInfo("fast_shield", 
                Component.translatable("catalyst.feature.fast_shield").getString(), true));
        }
        if (config.autoDoorEnabled) {
            features.add(new FeatureInfo("auto_door", 
                Component.translatable("catalyst.feature.auto_door").getString(), true));
        }
        if (config.autoWaterBucketEnabled) {
            features.add(new FeatureInfo("auto_water_bucket", 
                Component.translatable("catalyst.feature.auto_water_bucket").getString(), true));
        }
        if (config.entityXrayEnabled) {
            features.add(new FeatureInfo("entity_xray", 
                Component.translatable("catalyst.feature.entity_xray").getString(), true));
        }
        if (config.miniHudEnabled) {
            features.add(new FeatureInfo("mini_hud", 
                Component.translatable("catalyst.feature.mini_hud").getString(), true));
        }
        if (config.inventorySorterEnabled) {
            features.add(new FeatureInfo("inventory_sorter", 
                Component.translatable("catalyst.feature.inventory_sorter").getString(), true));
        }
        
        return features;
    }
    
    private static double easeOutBack(double x) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
    }
    
    private static double easeInBack(double x) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }
}
