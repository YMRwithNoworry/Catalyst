package org.alku.catalyst.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.alku.catalyst.client.feature.InventorySorter;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin extends Screen {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow @Final protected AbstractContainerMenu menu;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    
    @Unique
    private Button catalyst$sortButton;
    
    protected ContainerScreenMixin(Component title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    protected void onInit(CallbackInfo ci) {
        if (!CatalystConfig.getInstance().inventorySorterEnabled) {
            return;
        }
        
        catalyst$sortButton = Button.builder(
            Component.translatable("catalyst.gui.sort_button"),
            button -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && !InventorySorter.isSorting()) {
                    InventorySorter.sortCurrentContainer(mc);
                }
            }
        ).bounds(leftPos + imageWidth - 60, topPos + 4, 56, 18).build();
        
        this.addRenderableWidget(catalyst$sortButton);
        
        if (CatalystConfig.getInstance().autoSortOnOpen && !InventorySorter.isSorting()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                InventorySorter.sortCurrentContainer(mc);
            }
        }
    }
    
    @Inject(method = "render", at = @At("HEAD"))
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (catalyst$sortButton != null) {
            catalyst$sortButton.setX(leftPos + imageWidth - 60);
            catalyst$sortButton.setY(topPos + 4);
        }
    }
}
