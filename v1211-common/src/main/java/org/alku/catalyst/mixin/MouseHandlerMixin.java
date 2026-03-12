package org.alku.catalyst.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.alku.catalyst.client.gui.CatalystConfigScreen;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.MouseHandler")
public class MouseHandlerMixin {
    
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen instanceof CatalystConfigScreen) {
            CatalystConfig config = CatalystConfig.getInstance();
            float oldScale = config.guiScale;
            config.guiScale += (float) yOffset * 0.1f;
            config.guiScale = Math.max(0.5f, Math.min(2.0f, config.guiScale));
            
            if (oldScale != config.guiScale) {
                config.save();
                mc.setScreen(new CatalystConfigScreen(null));
            }
            ci.cancel();
        }
    }
}
