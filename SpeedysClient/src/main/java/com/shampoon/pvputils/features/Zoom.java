/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.option.KeyBinding
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class Zoom {
    public static final int LEVEL_MIN = 1;
    public static final int LEVEL_MAX = 50;

    private Zoom() {
    }

    public static int clampLevel(int level) {
        return Math.clamp((long)level, 1, 50);
    }

    public static boolean shouldApplyZoomFov(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.zoomEnabled) {
            return false;
        }
        if (client.player == null || client.world == null) {
            return false;
        }
        if (client.currentScreen != null) {
            return false;
        }
        KeyBinding key = PVPUtils.getZoomHoldKeyBinding();
        if (key == null || key.isUnbound()) {
            return false;
        }
        return key.isPressed();
    }

    public static float multiplier() {
        return Math.max(1, Math.min(50, PVPUtils.CONFIG.zoomLevel));
    }
}

