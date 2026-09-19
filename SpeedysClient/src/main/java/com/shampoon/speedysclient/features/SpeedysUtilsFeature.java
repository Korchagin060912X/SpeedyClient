/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Type
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.text.Text
 */
package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class SpeedysUtilsFeature {
    private static KeyBinding itemSwapKey;

    private SpeedysUtilsFeature() {
    }

    public static void init() {
        itemSwapKey = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.speedysclient.item_swap", InputUtil.Type.KEYSYM, 86, "category.pvputils"));
    }

    public static KeyBinding getItemSwapKey() {
        return itemSwapKey;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || PVPUtils.CONFIG == null) {
            return;
        }
        while (itemSwapKey != null && itemSwapKey.wasPressed()) {
            SpeedysUtilsFeature.trySwap(client);
        }
    }

    public static void onTotemPop(Entity entity) {
        PlayerEntity target;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || PVPUtils.CONFIG == null || !PVPUtils.CONFIG.totemTrackerEnabled) {
            return;
        }
        if (!(entity instanceof PlayerEntity) || (target = (PlayerEntity)entity) == client.player) {
            return;
        }
        ItemStack off = target.getOffHandStack();
        ItemStack main = target.getMainHandStack();
        boolean enchanted = off.isOf(Items.TOTEM_OF_UNDYING) && off.hasEnchantments() || main.isOf(Items.TOTEM_OF_UNDYING) && main.hasEnchantments();
        String talik = enchanted ? "\u00a7a\u0422\u0430\u043b\u0438\u043a\u2714" : "\u00a7c\u0422\u0430\u043b\u0438\u043a\u2716";
        Text msg = Text.literal("\u00a7l\u00a71[SpeedyClient]\u00a7r: " + target.getName().getString() + " \u043f\u043e\u0442\u0435\u0440\u044f\u043b \u0442\u043e\u0442\u0435\u043c. " + talik);
        if (PVPUtils.CONFIG.miniHudNotifications) {
            MiniHudNotifications.pushTotem(msg);
        } else {
            client.player.sendMessage(msg, false);
        }
    }

    public static boolean isLockedHotbarSlot(int slotZeroBased) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.lockSlotsEnabled) {
            return false;
        }
        return SpeedysUtilsFeature.parseLockedSlots(PVPUtils.CONFIG.lockSlotsCsv).contains(slotZeroBased + 1);
    }

    private static void trySwap(MinecraftClient client) {
        if (client.interactionManager == null || !PVPUtils.CONFIG.itemSwapEnabled) {
            return;
        }
        String item1 = SpeedysUtilsFeature.normalize(PVPUtils.CONFIG.itemSwapItem1Name);
        String item2 = SpeedysUtilsFeature.normalize(PVPUtils.CONFIG.itemSwapItem2Name);
        if (item1.isEmpty() || item2.isEmpty()) {
            return;
        }
        int handSlotId = PVPUtils.CONFIG.itemSwapUseOffhand ? 45 : SpeedysUtilsFeature.toScreenSlot(client.player.getInventory().getSelectedSlot());
        ItemStack current = PVPUtils.CONFIG.itemSwapUseOffhand ? client.player.getOffHandStack() : client.player.getMainHandStack();
        String currentName = SpeedysUtilsFeature.normalize(current.getName().getString());
        String targetName = currentName.contains(item1) ? item2 : item1;
        int sourceSlotId = SpeedysUtilsFeature.findInventoryScreenSlot(client, targetName);
        if (sourceSlotId < 0) {
            return;
        }
        ItemStack sourceStack = sourceSlotId == 45 ? client.player.getOffHandStack().copy() : client.player.getInventory().getStack(SpeedysUtilsFeature.fromScreenSlot(sourceSlotId)).copy();
        int syncId = client.player.playerScreenHandler.syncId;
        client.interactionManager.clickSlot(syncId, sourceSlotId, 0, SlotActionType.PICKUP, (PlayerEntity)client.player);
        client.interactionManager.clickSlot(syncId, handSlotId, 0, SlotActionType.PICKUP, (PlayerEntity)client.player);
        client.interactionManager.clickSlot(syncId, sourceSlotId, 0, SlotActionType.PICKUP, (PlayerEntity)client.player);
        MutableText line =
                Text.literal("\u00a7l\u00a71[SpeedyClient]\u00a7r: \u0421\u0432\u0430\u043f\u043d\u0443\u043b \u043d\u0430 ")
                        .append(sourceStack.getName());
        if (PVPUtils.CONFIG.miniHudNotifications) {
            MiniHudNotifications.pushItemSwap(line);
        } else {
            client.player.sendMessage(line, true);
        }
    }

    private static int findInventoryScreenSlot(MinecraftClient client, String normalizedNamePart) {
        for (int invSlot = 0; invSlot < 36; ++invSlot) {
            ItemStack stack = client.player.getInventory().getStack(invSlot);
            if (stack.isEmpty() || !SpeedysUtilsFeature.normalize(stack.getName().getString()).contains(normalizedNamePart)) continue;
            return SpeedysUtilsFeature.toScreenSlot(invSlot);
        }
        ItemStack offhand = client.player.getOffHandStack();
        if (!offhand.isEmpty() && SpeedysUtilsFeature.normalize(offhand.getName().getString()).contains(normalizedNamePart)) {
            return 45;
        }
        return -1;
    }

    private static int toScreenSlot(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return 36 + invSlot;
        }
        return invSlot;
    }

    private static int fromScreenSlot(int screenSlot) {
        if (screenSlot >= 36 && screenSlot <= 44) {
            return screenSlot - 36;
        }
        return screenSlot;
    }

    private static Set<Integer> parseLockedSlots(String csv) {
        String[] parts;
        HashSet<Integer> out = new HashSet<Integer>();
        if (csv == null || csv.isBlank()) {
            return out;
        }
        for (String p : parts = csv.split(",")) {
            try {
                int slot = Integer.parseInt(p.trim());
                if (slot < 1 || slot > 9) continue;
                out.add(slot);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return out;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }

}

