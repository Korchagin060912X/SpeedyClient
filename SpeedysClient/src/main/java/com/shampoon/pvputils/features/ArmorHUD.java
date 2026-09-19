/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.item.ItemStack
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHUD {
    public static void render(DrawContext context, float tickDelta) {
        if (!PVPUtils.CONFIG.armorHudEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int startX = screenWidth / 2 + 10;
        int startY = screenHeight - 55;
        EquipmentSlot[] armorSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; ++i) {
            ItemStack armorPiece = player.getEquippedStack(armorSlots[i]);
            if (armorPiece.isEmpty()) continue;
            int x = startX + i * 20;
            int y = startY;
            context.drawItem(armorPiece, x, y);
            ArmorHUD.drawDurabilityBar(context, armorPiece, x, y);
        }
    }

    private static void drawDurabilityBar(DrawContext context, ItemStack stack, int x, int y) {
        if (!stack.isItemBarVisible()) {
            return;
        }
        int barStep = stack.getItemBarStep();
        int barColor = stack.getItemBarColor() | 0xFF000000;
        int barX = x + 2;
        int barY = y + 13;
        context.fill(barX, barY, barX + 13, barY + 2, 0xFF000000);
        if (barStep > 0) {
            context.fill(barX, barY, barX + barStep, barY + 1, barColor);
        }
    }
}

