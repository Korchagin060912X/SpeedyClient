/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package me.shampoon.cooldownitem;

import me.shampoon.cooldownitem.CooldownItemConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CoolDownItemMod
implements ModInitializer {
    public static final String MOD_ID = "cooldownitem";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"cooldownitem");

    public void onInitialize() {
        CooldownItemConfig.load();
        LOGGER.info("CoolDownItem initialized");
    }
}

