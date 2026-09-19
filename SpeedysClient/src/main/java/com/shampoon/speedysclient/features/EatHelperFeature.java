package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

/**
 * Подсветка «лучшей» еды и зелий мгновенного исцеления I/II при низком HP и/или голоде.
 */
public final class EatHelperFeature {
    private static final Set<Integer> HIGHLIGHT = new HashSet<>();

    private EatHelperFeature() {
    }

    public static void tick(MinecraftClient client) {
        HIGHLIGHT.clear();
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.eatHelperEnabled || client.player == null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player.isSpectator()) {
            return;
        }
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        float needH = 1.0f - Math.min(1.0f, player.getHealth() / maxHp);
        float needF = 1.0f - Math.min(1.0f, player.getHungerManager().getFoodLevel() / 20.0f);
        if (needH < 0.06f && needF < 0.06f) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        int n = inv.size();
        float best = -1.0f;
        float[] scores = new float[n];
        for (int i = 0; i < n; i++) {
            float s = score(inv.getStack(i), needH, needF);
            scores[i] = s;
            if (s > best) {
                best = s;
            }
        }
        if (best <= 0.0f) {
            return;
        }
        for (int i = 0; i < n; i++) {
            if (scores[i] == best) {
                HIGHLIGHT.add(i);
            }
        }
    }

    public static boolean isHighlighted(int inventoryIndex) {
        return HIGHLIGHT.contains(inventoryIndex);
    }

    /**
     * Полупрозрачная заливка слота + лёгкая обводка, альфа плавно «мигает».
     */
    public static void drawSlotHighlight(DrawContext context, int itemX, int itemY) {
        if (PVPUtils.CONFIG == null) {
            return;
        }
        int r = PVPUtils.CONFIG.eatHelperR & 255;
        int g = PVPUtils.CONFIG.eatHelperG & 255;
        int b = PVPUtils.CONFIG.eatHelperB & 255;
        long ms = Util.getMeasuringTimeMs();
        long period = 900L;
        float t = (ms % period) / (float) period;
        float wave = 0.5f + 0.5f * MathHelper.sin(t * MathHelper.TAU);
        int aFill = (int) (28 + wave * 72);
        int aBorder = (int) (90 + wave * 130);
        int w = 16;
        int h = 16;
        int fillArgb = (aFill << 24) | (r << 16) | (g << 8) | b;
        context.fill(itemX, itemY, itemX + w, itemY + h, fillArgb);
        int borderArgb = (aBorder << 24) | (r << 16) | (g << 8) | b;
        context.fill(itemX, itemY, itemX + w, itemY + 1, borderArgb);
        context.fill(itemX, itemY + h - 1, itemX + w, itemY + h, borderArgb);
        context.fill(itemX, itemY, itemX + 1, itemY + h, borderArgb);
        context.fill(itemX + w - 1, itemY, itemX + w, itemY + h, borderArgb);
    }

    /**
     * Обводка слота левой руки в HUD (когда открыт только чат или без экрана).
     */
    public static void renderHudOverlays(DrawContext context, RenderTickCounter tickCounter) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.eatHelperEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        if (client.currentScreen != null && !(client.currentScreen instanceof ChatScreen)) {
            return;
        }
        if (!HIGHLIGHT.contains(PlayerInventory.OFF_HAND_SLOT)) {
            return;
        }
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int x = sw / 2 - 91 - 26;
        int y = sh - 22;
        drawSlotHighlight(context, x, y);
    }

    private static float score(ItemStack stack, float needH, float needF) {
        if (stack.isEmpty()) {
            return -1.0f;
        }
        float heal = healingWeight(stack);
        float food = foodWeight(stack);
        if (heal < 0.0f && food < 0.0f) {
            return -1.0f;
        }
        float hPart = heal < 0.0f ? 0.0f : heal;
        float fPart = food < 0.0f ? 0.0f : food;
        return needH * hPart + needF * fPart;
    }

    private static float healingWeight(ItemStack stack) {
        int amp = maxInstantHealthAmplifier(stack);
        if (amp < 0) {
            return -1.0f;
        }
        float healHp = amp == 0 ? 4.0f : 8.0f;
        return healHp * 2.75f;
    }

    private static float foodWeight(ItemStack stack) {
        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        if (food == null) {
            return -1.0f;
        }
        return food.nutrition() + food.saturation() * 1.5f;
    }

    /**
     * Максимальный подходящий уровень мгновенного исцеления (0 = I, 1 = II), иначе -1.
     */
    private static int maxInstantHealthAmplifier(ItemStack stack) {
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null || !contents.hasEffects()) {
            return -1;
        }
        int found = -1;
        for (StatusEffectInstance inst : contents.getEffects()) {
            if (!inst.getEffectType().equals(StatusEffects.INSTANT_HEALTH)) {
                continue;
            }
            int amp = inst.getAmplifier();
            if (amp >= 0 && amp <= 1) {
                found = Math.max(found, amp);
            }
        }
        return found;
    }
}
