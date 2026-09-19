/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Type
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.shampoon.pvputils;

import com.shampoon.pvputils.config.ConfigScreen;
import com.shampoon.pvputils.config.PVPUtilsConfig;
import com.shampoon.pvputils.features.ItemPicker;
import com.shampoon.pvputils.integration.ItemScrollerIntegration;
import com.shampoon.speedysclient.ui.SpeedysMainMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PVPUtils {
    public static final String MOD_ID = "pvputils";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"pvputils");
    public static PVPUtilsConfig CONFIG;
    private static KeyBinding configKeyBinding;
    private static KeyBinding itemPickerKeyBinding;
    private static KeyBinding fastXPKeyBinding;
    private static KeyBinding autoSprintKeyBinding;
    private static KeyBinding armorHudKeyBinding;
    private static KeyBinding effectTimerKeyBinding;
    private static KeyBinding fullBrightKeyBinding;
    private static KeyBinding zoomHoldKeyBinding;

    public static void initClient() {
        CONFIG = PVPUtilsConfig.load();
        ItemScrollerIntegration.applyConfigToItemScroller();
        ItemPicker.register();
        configKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.config", InputUtil.Type.KEYSYM, 344, "category.pvputils"));
        itemPickerKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.itempicker", InputUtil.Type.KEYSYM, 73, "category.pvputils"));
        fastXPKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.fastxp", InputUtil.Type.KEYSYM, 88, "category.pvputils"));
        autoSprintKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.autosprint", InputUtil.Type.KEYSYM, 82, "category.pvputils"));
        armorHudKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.armorhud", InputUtil.Type.KEYSYM, -1, "category.pvputils"));
        effectTimerKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.effecttimer", InputUtil.Type.KEYSYM, -1, "category.pvputils"));
        fullBrightKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.fullbright", InputUtil.Type.KEYSYM, -1, "category.pvputils"));
        zoomHoldKeyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.pvputils.zoom.hold", InputUtil.Type.KEYSYM, 67, "category.pvputils"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKeyBinding.wasPressed()) {
                client.setScreen((Screen)new SpeedysMainMenuScreen(client.currentScreen));
            }
            if (itemPickerKeyBinding.wasPressed()) {
                PVPUtils.CONFIG.itemPickerEnabled = !PVPUtils.CONFIG.itemPickerEnabled;
                CONFIG.save();
            }
            if (fastXPKeyBinding.wasPressed()) {
                PVPUtils.CONFIG.fastXPEnabled = !PVPUtils.CONFIG.fastXPEnabled;
                CONFIG.save();
            }
            if (autoSprintKeyBinding.wasPressed()) {
                PVPUtils.CONFIG.autoSprintEnabled = !PVPUtils.CONFIG.autoSprintEnabled;
                CONFIG.save();
            }
            if (armorHudKeyBinding.wasPressed()) {
                PVPUtils.CONFIG.armorHudEnabled = !PVPUtils.CONFIG.armorHudEnabled;
                CONFIG.save();
            }
            if (effectTimerKeyBinding.wasPressed()) {
                PVPUtils.CONFIG.effectTimerEnabled = !PVPUtils.CONFIG.effectTimerEnabled;
                CONFIG.save();
            }
            if (fullBrightKeyBinding.wasPressed()) {
                PVPUtils.CONFIG.fullBrightEnabled = !PVPUtils.CONFIG.fullBrightEnabled;
                CONFIG.save();
            }
        });
        LOGGER.info("PVPUtils (Speedys Client) initialized! Press Right Shift for menu.");
    }

    public static KeyBinding getArmorHudKeyBinding() {
        return armorHudKeyBinding;
    }

    public static KeyBinding getFastXPKeyBinding() {
        return fastXPKeyBinding;
    }

    public static KeyBinding getAutoSprintKeyBinding() {
        return autoSprintKeyBinding;
    }

    public static KeyBinding getEffectTimerKeyBinding() {
        return effectTimerKeyBinding;
    }

    public static KeyBinding getFullBrightKeyBinding() {
        return fullBrightKeyBinding;
    }

    public static KeyBinding getZoomHoldKeyBinding() {
        return zoomHoldKeyBinding;
    }

    public static void openLegacyConfig(MinecraftClient client) {
        if (client != null) {
            client.setScreen((Screen)new ConfigScreen(client.currentScreen));
        }
    }
}

