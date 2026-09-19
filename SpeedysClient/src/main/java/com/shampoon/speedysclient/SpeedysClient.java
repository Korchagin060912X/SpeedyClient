/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Type
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.shampoon.speedysclient;

import com.pearllandingpredictor.PearlLandingConfig;
import com.pearllandingpredictor.PearlLandingHooks;
import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.EffectTimerClassicHudDrag;
import com.shampoon.speedysclient.TrapTimerAndShiftClientHooks;
import com.shampoon.speedysclient.config.SpeedysWatermarkConfig;
import com.shampoon.speedysclient.config.WaymarksConfig;
import com.shampoon.speedysclient.waymarks.WaymarksHudRenderer;
import com.shampoon.speedysclient.waymarks.WaymarksTick;
import com.shampoon.speedysclient.features.AutoPotionFeature;
import com.shampoon.speedysclient.features.EatHelperFeature;
import com.shampoon.speedysclient.features.PotionHighlighterFeature;
import com.shampoon.speedysclient.features.InvHudFeature;
import com.shampoon.speedysclient.features.MiniHudNotifications;
import com.shampoon.speedysclient.features.SpeedysUtilsFeature;
import com.shampoon.speedysclient.features.SpeedysVisualsRenderer;
import com.shampoon.speedysclient.features.SpeedysVisualsTick;
import com.shampoon.speedysclient.features.TargetHudFeature;
import com.shampoon.speedysclient.features.CooldownListHud;
import com.shampoon.speedysclient.features.EffectTimerListHud;
import com.shampoon.speedysclient.features.HudPanelDragCoordinator;
import com.shampoon.speedysclient.features.TrapTimerFeature;
import com.shampoon.speedysclient.hud.SpeedysWatermark;
import io.github.musicintegration.gsmtc.GsmtcService;
import io.github.musicintegration.platform.WindowsSupport;
import me.shampoon.attackindicator.ModConfig;
import me.shampoon.cooldownitem.CooldownItemConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.artem.durabilitytint.DurabilityTintConfig;
import ru.artem.durabilitytint.ui.DurabilityTintConfigScreen;

public final class SpeedysClient
implements ClientModInitializer {
    public static final String MOD_ID = "speedysclient";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"speedysclient");

    public void onInitializeClient() {
        SpeedysWatermarkConfig.load();
        WaymarksConfig.load();
        PVPUtils.initClient();
        ModConfig.load();
        DurabilityTintConfig.load();
        CooldownItemConfig.load();
        PearlLandingConfig.load();
        PearlLandingHooks.register();
        if (WindowsSupport.isWindows()) {
            io.github.musicintegration.config.ModConfig.load();
            GsmtcService.start();
        }
        KeyBinding durabilityMenuKey = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.durability_tint.open_settings", InputUtil.Type.KEYSYM, 79, "category.durability_tint"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (durabilityMenuKey.wasPressed()) {
                if (client.player == null) continue;
                client.setScreen((Screen)new DurabilityTintConfigScreen(client.currentScreen));
            }
        });
        HudRenderCallback.EVENT.register(SpeedysWatermark::render);
        ClientTickEvents.END_CLIENT_TICK.register(SpeedysWatermark::tick);
        WorldRenderEvents.LAST.register(SpeedysVisualsRenderer::render);
        SpeedysUtilsFeature.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TrapTimerAndShiftClientHooks.tick(client);
            TrapTimerFeature.tick(client);
            SpeedysVisualsTick.tick(client);
            SpeedysUtilsFeature.tick(client);
            AutoPotionFeature.tick(client);
            EatHelperFeature.tick(client);
            MiniHudNotifications.tick(client);
            WaymarksTick.tick(client);
        });
        HudRenderCallback.EVENT.register(TrapTimerFeature::renderHud);
        HudRenderCallback.EVENT.register(EatHelperFeature::renderHudOverlays);
        HudRenderCallback.EVENT.register(PotionHighlighterFeature::renderHudOverlays);
        HudRenderCallback.EVENT.register((context, tickCounter) -> TargetHudFeature.render(context));
        HudRenderCallback.EVENT.register((context, tickCounter) -> InvHudFeature.render(context));
        HudRenderCallback.EVENT.register(MiniHudNotifications::render);
        HudRenderCallback.EVENT.register(EffectTimerClassicHudDrag::tickDrag);
        HudRenderCallback.EVENT.register(EffectTimerListHud::render);
        HudRenderCallback.EVENT.register(CooldownListHud::render);
        HudRenderCallback.EVENT.register(HudPanelDragCoordinator::endFrame);
        HudRenderCallback.EVENT.register(WaymarksHudRenderer::render);
        LOGGER.info("Speedys Client loaded");
    }
}

