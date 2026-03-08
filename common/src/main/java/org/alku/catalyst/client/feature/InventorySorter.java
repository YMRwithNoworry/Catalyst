package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventorySorter {
    private static boolean isSorting = false;
    private static final List<Runnable> pendingActions = new ArrayList<>();
    private static int actionIndex = 0;
    private static long lastActionTime = 0;
    private static final int ACTION_DELAY_MS = 50;
    private static int currentContainerId = -1;
    
    public static void sortCurrentContainer(Minecraft mc) {
        if (!CatalystConfig.getInstance().inventorySorterEnabled || isSorting) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null) {
            return;
        }
        
        AbstractContainerMenu container = player.containerMenu;
        if (container == null) {
            return;
        }
        
        isSorting = true;
        pendingActions.clear();
        actionIndex = 0;
        currentContainerId = container.containerId;
        
        if (container instanceof InventoryMenu) {
            sortPlayerInventory(mc, player, container);
        } else {
            sortOpenContainer(mc, player, container);
        }
        
        if (pendingActions.isEmpty()) {
            isSorting = false;
        }
    }
    
    public static void tick(Minecraft mc) {
        if (!isSorting || pendingActions.isEmpty()) {
            return;
        }
        
        LocalPlayer player = mc.player;
        if (player == null || player.containerMenu == null || 
            player.containerMenu.containerId != currentContainerId) {
            reset();
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < ACTION_DELAY_MS) {
            return;
        }
        
        if (actionIndex < pendingActions.size()) {
            pendingActions.get(actionIndex).run();
            actionIndex++;
            lastActionTime = currentTime;
        } else {
            reset();
        }
    }
    
    private static void clickSlot(Minecraft mc, int slotIndex, int button, ClickType clickType) {
        if (mc.player != null && mc.gameMode != null) {
            mc.gameMode.handleInventoryMouseClick(currentContainerId, slotIndex, button, clickType, mc.player);
        }
    }
    
    public static void sortPlayerInventory(Minecraft mc, LocalPlayer player, AbstractContainerMenu container) {
        List<Integer> inventorySlots = new ArrayList<>();
        List<Integer> hotbarSlots = new ArrayList<>();
        
        for (Slot slot : container.slots) {
            if (slot.container == player.getInventory()) {
                int index = slot.index;
                if (index >= 0 && index < 9) {
                    hotbarSlots.add(slot.index);
                } else if (index >= 9 && index < 36) {
                    inventorySlots.add(slot.index);
                }
            }
        }
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        if (config.sortHotbar) {
            planSortSlots(mc, container, hotbarSlots, config.sortMode);
        }
        
        planSortSlots(mc, container, inventorySlots, config.sortMode);
    }
    
    public static void sortOpenContainer(Minecraft mc, LocalPlayer player, AbstractContainerMenu container) {
        List<Integer> containerSlots = new ArrayList<>();
        List<Integer> playerInvSlots = new ArrayList<>();
        List<Integer> playerHotbarSlots = new ArrayList<>();
        
        Container playerInventory = player.getInventory();
        
        for (Slot slot : container.slots) {
            if (slot.container == playerInventory) {
                int index = slot.index;
                if (index >= 0 && index < 9) {
                    playerHotbarSlots.add(slot.index);
                } else if (index >= 9 && index < 36) {
                    playerInvSlots.add(slot.index);
                }
            } else if (slot.container != null && slot.container != playerInventory) {
                containerSlots.add(slot.index);
            }
        }
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        planSortSlots(mc, container, containerSlots, config.sortMode);
        
        if (config.sortPlayerInventoryInContainer) {
            planSortSlots(mc, container, playerInvSlots, config.sortMode);
            if (config.sortHotbar) {
                planSortSlots(mc, container, playerHotbarSlots, config.sortMode);
            }
        }
    }
    
    private static void planSortSlots(Minecraft mc, AbstractContainerMenu container, List<Integer> slotIndices, int sortMode) {
        if (slotIndices.isEmpty()) {
            return;
        }
        
        List<ItemStack> items = new ArrayList<>();
        for (int slotIndex : slotIndices) {
            Slot slot = container.getSlot(slotIndex);
            if (slot != null && !slot.getItem().isEmpty()) {
                items.add(slot.getItem().copy());
            }
        }
        
        if (items.isEmpty()) {
            return;
        }
        
        List<ItemStack> merged = mergeStacks(items);
        Comparator<ItemStack> comparator = getComparator(sortMode);
        merged.sort(comparator);
        
        Map<Integer, ItemStack> targetLayout = new HashMap<>();
        for (int i = 0; i < slotIndices.size(); i++) {
            if (i < merged.size()) {
                targetLayout.put(slotIndices.get(i), merged.get(i));
            } else {
                targetLayout.put(slotIndices.get(i), ItemStack.EMPTY);
            }
        }
        
        planMoveActions(mc, container, slotIndices, targetLayout);
    }
    
    private static void planMoveActions(Minecraft mc, AbstractContainerMenu container, List<Integer> slotIndices, Map<Integer, ItemStack> targetLayout) {
        Map<Integer, ItemStack> currentLayout = new HashMap<>();
        for (int slotIndex : slotIndices) {
            Slot slot = container.getSlot(slotIndex);
            if (slot != null) {
                currentLayout.put(slotIndex, slot.getItem().copy());
            }
        }
        
        List<int[]> moves = new ArrayList<>();
        
        for (int targetSlot : slotIndices) {
            ItemStack targetStack = targetLayout.get(targetSlot);
            ItemStack currentStack = currentLayout.get(targetSlot);
            
            if (ItemStack.isSameItemSameTags(targetStack, currentStack) && 
                targetStack.getCount() == currentStack.getCount()) {
                continue;
            }
            
            if (!targetStack.isEmpty() && !ItemStack.isSameItemSameTags(targetStack, currentStack)) {
                for (int sourceSlot : slotIndices) {
                    if (sourceSlot == targetSlot) continue;
                    
                    ItemStack sourceStack = currentLayout.get(sourceSlot);
                    if (!sourceStack.isEmpty() && ItemStack.isSameItemSameTags(sourceStack, targetStack)) {
                        moves.add(new int[]{sourceSlot, targetSlot});
                        
                        int transferCount = Math.min(sourceStack.getCount(), 
                            targetStack.getMaxStackSize() - currentStack.getCount());
                        
                        if (currentStack.isEmpty()) {
                            currentLayout.put(targetSlot, sourceStack.copy());
                        } else {
                            currentLayout.get(targetSlot).grow(transferCount);
                        }
                        currentLayout.get(sourceSlot).shrink(transferCount);
                        
                        if (currentLayout.get(sourceSlot).isEmpty()) {
                            currentLayout.put(sourceSlot, ItemStack.EMPTY);
                        }
                        break;
                    }
                }
            }
        }
        
        for (int[] move : moves) {
            int fromSlot = move[0];
            int toSlot = move[1];
            
            pendingActions.add(() -> {
                clickSlot(mc, fromSlot, 0, ClickType.PICKUP);
            });
            pendingActions.add(() -> {
                clickSlot(mc, toSlot, 0, ClickType.PICKUP);
            });
            pendingActions.add(() -> {
                if (mc.player != null) {
                    AbstractContainerMenu c = mc.player.containerMenu;
                    if (c != null && !c.getCarried().isEmpty()) {
                        clickSlot(mc, fromSlot, 0, ClickType.PICKUP);
                    }
                }
            });
        }
    }
    
    private static List<ItemStack> mergeStacks(List<ItemStack> items) {
        List<ItemStack> merged = new ArrayList<>();
        
        for (ItemStack stack : items) {
            boolean merged_flag = false;
            
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameTags(stack, existing) && existing.getCount() < existing.getMaxStackSize()) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    int toAdd = Math.min(space, stack.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    
                    if (stack.isEmpty()) {
                        merged_flag = true;
                        break;
                    }
                }
            }
            
            if (!merged_flag && !stack.isEmpty()) {
                merged.add(stack);
            }
        }
        
        return merged;
    }
    
    private static Comparator<ItemStack> getComparator(int sortMode) {
        return switch (sortMode) {
            case 1 -> Comparator.comparing(stack -> stack.getDisplayName().getString());
            case 2 -> Comparator.comparingInt(stack -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(stack.getItem()));
            default -> (stack1, stack2) -> {
                String category1 = getItemCategory(stack1);
                String category2 = getItemCategory(stack2);
                
                if (!category1.equals(category2)) {
                    return category1.compareTo(category2);
                }
                
                return stack1.getDisplayName().getString().compareTo(stack2.getDisplayName().getString());
            };
        };
    }
    
    private static String getItemCategory(ItemStack stack) {
        var item = stack.getItem();
        
        if (item instanceof net.minecraft.world.item.BlockItem) {
            return "1_Blocks";
        }
        if (item instanceof net.minecraft.world.item.SwordItem || 
            item instanceof net.minecraft.world.item.AxeItem ||
            item instanceof net.minecraft.world.item.PickaxeItem ||
            item instanceof net.minecraft.world.item.ShovelItem ||
            item instanceof net.minecraft.world.item.HoeItem ||
            item instanceof net.minecraft.world.item.BowItem ||
            item instanceof net.minecraft.world.item.CrossbowItem ||
            item instanceof net.minecraft.world.item.TridentItem) {
            return "2_Weapons_Tools";
        }
        if (item instanceof net.minecraft.world.item.ArmorItem) {
            return "3_Armor";
        }
        if (item.isEdible()) {
            return "4_Food";
        }
        if (stack.getBarWidth() > 0) {
            return "5_Durability";
        }
        return "6_Other";
    }
    
    public static boolean isSorting() {
        return isSorting;
    }
    
    public static void reset() {
        isSorting = false;
        pendingActions.clear();
        actionIndex = 0;
        currentContainerId = -1;
    }
}
