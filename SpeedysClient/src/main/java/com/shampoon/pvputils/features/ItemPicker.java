/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.Box
 *  net.minecraft.world.GameMode
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

public class ItemPicker {
    private static final Map<Integer, ItemData> trackedItems = new HashMap<Integer, ItemData>();
    private static final Map<Item, Integer> previousInventory = new HashMap<Item, Integer>();
    private static boolean wasInCreative = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }
            if (!PVPUtils.CONFIG.itemPickerEnabled) {
                return;
            }
            try {
                int id;
                boolean isCreative = false;
                if (client.interactionManager != null) {
                    boolean bl = isCreative = client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE;
                }
                if (isCreative && !wasInCreative) {
                    trackedItems.clear();
                    previousInventory.clear();
                    wasInCreative = true;
                    return;
                }
                if (isCreative) {
                    return;
                }
                wasInCreative = false;
                if (client.currentScreen != null) {
                    return;
                }
                HashMap<Item, Integer> currentInventory = new HashMap<Item, Integer>();
                for (int i = 0; i < client.player.getInventory().size(); ++i) {
                    ItemStack stack = client.player.getInventory().getStack(i);
                    if (stack.isEmpty()) continue;
                    Item item = stack.getItem();
                    currentInventory.put(item, currentInventory.getOrDefault(item, 0) + stack.getCount());
                }
                Box searchBox = client.player.getBoundingBox().expand(48.0);
                List<ItemEntity> nearbyItems = client.world.getEntitiesByClass(ItemEntity.class, searchBox, entity -> !entity.getStack().isEmpty());
                HashMap<Integer, ItemData> currentItems = new HashMap<Integer, ItemData>();
                for (ItemEntity itemEntity : nearbyItems) {
                    id = itemEntity.getId();
                    ItemStack stack = itemEntity.getStack();
                    String itemName = stack.getName().getString();
                    int count = stack.getCount();
                    Item item = stack.getItem();
                    double distance = client.player.squaredDistanceTo((Entity)itemEntity);
                    boolean isClose = distance < 4.0;
                    ItemData oldData = trackedItems.get(id);
                    if (oldData != null && oldData.wasClose) {
                        isClose = true;
                    }
                    currentItems.put(id, new ItemData(itemName, count, isClose, distance, item));
                }
                for (Map.Entry<Integer, ItemData> entry : trackedItems.entrySet()) {
                    id = entry.getKey();
                    ItemData oldData = entry.getValue();
                    if (currentItems.containsKey(id) || !oldData.wasClose || !(oldData.lastDistance < 4.0)) continue;
                    int oldCount = previousInventory.getOrDefault(oldData.item, 0);
                    int newCount = currentInventory.getOrDefault(oldData.item, 0);
                    if (newCount <= oldCount) continue;
                    int pickedUp = newCount - oldCount;
                    String message = String.format("\u00a77[\u00a7l\u00a71SpeedyClient\u00a77] \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043d \u043f\u0440\u0435\u0434\u043c\u0435\u0442 %s \u0445%d", oldData.itemName, pickedUp);
                    client.player.sendMessage((Text)Text.literal((String)message), false);
                }
                trackedItems.clear();
                trackedItems.putAll(currentItems);
                previousInventory.clear();
                previousInventory.putAll(currentInventory);
            }
            catch (Exception e) {
                PVPUtils.LOGGER.error("ItemPicker error: " + e.getMessage(), (Throwable)e);
            }
        });
    }

    private static class ItemData {
        String itemName;
        int count;
        boolean wasClose;
        double lastDistance;
        Item item;

        ItemData(String itemName, int count, boolean wasClose, double distance, Item item) {
            this.itemName = itemName;
            this.count = count;
            this.wasClose = wasClose;
            this.lastDistance = distance;
            this.item = item;
        }
    }
}

