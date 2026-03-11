package org.alku.catalyst.client.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
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
            renderWaypointBeam(poseStack, wp, cameraPos, partialTick, bufferSource);
        }
    }
    
    private static void renderWaypointBeam(PoseStack poseStack, Waypoint waypoint, Vec3 cameraPos, float partialTick, MultiBufferSource bufferSource) {
        BlockPos pos = waypoint.getPos();
        double x = pos.getX() - cameraPos.x;
        double z = pos.getZ() - cameraPos.z;
        
        float r = ((waypoint.getColor() >> 16) & 0xFF) / 255.0f;
        float g = ((waypoint.getColor() >> 8) & 0xFF) / 255.0f;
        float b = (waypoint.getColor() & 0xFF) / 255.0f;
        
        poseStack.pushPose();
        poseStack.translate(x, 0, z);
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        double beamWidth = 0.2;
        int height = 256;
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        Matrix4f matrix = poseStack.last().pose();
        
        float alpha = 0.5f;
        
        buffer.vertex(matrix, (float)-beamWidth, height, (float)-beamWidth).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, (float)beamWidth, height, (float)-beamWidth).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, (float)beamWidth, height, (float)beamWidth).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, (float)-beamWidth, height, (float)beamWidth).color(r, g, b, alpha).endVertex();
        
        buffer.vertex(matrix, (float)-beamWidth, 0, (float)-beamWidth).color(r, g, b, 0.1f).endVertex();
        buffer.vertex(matrix, (float)-beamWidth, 0, (float)beamWidth).color(r, g, b, 0.1f).endVertex();
        buffer.vertex(matrix, (float)beamWidth, 0, (float)beamWidth).color(r, g, b, 0.1f).endVertex();
        buffer.vertex(matrix, (float)beamWidth, 0, (float)-beamWidth).color(r, g, b, 0.1f).endVertex();
        
        tesselator.end();
        
        poseStack.popPose();
        
        renderWaypointLabel(poseStack, waypoint, x, z, bufferSource);
    }
    
    private static void renderWaypointLabel(PoseStack poseStack, Waypoint waypoint, double x, double z, MultiBufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        
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
        int padding = 4;
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        Matrix4f matrix = poseStack.last().pose();
        
        float bgAlpha = 0.5f;
        buffer.vertex(matrix, -textWidth / 2 - padding, -textHeight / 2 - padding, 0).color(0, 0, 0, bgAlpha).endVertex();
        buffer.vertex(matrix, textWidth / 2 + padding, -textHeight / 2 - padding, 0).color(0, 0, 0, bgAlpha).endVertex();
        buffer.vertex(matrix, textWidth / 2 + padding, textHeight / 2 + padding, 0).color(0, 0, 0, bgAlpha).endVertex();
        buffer.vertex(matrix, -textWidth / 2 - padding, textHeight / 2 + padding, 0).color(0, 0, 0, bgAlpha).endVertex();
        
        tesselator.end();
        
        font.drawInBatch(initial, -textWidth / 2.0f, -textHeight / 2.0f, textColor, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        
        poseStack.popPose();
    }
}
