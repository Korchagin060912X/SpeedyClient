package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.SpeedysClient;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/**
 * Таймер трапки + оверлей зоны. Повторное нажатие ПКМ / use со скрапом в руке сбрасывает таймер и убирает коробку.
 */
public final class TrapTimerFeature {
    private static double secondsLeft = 0.0;
    private static boolean finishLogged;
    private static int lastRenderLogSecond;
    private static long lastStartAtMs;
    private static int lastActionbarTick;

    private TrapTimerFeature() {
    }

    public static void handleScrapUse() {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.trapTimerEnabled) {
            SpeedysClient.LOGGER.info("[TrapTimer] skipped: disabled or no config");
            return;
        }
        if (secondsLeft > 0.0) {
            secondsLeft = 0.0;
            finishLogged = true;
            lastActionbarTick = -1;
            lastRenderLogSecond = Integer.MIN_VALUE;
            MiniHudNotifications.clearTrap();
            SpeedysClient.LOGGER.info("[TrapTimer] cancelled (повторное действие — убрали трапку / сброс)");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastStartAtMs < 350L) {
            SpeedysClient.LOGGER.info("[TrapTimer] start ignored: anti-spam");
            return;
        }
        lastStartAtMs = now;
        secondsLeft = PVPUtils.CONFIG.trapTimerDragonMode ? 30.0 : 15.0;
        finishLogged = false;
        lastRenderLogSecond = Integer.MIN_VALUE;
        lastActionbarTick = -1;
        SpeedysClient.LOGGER.info("[TrapTimer] started: {}s", String.format(Locale.US, "%.1f", secondsLeft));
    }

    public static void tick(MinecraftClient client) {
        if (secondsLeft <= 0.0) {
            return;
        }
        secondsLeft = Math.max(0.0, secondsLeft - 0.05);
        if (client.player != null && PVPUtils.CONFIG != null) {
            boolean mini = PVPUtils.CONFIG.miniHudNotifications && PVPUtils.CONFIG.trapTimerEnabled;
            if (mini) {
                MiniHudNotifications.updateTrapCountdown(secondsLeft);
            } else {
                MiniHudNotifications.clearTrap();
                int tenth = (int) Math.floor(secondsLeft * 10.0);
                if (tenth != lastActionbarTick) {
                    lastActionbarTick = tenth;
                    String num = String.format(Locale.US, "%.1f", secondsLeft);
                    client.player.sendMessage(
                            Text.literal("\u00a7l\u00a71[SpeedyClient]\u00a7r \u0422\u0440\u0430\u043f\u043a\u0430 \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437: " + num),
                            true);
                }
            }
        }
        if (secondsLeft <= 0.0 && !finishLogged) {
            finishLogged = true;
            SpeedysClient.LOGGER.info("[TrapTimer] finished");
        }
    }

    public static boolean isCountdownActive() {
        return secondsLeft > 0.0;
    }

    public static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (secondsLeft <= 0.0) {
            return;
        }
        int wholeSecond = (int) Math.ceil(secondsLeft);
        if (wholeSecond != lastRenderLogSecond) {
            lastRenderLogSecond = wholeSecond;
            SpeedysClient.LOGGER.info("[TrapTimer] actionbar tick, left={}s", String.format(Locale.US, "%.1f", secondsLeft));
        }
    }

    static {
        lastRenderLogSecond = Integer.MIN_VALUE;
        lastActionbarTick = -1;
    }
}
