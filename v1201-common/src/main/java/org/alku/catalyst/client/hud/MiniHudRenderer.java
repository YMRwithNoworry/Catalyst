package org.alku.catalyst.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.entity.Entity;
import org.alku.catalyst.config.CatalystConfig;

public class MiniHudRenderer {

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (!CatalystConfig.getInstance().miniHudEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        int x = 4;
        int y = 4;
        int lineHeight = 10;
        int color = 0xFFFFFF;

        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (CatalystConfig.getInstance().miniHudShowCoords) {
            BlockPos pos = player.blockPosition();
            String coords = Component.translatable("catalyst.hud.coords", pos.getX(), pos.getY(), pos.getZ()).getString();
            guiGraphics.drawString(mc.font, coords, x, y, color);
            y += lineHeight;
        }

        if (CatalystConfig.getInstance().miniHudShowBiome) {
            BlockPos pos = player.blockPosition();
            Biome biome = level.getBiome(pos).value();
            String biomeName = biome.toString();
            if (biomeName.contains(":")) {
                biomeName = biomeName.substring(biomeName.indexOf(":") + 1);
            }
            guiGraphics.drawString(mc.font, Component.translatable("catalyst.hud.biome", biomeName).getString(), x, y, color);
            y += lineHeight;
        }

        if (CatalystConfig.getInstance().miniHudShowTime) {
            long dayTime = level.getDayTime() % 24000;
            int hours = (int) (dayTime / 1000 + 6) % 24;
            int minutes = (int) ((dayTime % 1000) * 60 / 1000);
            guiGraphics.drawString(mc.font, Component.translatable("catalyst.hud.time", String.format("%02d:%02d", hours, minutes)).getString(), x, y, color);
            y += lineHeight;
        }

        if (CatalystConfig.getInstance().miniHudShowDayCount) {
            long dayCount = level.getDayTime() / 24000L + 1;
            guiGraphics.drawString(mc.font, Component.translatable("catalyst.hud.day", dayCount).getString(), x, y, color);
            y += lineHeight;
        }

        if (CatalystConfig.getInstance().miniHudShowEntityCount) {
            int entityCount = 0;
            try {
                for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(64), e -> true)) {
                    if (!entity.isSpectator()) {
                        entityCount++;
                    }
                }
            } catch (Exception ignored) {
            }
            guiGraphics.drawString(mc.font, Component.translatable("catalyst.hud.entities", entityCount).getString(), x, y, color);
        }
    }
}
