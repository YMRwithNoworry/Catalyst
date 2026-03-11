package org.alku.catalyst.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    
    @Shadow @Final private NativeImage lightPixels;
    @Shadow @Final private DynamicTexture lightTexture;
    @Shadow private boolean updateLightTexture;
    
    @Inject(
        method = "updateLightTexture",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUpdateLightTexture(CallbackInfo ci) {
        CatalystConfig config = CatalystConfig.getInstance();
        if (config.gammaOverrideEnabled && config.nightVisionMode) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    this.lightPixels.setPixelRGBA(x, y, -1);
                }
            }
            this.lightTexture.upload();
            this.updateLightTexture = false;
            ci.cancel();
        }
    }
}
