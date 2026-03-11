package org.alku.catalyst.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.alku.catalyst.config.CatalystConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventorySorter {
    
    private static boolean sorting = false;
    
    public static void sortCurrentContainer(Minecraft mc) {
        if (!CatalystConfig.getInstance().inventorySorterEnabled || sorting) {
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
        
        System.out.println("[Catalyst] Sorting container: " + container.getClass().getSimpleName() + " (inventorySorterEnabled=" + CatalystConfig.getInstance().inventorySorterEnabled + ", autoSortOnOpen=" + CatalystConfig.getInstance().autoSortOnOpen + ")");
        
        sorting = true;
        try {
            int containerId = container.containerId;
            
            if (container instanceof InventoryMenu) {
                sortPlayerInventory(mc, player, container, containerId);
            } else {
                sortOpenContainer(mc, player, container, containerId);
            }
        } finally {
            sorting = false;
        }
    }
    
    public static void tick(Minecraft mc) {
    }
    
    private static void clickSlot(Minecraft mc, int containerId, int slotIndex, int button, ClickType clickType) {
        if (mc.player != null && mc.gameMode != null) {
            mc.gameMode.handleInventoryMouseClick(containerId, slotIndex, button, clickType, mc.player);
        }
    }
    
    public static void sortPlayerInventory(Minecraft mc, LocalPlayer player, AbstractContainerMenu container, int containerId) {
        List<Integer> mainInventorySlots = new ArrayList<>();
        List<Integer> hotbarSlots = new ArrayList<>();
        
        Container playerInventory = player.getInventory();
        
        for (int i = 0; i < container.slots.size(); i++) {
            Slot slot = container.getSlot(i);
            if (slot.container == playerInventory) {
                int containerSlot = slot.getContainerSlot();
                if (containerSlot >= 0 && containerSlot < 9) {
                    hotbarSlots.add(i);
                } else if (containerSlot >= 9 && containerSlot < 36) {
                    mainInventorySlots.add(i);
                }
            }
        }
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        doSort(mc, container, containerId, mainInventorySlots, config.sortMode);
        
        if (config.sortHotbar) {
            doSort(mc, container, containerId, hotbarSlots, config.sortMode);
        }
    }
    
    public static void sortOpenContainer(Minecraft mc, LocalPlayer player, AbstractContainerMenu container, int containerId) {
        List<Integer> containerSlots = new ArrayList<>();
        List<Integer> playerInvSlots = new ArrayList<>();
        List<Integer> playerHotbarSlots = new ArrayList<>();
        
        Container playerInventory = player.getInventory();
        
        for (int i = 0; i < container.slots.size(); i++) {
            Slot slot = container.getSlot(i);
            if (slot.container == playerInventory) {
                int containerSlot = slot.getContainerSlot();
                if (containerSlot >= 0 && containerSlot < 9) {
                    playerHotbarSlots.add(i);
                } else if (containerSlot >= 9 && containerSlot < 36) {
                    playerInvSlots.add(i);
                }
            } else if (slot.container != null && slot.container != playerInventory) {
                containerSlots.add(i);
            }
        }
        
        System.out.println("[Catalyst] sortOpenContainer: totalSlots=" + container.slots.size() + ", containerSlots=" + containerSlots.size() + ", playerInvSlots=" + playerInvSlots.size() + ", playerHotbarSlots=" + playerHotbarSlots.size());
        
        CatalystConfig config = CatalystConfig.getInstance();
        
        doSort(mc, container, containerId, containerSlots, config.sortMode);
        
        if (config.sortPlayerInventoryInContainer) {
            doSort(mc, container, containerId, playerInvSlots, config.sortMode);
            if (config.sortHotbar) {
                doSort(mc, container, containerId, playerHotbarSlots, config.sortMode);
            }
        }
    }
    
    private static void doSort(Minecraft mc, AbstractContainerMenu container, int containerId, 
                                List<Integer> slotIndices, int sortMode) {
        if (slotIndices.isEmpty()) {
            return;
        }
        
        int n = slotIndices.size();
        
        List<ItemStack> currentItems = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            currentItems.add(container.getSlot(slotIndices.get(i)).getItem().copy());
        }
        
        mergeItems(mc, container, containerId, slotIndices, currentItems);
        
        currentItems.clear();
        for (int i = 0; i < n; i++) {
            currentItems.add(container.getSlot(slotIndices.get(i)).getItem().copy());
        }
        
        List<ItemStack> targetItems = buildTargetItems(currentItems, sortMode);
        
        boolean[] done = new boolean[n];
        
        for (int targetIdx = 0; targetIdx < n; targetIdx++) {
            if (done[targetIdx]) continue;
            
            ItemStack target = targetItems.get(targetIdx);
            
            if (isAlreadyCorrect(currentItems, targetItems, targetIdx)) {
                done[targetIdx] = true;
                continue;
            }
            
            if (target.isEmpty()) {
                ItemStack current = currentItems.get(targetIdx);
                if (!current.isEmpty()) {
                    int emptyIdx = findEmptySlot(currentItems, done, targetIdx);
                    if (emptyIdx != -1) {
                        moveItem(mc, containerId, slotIndices, currentItems, targetIdx, emptyIdx);
                    }
                }
                done[targetIdx] = true;
                continue;
            }
            
            int sourceIdx = findSourceSlot(currentItems, target, done, targetIdx);
            if (sourceIdx == -1) {
                done[targetIdx] = true;
                continue;
            }
            
            ItemStack atTarget = currentItems.get(targetIdx);
            
            if (atTarget.isEmpty()) {
                moveItem(mc, containerId, slotIndices, currentItems, sourceIdx, targetIdx);
                done[targetIdx] = true;
            } else {
                int emptyIdx = findEmptySlot(currentItems, done, targetIdx);
                if (emptyIdx != -1) {
                    moveItem(mc, containerId, slotIndices, currentItems, targetIdx, emptyIdx);
                    moveItem(mc, containerId, slotIndices, currentItems, sourceIdx, targetIdx);
                    done[targetIdx] = true;
                    done[emptyIdx] = false;
                } else {
                    swapItems(mc, containerId, slotIndices, currentItems, targetIdx, sourceIdx);
                    done[targetIdx] = true;
                    done[sourceIdx] = false;
                }
            }
        }
    }
    
    private static void mergeItems(Minecraft mc, AbstractContainerMenu container, int containerId,
                                    List<Integer> slotIndices, List<ItemStack> items) {
        int n = slotIndices.size();
        
        for (int i = 0; i < n; i++) {
            ItemStack stackI = items.get(i);
            if (stackI.isEmpty()) continue;
            
            int maxStack = stackI.getMaxStackSize();
            if (stackI.getCount() >= maxStack) continue;
            
            for (int j = i + 1; j < n; j++) {
                ItemStack stackJ = items.get(j);
                if (stackJ.isEmpty()) continue;
                
                if (!ItemStack.isSameItemSameTags(stackI, stackJ)) continue;
                
                int spaceInI = maxStack - stackI.getCount();
                int toMove = Math.min(spaceInI, stackJ.getCount());
                
                if (toMove > 0) {
                    mergeStacks(mc, containerId, slotIndices, items, j, i, toMove);
                    stackI = items.get(i);
                    if (stackI.getCount() >= maxStack) break;
                }
            }
        }
    }
    
    private static void mergeStacks(Minecraft mc, int containerId, List<Integer> slotIndices,
                                     List<ItemStack> items, int fromIdx, int toIdx, int amount) {
        int fromSlot = slotIndices.get(fromIdx);
        int toSlot = slotIndices.get(toIdx);
        
        clickSlot(mc, containerId, fromSlot, 0, ClickType.PICKUP);
        clickSlot(mc, containerId, toSlot, 0, ClickType.PICKUP);
        clickSlot(mc, containerId, fromSlot, 0, ClickType.PICKUP);
        
        ItemStack fromStack = items.get(fromIdx);
        ItemStack toStack = items.get(toIdx);
        
        int actualMoved = Math.min(amount, fromStack.getCount());
        toStack.grow(actualMoved);
        fromStack.shrink(actualMoved);
        
        if (fromStack.isEmpty()) {
            items.set(fromIdx, ItemStack.EMPTY);
        }
    }
    
    private static boolean isAlreadyCorrect(List<ItemStack> current, List<ItemStack> target, int idx) {
        ItemStack currentStack = current.get(idx);
        ItemStack targetStack = target.get(idx);
        
        if (currentStack.isEmpty() && targetStack.isEmpty()) return true;
        if (currentStack.isEmpty() || targetStack.isEmpty()) return false;
        
        return ItemStack.isSameItemSameTags(currentStack, targetStack) && currentStack.getCount() == targetStack.getCount();
    }
    
    private static int findEmptySlot(List<ItemStack> items, boolean[] done, int excludeIdx) {
        for (int i = excludeIdx + 1; i < items.size(); i++) {
            if (!done[i] && items.get(i).isEmpty()) {
                return i;
            }
        }
        for (int i = 0; i < excludeIdx; i++) {
            if (!done[i] && items.get(i).isEmpty()) {
                return i;
            }
        }
        for (int i = 0; i < items.size(); i++) {
            if (i != excludeIdx && items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
    
    private static int findSourceSlot(List<ItemStack> items, ItemStack target, boolean[] done, int excludeIdx) {
        for (int i = 0; i < items.size(); i++) {
            if (i == excludeIdx || done[i]) continue;
            
            ItemStack src = items.get(i);
            if (!src.isEmpty() && ItemStack.isSameItemSameTags(src, target) && src.getCount() == target.getCount()) {
                return i;
            }
        }
        
        for (int i = 0; i < items.size(); i++) {
            if (i == excludeIdx || done[i]) continue;
            
            ItemStack src = items.get(i);
            if (!src.isEmpty() && ItemStack.isSameItemSameTags(src, target)) {
                return i;
            }
        }
        
        return -1;
    }
    
    private static void moveItem(Minecraft mc, int containerId, List<Integer> slotIndices, 
                                  List<ItemStack> items, int fromIdx, int toIdx) {
        int fromSlot = slotIndices.get(fromIdx);
        int toSlot = slotIndices.get(toIdx);
        
        clickSlot(mc, containerId, fromSlot, 0, ClickType.PICKUP);
        clickSlot(mc, containerId, toSlot, 0, ClickType.PICKUP);
        
        ItemStack temp = items.get(fromIdx);
        items.set(fromIdx, items.get(toIdx));
        items.set(toIdx, temp);
    }
    
    private static void swapItems(Minecraft mc, int containerId, List<Integer> slotIndices, 
                                   List<ItemStack> items, int idx1, int idx2) {
        int slot1 = slotIndices.get(idx1);
        int slot2 = slotIndices.get(idx2);
        
        clickSlot(mc, containerId, slot1, 0, ClickType.PICKUP);
        clickSlot(mc, containerId, slot2, 0, ClickType.PICKUP);
        clickSlot(mc, containerId, slot1, 0, ClickType.PICKUP);
        
        ItemStack temp = items.get(idx1);
        items.set(idx1, items.get(idx2));
        items.set(idx2, temp);
    }
    
    private static List<ItemStack> buildTargetItems(List<ItemStack> currentItems, int sortMode) {
        Map<ItemKey, MergedItem> mergedMap = new HashMap<>();
        
        for (ItemStack stack : currentItems) {
            if (!stack.isEmpty()) {
                ItemKey key = new ItemKey(stack);
                MergedItem merged = mergedMap.computeIfAbsent(key, k -> new MergedItem(stack.copy()));
                merged.totalCount += stack.getCount();
            }
        }
        
        List<ItemStack> result = new ArrayList<>();
        
        for (MergedItem merged : mergedMap.values()) {
            int remaining = merged.totalCount;
            int maxStack = merged.template.getMaxStackSize();
            
            while (remaining > 0) {
                int count = Math.min(remaining, maxStack);
                ItemStack newStack = merged.template.copy();
                newStack.setCount(count);
                result.add(newStack);
                remaining -= count;
            }
        }
        
        result.sort(getComparator(sortMode));
        
        while (result.size() < currentItems.size()) {
            result.add(ItemStack.EMPTY);
        }
        
        return result;
    }
    
    private static class ItemKey {
        private final ResourceLocation itemId;
        private final int componentHash;
        
        public ItemKey(ItemStack stack) {
            this.itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            this.componentHash = stack.getComponents().hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemKey itemKey = (ItemKey) o;
            return componentHash == itemKey.componentHash && itemId.equals(itemKey.itemId);
        }
        
        @Override
        public int hashCode() {
            return 31 * itemId.hashCode() + componentHash;
        }
    }
    
    private static class MergedItem {
        final ItemStack template;
        int totalCount;
        
        MergedItem(ItemStack template) {
            this.template = template;
            this.totalCount = 0;
        }
    }
    
    private static Comparator<ItemStack> getComparator(int sortMode) {
        return switch (sortMode) {
            case 1 -> new DisplayNameComparator();
            case 2 -> new ItemIdComparator();
            default -> new IPNRuleComparator();
        };
    }
    
    private static class IPNRuleComparator implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack a, ItemStack b) {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            
            int result = compareCreativeMenuOrder(a, b);
            if (result != 0) return result;
            
            result = compareCustomName(a, b);
            if (result != 0) return result;
            
            result = compareEnchantmentScore(a, b);
            if (result != 0) return result;
            
            result = compareDurability(a, b);
            if (result != 0) return result;
            
            result = compareDisplayName(a, b);
            if (result != 0) return result;
            
            result = comparePotionEffects(a, b);
            if (result != 0) return result;
            
            result = compareNBT(a, b);
            if (result != 0) return result;
            
            return Integer.compare(b.getCount(), a.getCount());
        }
        
        private int compareCreativeMenuOrder(ItemStack a, ItemStack b) {
            int indexA = getSearchTabIndex(a);
            int indexB = getSearchTabIndex(b);
            return Integer.compare(indexA, indexB);
        }
        
        private int getSearchTabIndex(ItemStack stack) {
            int idx = 0;
            for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                if (tab.getType() == CreativeModeTab.Type.CATEGORY) {
                    if (tab.contains(stack)) {
                        return idx;
                    }
                    idx++;
                }
            }
            return Integer.MAX_VALUE;
        }
        
        private int compareCustomName(ItemStack a, ItemStack b) {
            boolean hasCustomA = a.hasCustomHoverName();
            boolean hasCustomB = b.hasCustomHoverName();
            if (hasCustomA && !hasCustomB) return -1;
            if (!hasCustomA && hasCustomB) return 1;
            return 0;
        }
        
        private int compareEnchantmentScore(ItemStack a, ItemStack b) {
            int scoreA = computeEnchantmentScore(a);
            int scoreB = computeEnchantmentScore(b);
            return Integer.compare(scoreB, scoreA);
        }
        
        private int computeEnchantmentScore(ItemStack stack) {
            var enchantments = stack.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS);
            if (enchantments == null) return 0;
            
            int score = 0;
            for (var entry : enchantments.entrySet()) {
                score += entry.getIntValue();
            }
            return score;
        }
        
        private int compareDurability(ItemStack a, ItemStack b) {
            boolean damageableA = a.isDamageableItem();
            boolean damageableB = b.isDamageableItem();
            
            if (!damageableA && !damageableB) return 0;
            if (damageableA && !damageableB) return -1;
            if (!damageableA && damageableB) return 1;
            
            int durabilityA = a.getMaxDamage() - a.getDamageValue();
            int durabilityB = b.getMaxDamage() - b.getDamageValue();
            return Integer.compare(durabilityB, durabilityA);
        }
        
        private int compareDisplayName(ItemStack a, ItemStack b) {
            String nameA = a.getDisplayName().getString();
            String nameB = b.getDisplayName().getString();
            return nameA.compareToIgnoreCase(nameB);
        }
        
        private int comparePotionEffects(ItemStack a, ItemStack b) {
            boolean isPotionA = a.getItem() instanceof net.minecraft.world.item.PotionItem;
            boolean isPotionB = b.getItem() instanceof net.minecraft.world.item.PotionItem;
            
            if (isPotionA && isPotionB) {
                String potionA = a.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(p -> p.unwrapKey().map(k -> k.location().toString()).orElse("")).orElse("");
                String potionB = b.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(p -> p.unwrapKey().map(k -> k.location().toString()).orElse("")).orElse("");
                return potionA.compareTo(potionB);
            }
            if (isPotionA) return -1;
            if (isPotionB) return 1;
            return 0;
        }
        
        private int compareNBT(ItemStack a, ItemStack b) {
            boolean hasComponentsA = !a.getComponents().isEmpty();
            boolean hasComponentsB = !b.getComponents().isEmpty();
            
            if (hasComponentsA && hasComponentsB) {
                return a.getComponents().toString().compareTo(b.getComponents().toString());
            }
            if (hasComponentsA) return -1;
            if (hasComponentsB) return 1;
            return 0;
        }
    }
    
    private static class DisplayNameComparator implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack a, ItemStack b) {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            
            int result = compareCustomName(a, b);
            if (result != 0) return result;
            
            String nameA = a.getDisplayName().getString();
            String nameB = b.getDisplayName().getString();
            result = nameA.compareToIgnoreCase(nameB);
            if (result != 0) return result;
            
            return Integer.compare(b.getCount(), a.getCount());
        }
        
        private int compareCustomName(ItemStack a, ItemStack b) {
            boolean hasCustomA = a.hasCustomHoverName();
            boolean hasCustomB = b.hasCustomHoverName();
            if (hasCustomA && !hasCustomB) return -1;
            if (!hasCustomA && hasCustomB) return 1;
            return 0;
        }
    }
    
    private static class ItemIdComparator implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack a, ItemStack b) {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            
            ResourceLocation idA = BuiltInRegistries.ITEM.getKey(a.getItem());
            ResourceLocation idB = BuiltInRegistries.ITEM.getKey(b.getItem());
            
            int result = idA.getNamespace().compareTo(idB.getNamespace());
            if (result != 0) return result;
            
            result = idA.getPath().compareTo(idB.getPath());
            if (result != 0) return result;
            
            return Integer.compare(b.getCount(), a.getCount());
        }
    }
    
    public static boolean isSorting() {
        return sorting;
    }
    
    public static void reset() {
        sorting = false;
    }
}
