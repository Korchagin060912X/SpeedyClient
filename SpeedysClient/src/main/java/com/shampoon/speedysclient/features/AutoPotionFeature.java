package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * Цикл: ждать интервал из настроек → удерживать ПКМ 3 секунды → отпустить → снова ждать → …
 */
public final class AutoPotionFeature {
    /** 3 секунды при 20 TPS. */
    private static final int HOLD_USE_TICKS = 60;

    private static int waitTicksAccumulator;
    /** &gt; 0 — фаза удержания ПКМ (оставшиеся тики). */
    private static int holdTicksRemaining;

    private AutoPotionFeature() {
    }

    public static void tick(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.autoPotionEnabled) {
            AutoPotionFeature.resetCountersOnly();
            return;
        }
        if (client.player == null || client.interactionManager == null || client.world == null) {
            AutoPotionFeature.fullReset(client);
            return;
        }
        if (client.currentScreen != null) {
            AutoPotionFeature.fullReset(client);
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player.isSpectator()) {
            AutoPotionFeature.fullReset(client);
            return;
        }

        ItemStack main = player.getMainHandStack();
        if (!AutoPotionFeature.isDrinkablePotion(main)) {
            if (AutoPotionFeature.holdTicksRemaining > 0) {
                client.options.useKey.setPressed(false);
            }
            AutoPotionFeature.holdTicksRemaining = 0;
            AutoPotionFeature.waitTicksAccumulator = 0;
            return;
        }

        int intervalTicks = AutoPotionFeature.intervalTicksFromConfig();

        if (AutoPotionFeature.holdTicksRemaining > 0) {
            client.options.useKey.setPressed(true);
            AutoPotionFeature.holdTicksRemaining--;
            if (AutoPotionFeature.holdTicksRemaining == 0) {
                client.options.useKey.setPressed(false);
            }
            return;
        }

        AutoPotionFeature.waitTicksAccumulator++;
        if (AutoPotionFeature.waitTicksAccumulator < intervalTicks) {
            return;
        }
        AutoPotionFeature.waitTicksAccumulator = 0;

        AutoPotionFeature.holdTicksRemaining = HOLD_USE_TICKS;
        client.interactionManager.interactItem((PlayerEntity) player, Hand.MAIN_HAND);
        client.options.useKey.setPressed(true);
        AutoPotionFeature.holdTicksRemaining--;
        if (AutoPotionFeature.holdTicksRemaining == 0) {
            client.options.useKey.setPressed(false);
        }
    }

    private static int intervalTicksFromConfig() {
        float delaySec = PVPUtils.CONFIG.autoPotionDelaySeconds;
        return Math.max(20, Math.round(delaySec * 20.0f));
    }

    /** Только счётчики — не трогаем {@code useKey}, иначе при выключенном автопоте каждый тик сбрасывается ПКМ (лук, еда, зелья). */
    private static void resetCountersOnly() {
        AutoPotionFeature.waitTicksAccumulator = 0;
        AutoPotionFeature.holdTicksRemaining = 0;
    }

    private static void fullReset(MinecraftClient client) {
        AutoPotionFeature.resetCountersOnly();
        if (client != null && client.options != null) {
            client.options.useKey.setPressed(false);
        }
    }

    private static boolean isDrinkablePotion(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(Items.POTION);
    }
}
