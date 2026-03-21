package org.alku.catalyst.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.alku.catalyst.config.CatalystConfig;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class EntityXrayMixin {

    @Inject(
        method = "renderEntity",
        at = @At("HEAD")
    )
    private void beforeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (CatalystConfig.getInstance().entityXrayEnabled && !entity.isSpectator()) {
            GL11.glDepthFunc(GL11.GL_ALWAYS);
        }
    }

    @Inject(
        method = "renderEntity",
        at = @At("RETURN")
    )
    private void afterRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (CatalystConfig.getInstance().entityXrayEnabled && !entity.isSpectator()) {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
    }
}
