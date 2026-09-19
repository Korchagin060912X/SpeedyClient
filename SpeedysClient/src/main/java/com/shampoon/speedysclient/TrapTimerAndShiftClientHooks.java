/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  org.lwjgl.glfw.GLFW
 */
package com.shampoon.speedysclient;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.SpeedysClient;
import com.shampoon.speedysclient.features.TrapTimerFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class TrapTimerAndShiftClientHooks {
    private static boolean prevMouseRight;

    private TrapTimerAndShiftClientHooks() {
    }

    public static void tick(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.trapTimerEnabled) {
            return;
        }
        if (client.player == null || client.currentScreen != null) {
            return;
        }
        long window = client.getWindow().getHandle();
        boolean mouseRight = GLFW.glfwGetMouseButton((long)window, (int)1) == 1;
        boolean mouseEdge = mouseRight && !prevMouseRight;
        prevMouseRight = mouseRight;
        boolean usePressed = client.options.useKey.wasPressed();
        if (!mouseEdge && !usePressed) {
            return;
        }
        ItemStack main = client.player.getMainHandStack();
        ItemStack off = client.player.getOffHandStack();
        boolean hasScrap = main.isOf(Items.NETHERITE_SCRAP) || off.isOf(Items.NETHERITE_SCRAP);
        SpeedysClient.LOGGER.info("[TrapTimer] input detected: mouseEdge={}, usePressed={}, main={}, off={}, hasScrap={}", new Object[]{mouseEdge, usePressed, main.getItem().toString(), off.getItem().toString(), hasScrap});
        if (!hasScrap) {
            SpeedysClient.LOGGER.info("[TrapTimer] start skipped: Netherite Scrap not in hand");
            return;
        }
        TrapTimerFeature.handleScrapUse();
    }
}

