package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.EffectTimer;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

/** Панель списка эффектов в стиле Speedys; перетаскивание при открытом чате, позиция в {@link PVPUtils#CONFIG}. */
public final class EffectTimerListHud {
    private static final int MARGIN = 5;
    private static final int HEADER_ICON = 8;
    private static final int EFFECT_ICON = 10;
    private static final int ROW_H = 14;
    private static final int PAD = 3;
    private static final int BG = -804253158;
    private static final int OUTLINE = -14534042;
    private static final int COLOR_NAME = 0xFFFFFFFF;
    private static final int COLOR_LVL = 0xFFC04055;
    private static final int COLOR_TIME = 0xFF9EB7FF;
    private static final int SEPARATOR = 0x60000000;

    private static final Identifier HEADER_TEX = Identifier.ofVanilla("textures/item/potion.png");

    private EffectTimerListHud() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        HudPanelDragCoordinator.resetEffectPanel();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || PVPUtils.CONFIG == null
                || !PVPUtils.CONFIG.effectTimerEnabled || !PVPUtils.CONFIG.effectTimerListPanel) {
            return;
        }
        ClientPlayerEntity player = client.player;
        ArrayList<StatusEffectInstance> list = new ArrayList<>();
        for (StatusEffectInstance e : player.getStatusEffects()) {
            if (e.shouldShowIcon()) {
                list.add(e);
            }
        }
        if (list.isEmpty()) {
            return;
        }
        EffectTimer.trackEffectRecency(player, list);
        list.sort(Comparator.comparingInt((StatusEffectInstance e) -> EffectTimer.getEffectRecency(EffectTimer.effectKey(e)))
                .reversed()
                .thenComparing(EffectTimer::effectKey));

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        var tr = client.textRenderer;
        String headerTitle = "\u0417\u0435\u043b\u044c\u044f";
        int headerH = 17;
        int maxInner = tr.getWidth(headerTitle) + HEADER_ICON + PAD * 3;
        for (int i = 0; i < list.size(); i++) {
            StatusEffectInstance e = list.get(i);
            String prefix = (i + 1) + ". ";
            String name = I18n.translate(e.getEffectType().value().getTranslationKey());
            int lvlAmp = e.getAmplifier() + 1;
            String lvl = "LVL " + lvlAmp;
            String time = EffectTimer.formatDurationCompact(e);
            int leftBlock = tr.getWidth(prefix) + EFFECT_ICON + PAD + tr.getWidth(name) + tr.getWidth(" ") + tr.getWidth(lvl);
            int rowW = leftBlock + PAD + tr.getWidth(time);
            maxInner = Math.max(maxInner, rowW);
        }
        int panelW = maxInner + PAD * 2;
        int panelH = headerH + 2 + list.size() * ROW_H + PAD;

        int defaultLeft = sw - panelW - MARGIN;
        int defaultTop = MARGIN;
        int ox = PVPUtils.CONFIG.effectTimerHudOffsetX;
        int oy = PVPUtils.CONFIG.effectTimerHudOffsetY;
        int panelLeft = defaultLeft + ox;
        int panelTop = defaultTop + oy;
        panelLeft = Math.max(4, Math.min(panelLeft, sw - panelW - 4));
        panelTop = Math.max(4, Math.min(panelTop, sh - panelH - 4));

        HudPanelDragCoordinator.publishEffect(defaultLeft, defaultTop, panelLeft, panelTop, panelW, panelH);

        EffectTimerListHud.drawRoundedRect(context, panelLeft, panelTop, panelLeft + panelW, panelTop + panelH, BG);
        EffectTimerListHud.drawRoundedOutline(context, panelLeft, panelTop, panelLeft + panelW, panelTop + panelH, OUTLINE);

        int hx = panelLeft + PAD;
        int hy = panelTop + (headerH - 9) / 2;
        int iconY = panelTop + (headerH - HEADER_ICON) / 2;
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                HEADER_TEX,
                hx,
                iconY,
                0.0f,
                0.0f,
                HEADER_ICON,
                HEADER_ICON,
                16,
                16);
        context.drawText(tr, headerTitle, hx + HEADER_ICON + PAD, hy, COLOR_NAME, false);
        int sepY = panelTop + headerH;
        context.fill(panelLeft + PAD, sepY, panelLeft + panelW - PAD, sepY + 1, SEPARATOR);

        int rowTop = sepY + 3;
        for (int i = 0; i < list.size(); i++) {
            StatusEffectInstance e = list.get(i);
            int ry = rowTop + i * ROW_H;
            int cy = ry + (ROW_H - 9) / 2;
            String prefix = (i + 1) + ". ";
            context.drawText(tr, prefix, panelLeft + PAD, cy, COLOR_NAME, false);
            int pix = panelLeft + PAD + tr.getWidth(prefix);
            Identifier fxTex = InGameHud.getEffectTexture(e.getEffectType());
            int iy = ry + (ROW_H - EFFECT_ICON) / 2;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, fxTex, pix, iy, EFFECT_ICON, EFFECT_ICON, 0xFFFFFFFF);
            int tx = pix + EFFECT_ICON + PAD;
            String name = I18n.translate(e.getEffectType().value().getTranslationKey());
            context.drawText(tr, name, tx, cy, COLOR_NAME, false);
            int lvlAmp = e.getAmplifier() + 1;
            String lvl = "LVL " + lvlAmp;
            context.drawText(tr, lvl, tx + tr.getWidth(name) + tr.getWidth(" "), cy, COLOR_LVL, false);
            String time = EffectTimer.formatDurationCompact(e);
            int tw = tr.getWidth(time);
            context.drawText(tr, time, panelLeft + panelW - PAD - tw, cy, COLOR_TIME, false);
        }
    }

    private static void drawRoundedRect(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, b, color);
        context.fill(l, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 3, color);
        context.fill(r - 3, t + 1, r - 1, t + 3, color);
        context.fill(l + 1, b - 3, l + 3, b - 1, color);
        context.fill(r - 3, b - 3, r - 1, b - 1, color);
    }

    private static void drawRoundedOutline(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, t + 1, color);
        context.fill(l + 3, b - 1, r - 3, b, color);
        context.fill(l, t + 3, l + 1, b - 3, color);
        context.fill(r - 1, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 2, color);
        context.fill(r - 3, t + 1, r - 1, t + 2, color);
        context.fill(l + 1, b - 2, l + 3, b - 1, color);
        context.fill(r - 3, b - 2, r - 1, b - 1, color);
    }
}
