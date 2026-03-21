package org.alku.catalyst.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LightTexture;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    
    @Inject(
        method = "updateLightTexture(Lnet/minecraft/client/DeltaTracker;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUpdateLightTexture(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CatalystConfig.getInstance().gammaOverrideEnabled) {
            ci.cancel();
        }
    }
}
