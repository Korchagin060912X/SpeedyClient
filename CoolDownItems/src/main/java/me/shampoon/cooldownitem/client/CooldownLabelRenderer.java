/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.entity.player.ItemCooldownManager
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 */
package me.shampoon.cooldownitem.client;

import java.util.Locale;
import me.shampoon.cooldownitem.CooldownItemConfig;
import me.shampoon.cooldownitem.mixin.CooldownEntryAccessor;
import me.shampoon.cooldownitem.mixin.ItemCooldownManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class CooldownLabelRenderer {
    private CooldownLabelRenderer() {
    }

    public static void render(DrawContext context, ItemStack stack, int itemX, int itemY, PlayerEntity player) {
        if (!CooldownItemConfig.isFeatureEnabled() || stack.isEmpty() || player == null) {
            return;
        }
        if (CooldownItemConfig.get().isHudListMode()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        ItemCooldownManager cooldownManager = player.getItemCooldownManager();
        if (!cooldownManager.isCoolingDown(stack)) {
            return;
        }
        float cooldownRemaining = cooldownManager.getCooldownProgress(stack, tickDelta);
        if (cooldownRemaining <= 0.0f) {
            return;
        }
        ItemCooldownManagerAccessor managerAccessor = (ItemCooldownManagerAccessor)cooldownManager;
        Object entry = managerAccessor.cooldownitem$getEntries().get(cooldownManager.getGroup(stack));
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
        TextRenderer textRenderer = client.textRenderer;
        context.drawText(textRenderer, timerText, itemX + 1, itemY + 1, color, true);
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

