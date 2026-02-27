package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import org.alku.catalyst.config.CatalystConfig;

public class GammaOverride {
    private static double originalGamma = -1;
    private static boolean wasEnabled = false;
    private static final double MAX_GAMMA = 1.0;
    
    public static void tick(Minecraft mc) {
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (config.gammaOverrideEnabled) {
            if (!wasEnabled) {
                originalGamma = mc.options.gamma().get();
                wasEnabled = true;
            }
            
            double targetGamma = config.nightVisionMode ? MAX_GAMMA : Math.min(config.gammaValue, MAX_GAMMA);
            if (mc.options.gamma().get() != targetGamma) {
                mc.options.gamma().set(targetGamma);
            }
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
}
