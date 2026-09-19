/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package me.shampoon.cooldownitem.client;

import java.util.Locale;
import me.shampoon.cooldownitem.CooldownItemConfig;
import me.shampoon.cooldownitem.mixin.CooldownEntryAccessor;
import me.shampoon.cooldownitem.mixin.ItemCooldownManagerAccessor;
import net.minecraft.class_1657;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class CooldownLabelRenderer {
    private CooldownLabelRenderer() {
    }

    public static void render(class_332 context, class_1799 stack, int itemX, int itemY, class_1657 player) {
        if (!CooldownItemConfig.isFeatureEnabled() || stack.method_7960() || player == null) {
            return;
        }
        if (CooldownItemConfig.get().isHudListMode()) {
            return;
        }
        class_310 client = class_310.method_1551();
        float tickDelta = client.method_61966().method_60637(false);
        class_1796 cooldownManager = player.method_7357();
        if (!cooldownManager.method_7904(stack)) {
            return;
        }
        float cooldownRemaining = cooldownManager.method_7905(stack, tickDelta);
        if (cooldownRemaining <= 0.0f) {
            return;
        }
        ItemCooldownManagerAccessor managerAccessor = (ItemCooldownManagerAccessor)cooldownManager;
        Object entry = managerAccessor.cooldownitem$getEntries().get(cooldownManager.method_62836(stack));
        if (entry == null) {
            return;
        }
        CooldownEntryAccessor entryAccessor = (CooldownEntryAccessor)entry;
        int startTick = entryAccessor.cooldownitem$getStartTick();
        int endTick = entryAccessor.cooldownitem$getEndTick();
        int currentTick = managerAccessor.cooldownitem$getTick();
        float totalTicks = Math.max(1.0f, (float)(endTick - startTick));
        float remainingTicks = Math.max(0.0f, (float)(endTick - currentTick) - tickDelta);
        float remainingRatio = Math.min(1.0f, remainingTicks / totalTicks);
        float remainingSeconds = remainingTicks / 20.0f;
        String timerText = remainingSeconds >= 10.0f ? Integer.toString(Math.round(remainingSeconds)) : String.format(Locale.ROOT, "%.1fs", Float.valueOf(remainingSeconds));
        int color = CooldownLabelRenderer.colorByRemainingRatio(remainingRatio);
        class_327 textRenderer = client.field_1772;
        context.method_51433(textRenderer, timerText, itemX + 1, itemY + 1, color, true);
    }

    private static int colorByRemainingRatio(float remainingRatio) {
        if (remainingRatio <= 0.33f) {
            return -11141291;
        }
        if (remainingRatio <= 0.66f) {
            return -171;
        }
        return -43691;
    }
}

