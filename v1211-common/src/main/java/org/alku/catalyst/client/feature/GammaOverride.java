package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import org.alku.catalyst.config.CatalystConfig;

public class GammaOverride {
    private static double originalGamma = -1;
    private static boolean wasEnabled = false;
    private static final double MAX_GAMMA = 10000.0;
    
    public static void tick(Minecraft mc) {
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (config.gammaOverrideEnabled) {
            if (!wasEnabled) {
                originalGamma = mc.options.gamma().get();
                wasEnabled = true;
            }
            
            double targetGamma = config.gammaValue;
            if (config.nightVisionMode) {
                targetGamma = Math.max(config.gammaValue, MAX_GAMMA);
            }
            targetGamma = Math.min(targetGamma, MAX_GAMMA);
            mc.options.gamma().set(targetGamma);
        } else {
            if (wasEnabled && originalGamma >= 0) {
                mc.options.gamma().set(originalGamma);
                wasEnabled = false;
            }
        }
    }
    
    public static void onDisable(Minecraft mc) {
        if (wasEnabled && originalGamma >= 0) {
            mc.options.gamma().set(originalGamma);
            wasEnabled = false;
        }
    }
    
    public static void setGamma(Minecraft mc, double value) {
        double targetGamma = Math.min(Math.max(0, value), MAX_GAMMA);
        mc.options.gamma().set(targetGamma);
    }
}
