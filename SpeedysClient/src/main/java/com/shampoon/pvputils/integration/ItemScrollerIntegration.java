/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.dy.masa.itemscroller.config.Configs$Generic
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.shampoon.pvputils.integration;

import com.shampoon.pvputils.PVPUtils;
import fi.dy.masa.itemscroller.config.Configs;
import net.fabricmc.loader.api.FabricLoader;

public final class ItemScrollerIntegration {
    private ItemScrollerIntegration() {
    }

    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("itemscroller");
    }

    public static void applyConfigToItemScroller() {
        if (!ItemScrollerIntegration.isAvailable() || PVPUtils.CONFIG == null) {
            return;
        }
        Configs.Generic.MOD_MAIN_TOGGLE.setBooleanValue(PVPUtils.CONFIG.itemScrollerEnabled);
    }
}

