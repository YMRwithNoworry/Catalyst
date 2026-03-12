package org.alku.catalyst.client.waypoint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
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
        
        for (Waypoint wp : WaypointManager.getInstance().getWaypointsForDimension(currentDimension)) {
            renderWaypointLabel(poseStack, wp, cameraPos, bufferSource);
        }
    }
    
    private static void renderWaypointLabel(PoseStack poseStack, Waypoint waypoint, Vec3 cameraPos, MultiBufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        
        BlockPos pos = waypoint.getPos();
        double x = pos.getX() - cameraPos.x;
        double z = pos.getZ() - cameraPos.z;
        
        String initial = WaypointManager.getInstance().getDisplayInitial(waypoint);
        
        poseStack.pushPose();
        poseStack.translate(x, 260, z);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        
        float r = ((waypoint.getColor() >> 16) & 0xFF) / 255.0f;
        float g = ((waypoint.getColor() >> 8) & 0xFF) / 255.0f;
        float b = (waypoint.getColor() & 0xFF) / 255.0f;
        
        int textColor = ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255) | 0xFF000000;
        
        int textWidth = font.width(initial);
        int textHeight = font.lineHeight;
        
        Matrix4f matrix = poseStack.last().pose();
        
        font.drawInBatch(initial, -textWidth / 2.0f, -textHeight / 2.0f, textColor, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        
        poseStack.popPose();
    }
}
