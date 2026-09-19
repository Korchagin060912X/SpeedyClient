/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EffectTimer {
    /** Отступ между блоками таймеров в классическом HUD. */
    public static final int CLASSIC_TIMER_GAP = 5;
    /** Макс. символов в названии эффекта перед «…» в классическом HUD. */
    public static final int CLASSIC_NAME_MAX_CHARS = 14;
    /** @deprecated шаг слота больше не фиксирован — оставлено для совместимости. */
    @Deprecated
    public static final int TIMER_SLOT_WIDTH = 20;
    /** Отступ правого края первого слота от края экрана (как у ванили ≈25, чуть меньше для компактности). */
    public static final int HUD_RIGHT_INSET = 22;
    /** Масштаб текста таймера (компактнее). */
    public static final float TIMER_TEXT_SCALE = 0.72f;

    private static final Map<String, Integer> peakDurationTicks = new HashMap<String, Integer>();
    private static final Set<String> activeEffectKeysThisFrame = new HashSet<String>();
    private static final Map<String, Integer> effectRecencyAge = new HashMap<>();
    private static final Map<String, Integer> effectLastDuration = new HashMap<>();
    private static final int COLOR_GREEN = -11141291;
    private static final int COLOR_YELLOW = -171;
    private static final int COLOR_RED = -43691;

    public static void beginHudEffectFrame() {
        activeEffectKeysThisFrame.clear();
    }

    public static void endHudEffectFrame() {
        peakDurationTicks.keySet().retainAll(activeEffectKeysThisFrame);
    }

    /** Ключ экземпляра эффекта для трекинга длительности и «новизны». */
    public static String effectKey(StatusEffectInstance effect) {
        return EffectTimer.peakKey(effect);
    }

    /**
     * Обновляет метку «когда эффект стал актуальнее всего» (новый или обновлённый баф).
     * Чем выше age игрока — тем новее эффект в списке панели.
     */
    public static void trackEffectRecency(ClientPlayerEntity player, Iterable<StatusEffectInstance> effects) {
        int age = player.age;
        Set<String> present = new HashSet<>();
        for (StatusEffectInstance e : effects) {
            if (!e.shouldShowIcon()) {
                continue;
            }
            String k = EffectTimer.peakKey(e);
            present.add(k);
            int d = e.getDuration();
            Integer prev = effectLastDuration.get(k);
            if (prev == null || d > prev + 20) {
                effectRecencyAge.put(k, age);
            }
            effectLastDuration.put(k, d);
        }
        effectRecencyAge.keySet().retainAll(present);
        effectLastDuration.keySet().retainAll(present);
    }

    public static int getEffectRecency(String effectKey) {
        return effectRecencyAge.getOrDefault(effectKey, 0);
    }

    /** Компактное время: {@code 4:39}, бесконечность — «∞». */
    public static String formatDurationCompact(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "\u221e";
        }
        int ticks = effect.getDuration();
        if (ticks <= 0) {
            return "0:00";
        }
        int totalSec = ticks / 20;
        int m = totalSec / 60;
        int s = totalSec % 60;
        return m + ":" + String.format("%02d", s);
    }

    private static String ellipsizeEffectName(String name, int maxChars) {
        if (name.length() <= maxChars) {
            return name;
        }
        if (maxChars <= 1) {
            return "\u2026";
        }
        return name.substring(0, maxChars - 1) + "\u2026";
    }

    /** Одна строка классического HUD: «Огнестойкость… - 4:39». */
    public static String buildClassicTimerLine(StatusEffectInstance effect) {
        String name = I18n.translate(effect.getEffectType().value().getTranslationKey());
        return EffectTimer.ellipsizeEffectName(name, EffectTimer.CLASSIC_NAME_MAX_CHARS)
                + " - "
                + EffectTimer.formatDurationCompact(effect);
    }

    public static int measureClassicTimerLineWidth(TextRenderer tr, StatusEffectInstance effect) {
        String line = EffectTimer.buildClassicTimerLine(effect);
        int tw = tr.getWidth(line);
        return Math.max(1, (int) Math.ceil((double) tw * (double) EffectTimer.TIMER_TEXT_SCALE)) + EffectTimer.CLASSIC_TIMER_GAP;
    }

    public static int measureClassicRowTotalWidth(TextRenderer tr, List<StatusEffectInstance> row) {
        int s = 0;
        for (StatusEffectInstance e : row) {
            s += EffectTimer.measureClassicTimerLineWidth(tr, e);
        }
        return s;
    }

    /** Оставшееся время словами (рус.); для пресетов/прочего. */
    public static String formatDurationWordsRu(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "\u0431\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\u043e";
        }
        int ticks = effect.getDuration();
        if (ticks <= 0) {
            return "0 \u0441\u0435\u043a\u0443\u043d\u0434";
        }
        int totalSec = ticks / 20;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        if (min <= 0) {
            return sec + " " + EffectTimer.ruSecondsWord(sec);
        }
        if (sec <= 0) {
            return EffectTimer.ruMinutesPhrase(min);
        }
        return EffectTimer.ruMinutesPhrase(min) + " " + sec + " " + EffectTimer.ruSecondsWord(sec);
    }

    private static String ruMinutesPhrase(int m) {
        return m + " " + EffectTimer.ruMinutesWord(m);
    }

    private static String ruMinutesWord(int m) {
        int mod100 = m % 100;
        if (mod100 >= 11 && mod100 <= 14) {
            return "\u043c\u0438\u043d\u0443\u0442";
        }
        return switch (m % 10) {
            case 1 -> "\u043c\u0438\u043d\u0443\u0442\u0430";
            case 2, 3, 4 -> "\u043c\u0438\u043d\u0443\u0442\u044b";
            default -> "\u043c\u0438\u043d\u0443\u0442";
        };
    }

    private static String ruSecondsWord(int s) {
        int mod100 = s % 100;
        if (mod100 >= 11 && mod100 <= 14) {
            return "\u0441\u0435\u043a\u0443\u043d\u0434";
        }
        return switch (s % 10) {
            case 1 -> "\u0441\u0435\u043a\u0443\u043d\u0434\u0430";
            case 2, 3, 4 -> "\u0441\u0435\u043a\u0443\u043d\u0434\u044b";
            default -> "\u0441\u0435\u043a\u0443\u043d\u0434";
        };
    }

    /**
     * Классический HUD: строка «имя… - M:SS», выровнена по правому краю {@code alignRightX};
     * @return занятая ширина (масштабированная + зазор), чтобы сдвинуть следующий таймер левее.
     */
    public static int renderEffectTimer(DrawContext context, StatusEffectInstance effect, int alignRightX, int y) {
        if (!PVPUtils.CONFIG.effectTimerEnabled) {
            return 0;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return 0;
        }
        String peakKey = EffectTimer.peakKey(effect);
        activeEffectKeysThisFrame.add(peakKey);
        TextRenderer textRenderer = client.textRenderer;
        int duration = effect.getDuration();
        String text = EffectTimer.buildClassicTimerLine(effect);
        int textWidth = textRenderer.getWidth(text);
        int scaledW = Math.max(1, (int) Math.ceil((double) textWidth * (double) EffectTimer.TIMER_TEXT_SCALE));
        int textX = alignRightX - scaledW;
        Objects.requireNonNull(textRenderer);
        int textY = y + 5;
        int color = EffectTimer.timerColor(effect, duration, peakKey);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) textX, (float) textY);
        context.getMatrices().scale(EffectTimer.TIMER_TEXT_SCALE, EffectTimer.TIMER_TEXT_SCALE);
        context.drawText(textRenderer, text, 0, 0, color, false);
        context.getMatrices().popMatrix();
        return scaledW + EffectTimer.CLASSIC_TIMER_GAP;
    }

    private static String peakKey(StatusEffectInstance effect) {
        Identifier id = Registries.STATUS_EFFECT.getId((StatusEffect)effect.getEffectType().value());
        String idStr = id != null ? id.toString() : "unknown";
        return idStr + ":" + effect.getAmplifier() + ":" + effect.isAmbient();
    }

    private static int timerColor(StatusEffectInstance effect, int durationTicks, String peakKey) {
        if (effect.isInfinite()) {
            return -11141291;
        }
        if (durationTicks <= 0) {
            return -43691;
        }
        peakDurationTicks.merge(peakKey, durationTicks, Math::max);
        int peak = peakDurationTicks.getOrDefault(peakKey, durationTicks);
        if (peak <= 0) {
            return -11141291;
        }
        float remaining = (float)durationTicks / (float)peak;
        float elapsed = 1.0f - remaining;
        if (elapsed >= 0.99f) {
            return -43691;
        }
        if (remaining > 0.6666667f) {
            return -11141291;
        }
        if (remaining > 0.33333334f) {
            return -171;
        }
        return -43691;
    }
}

