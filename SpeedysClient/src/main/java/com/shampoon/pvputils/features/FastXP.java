/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class FastXP {
    private static int ticksSinceLastUse = 0;
    private static final int COOLDOWN_TICKS = 3;

    public static void tick(ClientPlayerEntity player) {
        if (!PVPUtils.CONFIG.fastXPEnabled) {
            return;
        }
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.options == null) {
                return;
            }
            ItemStack mainHand = player.getMainHandStack();
            if (mainHand == null || mainHand.isEmpty()) {
                return;
            }
            if (!mainHand.getItem().equals(Items.EXPERIENCE_BOTTLE)) {
                return;
            }
            if (client.options.useKey.isPressed()) {
                if (++ticksSinceLastUse >= 3) {
                    if (client.interactionManager != null) {
                        client.interactionManager.interactItem((PlayerEntity)player, player.getActiveHand());
                    }
                    ticksSinceLastUse = 0;
                }
            } else {
                ticksSinceLastUse = 0;
            }
        }
        catch (Exception e) {
            PVPUtils.LOGGER.error("FastXP error: " + e.getMessage(), (Throwable)e);
        }
    }
}

