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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class ChamsMixin {
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
        )
    )
    private void beforeEntityRender(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, CallbackInfo ci) {
        if (CatalystConfig.getInstance().chamsEnabled && shouldApplyChams(entity)) {
            RenderSystem.disableDepthTest();
        }
    }
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            shift = At.Shift.AFTER
        )
    )
    private void afterEntityRender(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, CallbackInfo ci) {
        if (CatalystConfig.getInstance().chamsEnabled && shouldApplyChams(entity)) {
            RenderSystem.enableDepthTest();
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
