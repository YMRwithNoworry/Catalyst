package org.alku.catalyst.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.alku.catalyst.config.CatalystConfig;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class ChamsMixin {
    
    private static boolean catalyst$depthTestWasEnabled = false;
    
    @Inject(
        method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void beforeEntityRender(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (CatalystConfig.getInstance().chamsEnabled && shouldApplyChams(entity)) {
            catalyst$depthTestWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            if (catalyst$depthTestWasEnabled) {
                RenderSystem.disableDepthTest();
            }
        }
    }
    
    @Inject(
        method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("RETURN")
    )
    private void afterEntityRender(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (CatalystConfig.getInstance().chamsEnabled && shouldApplyChams(entity)) {
            if (catalyst$depthTestWasEnabled) {
                RenderSystem.enableDepthTest();
            }
        }
    }
    
    private static boolean shouldApplyChams(Entity entity) {
        if (entity instanceof Player) {
            return CatalystConfig.getInstance().chamsPlayers;
        }
        if (entity instanceof Animal) {
            return CatalystConfig.getInstance().chamsAnimals;
        }
        if (entity instanceof Monster) {
            return CatalystConfig.getInstance().chamsMonsters;
        }
        return false;
    }
}
