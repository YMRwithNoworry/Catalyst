package org.alku.catalyst.client.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class WaypointRenderer {
    
    public static void renderWaypoints(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        
        ResourceKey<Level> currentDimension = mc.level.dimension();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(3.0f);
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        
        for (Waypoint wp : WaypointManager.getInstance().getWaypointsForDimension(currentDimension)) {
            renderWaypointBeam(poseStack, wp, cameraPos, consumer);
        }
        
        bufferSource.endBatch(RenderType.lines());
        
        for (Waypoint wp : WaypointManager.getInstance().getWaypointsForDimension(currentDimension)) {
            renderWaypointLabel(poseStack, wp, cameraPos, mc, bufferSource);
        }
        
        bufferSource.endBatch();
        
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }
    
    private static void renderWaypointBeam(PoseStack poseStack, Waypoint waypoint, Vec3 cameraPos, VertexConsumer consumer) {
        BlockPos pos = waypoint.getPos();
        double wpX = pos.getX() + 0.5;
        double wpZ = pos.getZ() + 0.5;
        
        int color = waypoint.getColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int)(0.8f * 255);
        
        poseStack.pushPose();
        poseStack.translate(wpX - cameraPos.x, -cameraPos.y, wpZ - cameraPos.z);
        
        Matrix4f matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();
        
        float minY = -64;
        float maxY = 320;
        
        consumer.vertex(matrix, 0.0f, minY, 0.0f).color(r, g, b, a).normal(normal, 0, 1, 0);
        consumer.vertex(matrix, 0.0f, maxY, 0.0f).color(r, g, b, a).normal(normal, 0, 1, 0);
        
        poseStack.popPose();
    }
    
    private static void renderWaypointLabel(PoseStack poseStack, Waypoint waypoint, Vec3 cameraPos, Minecraft mc, MultiBufferSource bufferSource) {
        Font font = mc.font;
        
        BlockPos pos = waypoint.getPos();
        double wpX = pos.getX() + 0.5;
        double wpY = pos.getY() + 2.0;
        double wpZ = pos.getZ() + 0.5;
        
        Component name = Component.literal(waypoint.getName());
        
        int textColor = 0xFFFFFF;
        
        poseStack.pushPose();
        poseStack.translate(wpX - cameraPos.x, wpY - cameraPos.y, wpZ - cameraPos.z);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        
        Matrix4f matrix4f = poseStack.last().pose();
        int width = font.width(name);
        float backgroundOpacity = mc.options.getBackgroundOpacity(0.25F);
        int bgColor = (int)(backgroundOpacity * 255.0F) << 24;
        
        font.drawInBatch(name, -width / 2.0f, 0, textColor, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, bgColor, 15728880);
        
        poseStack.popPose();
    }
}
