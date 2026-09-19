/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class AutoSprint {
    public static void tick(ClientPlayerEntity player) {
        if (!PVPUtils.CONFIG.autoSprintEnabled) {
            return;
        }
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.options == null) {
                return;
            }
            if (player.isSneaking() || player.isTouchingWater() || player.isSubmergedInWater()) {
                return;
            }
            if ((client.options.forwardKey.isPressed() || client.options.leftKey.isPressed() || client.options.rightKey.isPressed()) && player.getHungerManager().getFoodLevel() > 6 && !player.horizontalCollision) {
                player.setSprinting(true);
            }
        }
        catch (Exception e) {
            PVPUtils.LOGGER.error("AutoSprint error: " + e.getMessage(), (Throwable)e);
        }
    }
}

