package org.alku.catalyst.mixin;

import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.alku.catalyst.client.waypoint.WaypointRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WaypointRenderMixin {
    
    @Inject(
        method = {"renderLevel", "m_109599_"},
        at = @At("RETURN"),
        remap = false
    )
    private void afterRenderLevel(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, Matrix4f projectionMatrix2, CallbackInfo ci) {
        WaypointRenderer.renderWaypoints(deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}
