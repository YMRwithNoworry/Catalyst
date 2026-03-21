package org.alku.catalyst.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.alku.catalyst.client.hud.MiniHudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MiniHudMixin {

    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void onRender(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MiniHudRenderer.render(guiGraphics, partialTick);
    }
}
