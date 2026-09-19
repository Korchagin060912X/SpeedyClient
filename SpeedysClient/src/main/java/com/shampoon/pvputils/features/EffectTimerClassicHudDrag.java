package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.lwjgl.glfw.GLFW;

/** Перетаскивание смещения таймеров у иконок (классика) при открытом чате. */
public final class EffectTimerClassicHudDrag {
    private static boolean dragging;
    private static int grabOx;
    private static int grabOy;
    private static int startMx;
    private static int startMy;

    private EffectTimerClassicHudDrag() {
    }

    public static void tickDrag(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (PVPUtils.CONFIG == null
                || !PVPUtils.CONFIG.effectTimerEnabled
                || PVPUtils.CONFIG.effectTimerListPanel) {
            if (!(client.currentScreen instanceof ChatScreen)) {
                dragging = false;
            }
            return;
        }
        if (!(client.currentScreen instanceof ChatScreen)) {
            if (dragging) {
                PVPUtils.CONFIG.save();
            }
            dragging = false;
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        ArrayList<StatusEffectInstance> beneficial = new ArrayList<>();
        ArrayList<StatusEffectInstance> other = new ArrayList<>();
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            if (!effect.shouldShowIcon()) {
                continue;
            }
            if (effect.getEffectType().value().isBeneficial()) {
                beneficial.add(effect);
            } else {
                other.add(effect);
            }
        }
        if (beneficial.isEmpty() && other.isEmpty()) {
            return;
        }

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int ox = PVPUtils.CONFIG.effectTimerHudOffsetX;
        int oy = PVPUtils.CONFIG.effectTimerHudOffsetY;
        int rightOrigin = sw - EffectTimer.HUD_RIGHT_INSET + ox;
        int n1 = beneficial.size();
        int n2 = other.size();
        int wRow = 0;
        if (n1 > 0 && n2 > 0) {
            wRow = Math.max(
                    EffectTimer.measureClassicRowTotalWidth(client.textRenderer, beneficial),
                    EffectTimer.measureClassicRowTotalWidth(client.textRenderer, other));
        } else if (n1 > 0) {
            wRow = EffectTimer.measureClassicRowTotalWidth(client.textRenderer, beneficial);
        } else {
            wRow = EffectTimer.measureClassicRowTotalWidth(client.textRenderer, other);
        }
        int left = rightOrigin - wRow;
        int top;
        int bottom;
        if (n1 > 0 && n2 > 0) {
            top = 1 + oy;
            bottom = 51 + oy;
        } else if (n1 > 0) {
            top = 1 + oy;
            bottom = 26 + oy;
        } else {
            top = 26 + oy;
            bottom = 51 + oy;
        }
        int right = rightOrigin + 2;

        int mx = (int)(client.mouse.getX() * (double)sw / (double)Math.max(1, client.getWindow().getFramebufferWidth()));
        int my = (int)(client.mouse.getY() * (double)sh / (double)Math.max(1, client.getWindow().getFramebufferHeight()));
        boolean lmb = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean inside = mx >= left && mx <= right && my >= top && my <= bottom;

        if (lmb) {
            if (!dragging && inside) {
                dragging = true;
                grabOx = ox;
                grabOy = oy;
                startMx = mx;
                startMy = my;
            }
            if (dragging) {
                PVPUtils.CONFIG.effectTimerHudOffsetX = EffectTimerClassicHudDrag.clampInt(grabOx + mx - startMx, -4000, 4000);
                PVPUtils.CONFIG.effectTimerHudOffsetY = EffectTimerClassicHudDrag.clampInt(grabOy + my - startMy, -4000, 4000);
            }
        } else if (dragging) {
            dragging = false;
            PVPUtils.CONFIG.save();
        }
    }

    private static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
