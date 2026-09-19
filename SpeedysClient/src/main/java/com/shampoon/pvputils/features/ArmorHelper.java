/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class ArmorHelper {
    private static final float LOW_DURABILITY_FRACTION = 0.25f;
    private static final int LINE_HEIGHT = 12;
    private static final int MARGIN_RIGHT = 6;
    private static final int MARGIN_BOTTOM = 59;
    private static final int TEXT_COLOR = -43691;

    private ArmorHelper() {
    }

    public static String getWatermarkSummary(PlayerEntity player) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.armorHelperEnabled || player == null) {
            return "";
        }
        if (player.isSpectator() || player.isCreative()) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<String>(4);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            int left;
            float remaining;
            int max;
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable() || (max = stack.getMaxDamage()) <= 0 || (remaining = (float)(left = Math.max(0, max - stack.getDamage())) / (float)max) >= 0.25f) continue;
            parts.add(stack.getName().getString() + " " + Math.round(remaining * 100.0f) + "%");
        }
        if (parts.isEmpty()) {
            return "\u0411\u0440\u043e\u043d\u044f \u0432 \u043d\u043e\u0440\u043c\u0435";
        }
        return parts.stream().limit(2L).collect(Collectors.joining(" \u00b7 "));
    }

    public static void render(DrawContext context, int scaledWidth, int scaledHeight) {
    }
}

