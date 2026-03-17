package org.alku.catalyst.client.feature;

import dev.architectury.event.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.alku.catalyst.config.CatalystConfig;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MouseTweaks {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("MouseTweaks");
    
    private static boolean mouseLeftDown = false;
    private static boolean mouseRightDown = false;
    private static int lastMouseX = -1;
    private static int lastMouseY = -1;
    private static Slot lastHoveredSlot = null;
    private static boolean isDragging = false;
    private static int dragStartX = -1;
    private static int dragStartY = -1;
    private static int dragButton = -1;
    
    private static Map<Slot, Integer> rmbSlotPassCounts = new HashMap<>();
    private static ItemStack rmbDragItem = ItemStack.EMPTY;
    
    private static ItemStack lmbDragItem = ItemStack.EMPTY;
    private static boolean lmbShiftDragMode = false;
    
    private static boolean rmbShiftDragMode = false;
    private static Set<Integer> processedSlots = new HashSet<>();
    
    public static void init() {
    }
    
    public static void tick(Minecraft mc) {
        if (mc.screen instanceof AbstractContainerScreen) {
            double mouseX = mc.mouseHandler.xpos();
            double mouseY = mc.mouseHandler.ypos();
            
            double guiScale = mc.getWindow().getGuiScale();
            int scaledMouseX = (int) (mouseX / guiScale);
            int scaledMouseY = (int) (mouseY / guiScale);
            
            onMouseMove(mc, scaledMouseX, scaledMouseY);
        }
    }
    
    private static Slot getHoveredSlot(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen == null) {
            return null;
        }
        
        try {
            Field leftPosField = AbstractContainerScreen.class.getDeclaredField("leftPos");
            Field topPosField = AbstractContainerScreen.class.getDeclaredField("topPos");
            leftPosField.setAccessible(true);
            topPosField.setAccessible(true);
            
            int leftPos = leftPosField.getInt(screen);
            int topPos = topPosField.getInt(screen);
            
            int relX = mouseX - leftPos;
            int relY = mouseY - topPos;
            
            for (int i = 0; i < screen.getMenu().slots.size(); i++) {
                Slot slot = screen.getMenu().getSlot(i);
                if (relX >= slot.x && relX < slot.x + 16 && relY >= slot.y && relY < slot.y + 16) {
                    return slot;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    private static boolean isShiftKeyDown(Minecraft mc) {
        return mc.options.keyShift.isDown() || 
               GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
               GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }
    
    public static EventResult onMouseClicked(Minecraft mc, int button, int action, int mods) {
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (!(mc.screen instanceof AbstractContainerScreen) || !config.rmbTweak) {
            return EventResult.pass();
        }
        
        if (mc.screen instanceof CreativeModeInventoryScreen) {
            return EventResult.pass();
        }
        
        double guiScale = mc.getWindow().getGuiScale();
        int currentMouseX = (int) (mc.mouseHandler.xpos() / guiScale);
        int currentMouseY = (int) (mc.mouseHandler.ypos() / guiScale);
        
        if (action == 1) {
            if (button == 0) {
                mouseLeftDown = true;
                dragButton = 0;
                dragStartX = currentMouseX;
                dragStartY = currentMouseY;
                lastMouseX = currentMouseX;
                lastMouseY = currentMouseY;
                processedSlots.clear();
                
                if (config.lmbTweakWithItem || config.lmbTweakWithoutItem) {
                    return handleLeftClickStart(mc, button, action, mods, currentMouseX, currentMouseY);
                }
            } else if (button == 1) {
                mouseRightDown = true;
                dragButton = 1;
                dragStartX = currentMouseX;
                dragStartY = currentMouseY;
                lastMouseX = currentMouseX;
                lastMouseY = currentMouseY;
                processedSlots.clear();
                
                return handleRightClickStart(mc, button, action, mods, currentMouseX, currentMouseY);
            }
        } else if (action == 0) {
            if (button == 0) {
                mouseLeftDown = false;
                if (isDragging && dragButton == 0) {
                    isDragging = false;
                    return handleLeftClickEnd(mc, button, action, mods);
                }
            } else if (button == 1) {
                mouseRightDown = false;
                if (isDragging && dragButton == 1) {
                    isDragging = false;
                    return handleRightClickEnd(mc, button, action, mods);
                }
            }
        }
        
        return EventResult.pass();
    }
    
    public static EventResult onMouseScrolled(Minecraft mc, double scrollDelta) {
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (!config.rmbTweak || !config.wheelTweak || !(mc.screen instanceof AbstractContainerScreen)) {
            return EventResult.pass();
        }
        
        if (mc.screen instanceof CreativeModeInventoryScreen) {
            return EventResult.pass();
        }
        
        return handleWheelScroll(mc, scrollDelta);
    }
    
    public static void onMouseMove(Minecraft mc, double mouseX, double mouseY) {
        int currentMouseX = (int) mouseX;
        int currentMouseY = (int) mouseY;
        
        if (!(mc.screen instanceof AbstractContainerScreen)) {
            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
            return;
        }
        
        if (mc.screen instanceof CreativeModeInventoryScreen) {
            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
            return;
        }
        
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
        
        if ((mouseLeftDown || mouseRightDown) && !isDragging) {
            int dx = Math.abs(currentMouseX - dragStartX);
            int dy = Math.abs(currentMouseY - dragStartY);
            
            if (dx > 3 || dy > 3) {
                isDragging = true;
            }
        }
        
        if (isDragging) {
            List<Slot> slotsOnPath = getSlotsOnPath(screen, lastMouseX, lastMouseY, currentMouseX, currentMouseY);
            for (Slot slot : slotsOnPath) {
                if (slot != null) {
                    processSlot(mc, slot, screen);
                }
            }
        }
        
        lastHoveredSlot = getHoveredSlot(screen, currentMouseX, currentMouseY);
        lastMouseX = currentMouseX;
        lastMouseY = currentMouseY;
    }
    
    private static List<Slot> getSlotsOnPath(AbstractContainerScreen<?> screen, int x1, int y1, int x2, int y2) {
        List<Slot> slots = new ArrayList<>();
        Set<Integer> seenIndices = new HashSet<>();
        
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy) + 1;
        
        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0 : (float) i / steps;
            int x = (int) (x1 + (x2 - x1) * t);
            int y = (int) (y1 + (y2 - y1) * t);
            
            Slot slot = getHoveredSlot(screen, x, y);
            if (slot != null && !seenIndices.contains(slot.index)) {
                slots.add(slot);
                seenIndices.add(slot.index);
            }
        }
        
        return slots;
    }
    
    private static void processSlot(Minecraft mc, Slot slot, AbstractContainerScreen<?> screen) {
        CatalystConfig config = CatalystConfig.getInstance();
        boolean shiftDown = isShiftKeyDown(mc);
        
        if (dragButton == 0) {
            if (lmbShiftDragMode && config.lmbTweakWithoutItem) {
                if (shiftDown && !slot.getItem().isEmpty()) {
                    performShiftClick(mc, slot);
                }
            } else if (!lmbDragItem.isEmpty() && config.lmbTweakWithItem) {
                ItemStack slotItem = slot.getItem();
                if (!slotItem.isEmpty() && ItemStack.isSameItemSameTags(lmbDragItem, slotItem)) {
                    if (shiftDown) {
                        performShiftClick(mc, slot);
                    } else {
                        performMerge(mc, slot);
                    }
                }
            }
        } else if (dragButton == 1) {
            if (rmbShiftDragMode) {
                if (shiftDown && !slot.getItem().isEmpty()) {
                    performThrow(mc, slot);
                }
            } else if (!rmbDragItem.isEmpty()) {
                if (slot.mayPlace(rmbDragItem)) {
                    rmbSlotPassCounts.put(slot, rmbSlotPassCounts.getOrDefault(slot, 0) + 1);
                }
            }
        }
    }
    
    private static EventResult handleLeftClickStart(Minecraft mc, int button, int action, int mods, int mouseX, int mouseY) {
        if (!(mc.screen instanceof AbstractContainerScreen)) {
            return EventResult.pass();
        }
        
        CatalystConfig config = CatalystConfig.getInstance();
        boolean shiftDown = isShiftKeyDown(mc);
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
        Slot hoveredSlot = getHoveredSlot(screen, mouseX, mouseY);
        
        lmbShiftDragMode = false;
        lmbDragItem = ItemStack.EMPTY;
        
        if (shiftDown && config.lmbTweakWithoutItem) {
            lmbShiftDragMode = true;
            if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty()) {
                performShiftClick(mc, hoveredSlot);
            }
            return EventResult.interruptDefault();
        }
        
        if (hoveredSlot != null && mc.gameMode != null) {
            AbstractContainerMenu menu = mc.player.containerMenu;
            int containerId = menu.containerId;
            
            ItemStack carriedBefore = menu.getCarried();
            
            if (carriedBefore.isEmpty()) {
                if (config.lmbTweakWithItem) {
                    mc.gameMode.handleInventoryMouseClick(containerId, hoveredSlot.index, 0, ClickType.PICKUP, mc.player);
                    lmbDragItem = menu.getCarried();
                } else {
                    return EventResult.pass();
                }
            } else {
                lmbDragItem = carriedBefore;
            }
        } else {
            lmbDragItem = mc.player.containerMenu.getCarried();
        }
        
        return EventResult.interruptDefault();
    }
    
    private static EventResult handleRightClickStart(Minecraft mc, int button, int action, int mods, int mouseX, int mouseY) {
        if (!(mc.screen instanceof AbstractContainerScreen)) {
            return EventResult.pass();
        }
        
        CatalystConfig config = CatalystConfig.getInstance();
        boolean shiftDown = isShiftKeyDown(mc);
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
        Slot hoveredSlot = getHoveredSlot(screen, mouseX, mouseY);
        
        rmbShiftDragMode = false;
        rmbDragItem = ItemStack.EMPTY;
        
        if (shiftDown && config.rmbTweak) {
            rmbShiftDragMode = true;
            if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty()) {
                performThrow(mc, hoveredSlot);
            }
            return EventResult.interruptDefault();
        }
        
        if (hoveredSlot != null && mc.gameMode != null) {
            AbstractContainerMenu menu = mc.player.containerMenu;
            int containerId = menu.containerId;
            
            ItemStack carriedBefore = menu.getCarried();
            
            mc.gameMode.handleInventoryMouseClick(containerId, hoveredSlot.index, 1, ClickType.PICKUP, mc.player);
            
            rmbDragItem = menu.getCarried();
            
            if (!ItemStack.matches(carriedBefore, rmbDragItem)) {
                rmbSlotPassCounts.put(hoveredSlot, 1);
            }
        } else {
            rmbDragItem = mc.player.containerMenu.getCarried();
        }
        
        rmbSlotPassCounts.clear();
        return EventResult.interruptDefault();
    }
    
    private static EventResult handleLeftClickEnd(Minecraft mc, int button, int action, int mods) {
        lmbDragItem = ItemStack.EMPTY;
        lmbShiftDragMode = false;
        processedSlots.clear();
        return EventResult.interruptDefault();
    }
    
    private static EventResult handleRightClickEnd(Minecraft mc, int button, int action, int mods) {
        rmbShiftDragMode = false;
        processedSlots.clear();
        
        if (rmbSlotPassCounts.isEmpty()) {
            return EventResult.pass();
        }
        
        AbstractContainerMenu menu = mc.player.containerMenu;
        int containerId = menu.containerId;
        
        for (Map.Entry<Slot, Integer> entry : rmbSlotPassCounts.entrySet()) {
            Slot slot = entry.getKey();
            int passCount = entry.getValue();
            
            if (passCount <= 0) {
                continue;
            }
            
            for (int i = 0; i < passCount; i++) {
                if (mc.gameMode != null) {
                    ItemStack carriedBefore = menu.getCarried();
                    if (carriedBefore.isEmpty()) {
                        break;
                    }
                    
                    mc.gameMode.handleInventoryMouseClick(containerId, slot.index, 1, ClickType.PICKUP, mc.player);
                    
                    ItemStack carriedAfter = menu.getCarried();
                    if (ItemStack.matches(carriedBefore, carriedAfter)) {
                        break;
                    }
                }
            }
        }
        
        rmbSlotPassCounts.clear();
        rmbDragItem = ItemStack.EMPTY;
        return EventResult.interruptDefault();
    }
    
    private static EventResult handleWheelScroll(Minecraft mc, double scrollDelta) {
        if (!(mc.screen instanceof AbstractContainerScreen)) {
            return EventResult.pass();
        }
        
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
        Slot hoveredSlot = getHoveredSlot(screen, lastMouseX, lastMouseY);
        
        if (hoveredSlot == null || mc.gameMode == null) {
            return EventResult.pass();
        }
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        boolean isPush = scrollDelta < 0;
        
        if (config.wheelScrollDirection == 1) {
            isPush = !isPush;
        } else if (config.wheelScrollDirection == 2) {
            if (hoveredSlot.container == mc.player.getInventory()) {
                isPush = scrollDelta < 0;
            } else {
                isPush = scrollDelta > 0;
            }
        }
        
        if (isPush) {
            performWheelPush(mc, hoveredSlot);
        } else {
            performWheelPull(mc, hoveredSlot);
        }
        
        return EventResult.interruptDefault();
    }
    
    private static void performShiftClick(Minecraft mc, Slot slot) {
        if (mc.gameMode == null || mc.player == null) {
            return;
        }
        
        AbstractContainerMenu menu = mc.player.containerMenu;
        int containerId = menu.containerId;
        
        mc.gameMode.handleInventoryMouseClick(containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
    }
    
    private static void performThrow(Minecraft mc, Slot slot) {
        if (mc.gameMode == null || mc.player == null) {
            return;
        }
        
        AbstractContainerMenu menu = mc.player.containerMenu;
        int containerId = menu.containerId;
        
        mc.gameMode.handleInventoryMouseClick(containerId, slot.index, 1, ClickType.THROW, mc.player);
    }
    
    private static void performRmbPlace(Minecraft mc, Slot slot) {
        if (mc.gameMode == null || mc.player == null) {
            return;
        }
        
        AbstractContainerMenu menu = mc.player.containerMenu;
        int containerId = menu.containerId;
        
        mc.gameMode.handleInventoryMouseClick(containerId, slot.index, 1, ClickType.PICKUP, mc.player);
        
        rmbDragItem = menu.getCarried();
    }
    
    private static void performMerge(Minecraft mc, Slot slot) {
        if (mc.gameMode == null) {
            return;
        }
        
        AbstractContainerMenu menu = mc.player.containerMenu;
        int containerId = menu.containerId;
        
        ItemStack slotItem = slot.getItem();
        ItemStack carried = menu.getCarried();
        
        if (carried.isEmpty() || !ItemStack.isSameItemSameTags(carried, slotItem)) {
            return;
        }
        
        int maxStackSize = carried.getMaxStackSize();
        int totalAmount = carried.getCount() + slotItem.getCount();
        
        if (totalAmount <= maxStackSize) {
            mc.gameMode.handleInventoryMouseClick(containerId, slot.index, 0, ClickType.PICKUP, mc.player);
        } else {
            int remaining = totalAmount - maxStackSize;
            slotItem.setCount(remaining);
            carried.setCount(maxStackSize);
            slot.setChanged();
        }
        
        lmbDragItem = menu.getCarried();
    }
    
    private static void performWheelPush(Minecraft mc, Slot sourceSlot) {
        if (sourceSlot.container != mc.player.getInventory()) {
            return;
        }
        
        ItemStack sourceItem = sourceSlot.getItem();
        if (sourceItem.isEmpty()) {
            return;
        }
        
        Slot targetSlot = findWheelTargetSlot(mc, sourceItem, false);
        
        if (targetSlot != null) {
            transferItem(mc, sourceSlot, targetSlot);
        }
    }
    
    private static void performWheelPull(Minecraft mc, Slot sourceSlot) {
        if (sourceSlot.container == mc.player.getInventory()) {
            return;
        }
        
        ItemStack sourceItem = sourceSlot.getItem();
        if (sourceItem.isEmpty()) {
            return;
        }
        
        Slot targetSlot = findWheelTargetSlot(mc, sourceItem, true);
        
        if (targetSlot != null) {
            transferItem(mc, sourceSlot, targetSlot);
        }
    }
    
    private static Slot findWheelTargetSlot(Minecraft mc, ItemStack item, boolean searchPlayerInventory) {
        AbstractContainerMenu menu = mc.player.containerMenu;
        CatalystConfig config = CatalystConfig.getInstance();
        
        boolean reverse = config.wheelSearchOrder == 1;
        
        int start = reverse ? menu.slots.size() - 1 : 0;
        int end = reverse ? -1 : menu.slots.size();
        int step = reverse ? -1 : 1;
        
        for (int i = start; i != end; i += step) {
            Slot slot = menu.getSlot(i);
            boolean isPlayerInventory = slot.container == mc.player.getInventory();
            
            if (searchPlayerInventory != isPlayerInventory) {
                continue;
            }
            
            if (slot.mayPlace(item)) {
                ItemStack slotItem = slot.getItem();
                if (slotItem.isEmpty()) {
                    return slot;
                } else if (ItemStack.isSameItemSameTags(item, slotItem) && slotItem.getCount() < slotItem.getMaxStackSize()) {
                    return slot;
                }
            }
        }
        
        return null;
    }
    
    private static void transferItem(Minecraft mc, Slot sourceSlot, Slot targetSlot) {
        if (mc.gameMode == null) {
            return;
        }
        
        AbstractContainerMenu menu = mc.player.containerMenu;
        int containerId = menu.containerId;
        
        ItemStack sourceItem = sourceSlot.getItem();
        ItemStack targetItem = targetSlot.getItem();
        
        if (targetItem.isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(containerId, sourceSlot.index, 0, ClickType.PICKUP, mc.player);
            mc.gameMode.handleInventoryMouseClick(containerId, targetSlot.index, 0, ClickType.PICKUP, mc.player);
        } else if (ItemStack.isSameItemSameTags(sourceItem, targetItem)) {
            int maxStackSize = sourceItem.getMaxStackSize();
            int total = sourceItem.getCount() + targetItem.getCount();
            
            if (total <= maxStackSize) {
                mc.gameMode.handleInventoryMouseClick(containerId, sourceSlot.index, 0, ClickType.PICKUP, mc.player);
                mc.gameMode.handleInventoryMouseClick(containerId, targetSlot.index, 0, ClickType.PICKUP, mc.player);
            } else {
                int remaining = total - maxStackSize;
                sourceItem.setCount(remaining);
                targetItem.setCount(maxStackSize);
                sourceSlot.setChanged();
                targetSlot.setChanged();
            }
        }
    }
}
