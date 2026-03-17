package org.alku.catalyst.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    
    @Inject(
        method = {"updateLightTexture", "m_109267_"},
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void onUpdateLightTexture(CallbackInfo ci) {
        if (CatalystConfig.getInstance().gammaOverrideEnabled) {
            ci.cancel();
        }
    }
}
