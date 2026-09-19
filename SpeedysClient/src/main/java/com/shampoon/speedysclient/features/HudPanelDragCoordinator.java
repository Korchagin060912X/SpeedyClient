package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import me.shampoon.cooldownitem.CooldownItemConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;

/**
 * Один активный захват на кадр: панели «Зелья» и «КД» не двигаются одновременно; при перекрытии приоритет у КД.
 */
public final class HudPanelDragCoordinator {
    private enum Active {
        NONE,
        COOLDOWN,
        EFFECT,
        NOTIFY
    }

    private static Active active = Active.NONE;
    private static int grabDx;
    private static int grabDy;

    private static boolean cdValid;
    private static int cdDefL;
    private static int cdDefT;
    private static int cdPL;
    private static int cdPT;
    private static int cdW;
    private static int cdH;

    private static boolean fxValid;
    private static int fxDefL;
    private static int fxDefT;
    private static int fxPL;
    private static int fxPT;
    private static int fxW;
    private static int fxH;

    private static boolean ntValid;
    private static int ntDefL;
    private static int ntDefT;
    private static int ntPL;
    private static int ntPT;
    private static int ntW;
    private static int ntH;

    private HudPanelDragCoordinator() {
    }

    public static void resetCooldownPanel() {
        cdValid = false;
    }

    public static void resetEffectPanel() {
        fxValid = false;
    }

    public static void resetNotifyPanel() {
        ntValid = false;
    }

    public static void publishCooldown(int defLeft, int defTop, int panelLeft, int panelTop, int panelW, int panelH) {
        cdValid = true;
        cdDefL = defLeft;
        cdDefT = defTop;
        cdPL = panelLeft;
        cdPT = panelTop;
        cdW = panelW;
        cdH = panelH;
    }

    public static void publishEffect(int defLeft, int defTop, int panelLeft, int panelTop, int panelW, int panelH) {
        fxValid = true;
        fxDefL = defLeft;
        fxDefT = defTop;
        fxPL = panelLeft;
        fxPT = panelTop;
        fxW = panelW;
        fxH = panelH;
    }

    public static void publishNotify(int defLeft, int defTop, int panelLeft, int panelTop, int panelW, int panelH) {
        ntValid = true;
        ntDefL = defLeft;
        ntDefT = defTop;
        ntPL = panelLeft;
        ntPT = panelTop;
        ntW = panelW;
        ntH = panelH;
    }

    public static void endFrame(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof ChatScreen)) {
            finishActiveDrag();
            active = Active.NONE;
            return;
        }
        if (active == Active.COOLDOWN && !cdValid) {
            CooldownItemConfig.save();
            active = Active.NONE;
        } else if (active == Active.EFFECT && !fxValid) {
            if (PVPUtils.CONFIG != null) {
                PVPUtils.CONFIG.save();
            }
            active = Active.NONE;
        } else if (active == Active.NOTIFY && !ntValid) {
            if (PVPUtils.CONFIG != null) {
                PVPUtils.CONFIG.save();
            }
            active = Active.NONE;
        }

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int fbw = Math.max(1, client.getWindow().getFramebufferWidth());
        int fbh = Math.max(1, client.getWindow().getFramebufferHeight());
        int mx = (int) (client.mouse.getX() * (double) sw / (double) fbw);
        int my = (int) (client.mouse.getY() * (double) sh / (double) fbh);
        boolean lmb = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        boolean inCd = cdValid && mx >= cdPL && mx <= cdPL + cdW && my >= cdPT && my <= cdPT + cdH;
        boolean inFx = fxValid && mx >= fxPL && mx <= fxPL + fxW && my >= fxPT && my <= fxPT + fxH;
        boolean inNt = ntValid && mx >= ntPL && mx <= ntPL + ntW && my >= ntPT && my <= ntPT + ntH;

        if (!lmb) {
            finishActiveDrag();
            active = Active.NONE;
            return;
        }

        if (active == Active.NONE) {
            if (inCd) {
                active = Active.COOLDOWN;
                grabDx = mx - cdPL;
                grabDy = my - cdPT;
            } else if (inFx) {
                active = Active.EFFECT;
                grabDx = mx - fxPL;
                grabDy = my - fxPT;
            } else if (inNt && PVPUtils.CONFIG != null) {
                active = Active.NOTIFY;
                grabDx = mx - ntPL;
                grabDy = my - ntPT;
            }
        }

        if (active == Active.COOLDOWN && cdValid) {
            int newL = mx - grabDx;
            int newT = my - grabDy;
            CooldownItemConfig.get().hudPanelOffsetX = clamp(newL - cdDefL, -4000, 4000);
            CooldownItemConfig.get().hudPanelOffsetY = clamp(newT - cdDefT, -4000, 4000);
        } else if (active == Active.EFFECT && fxValid && PVPUtils.CONFIG != null) {
            int newL = mx - grabDx;
            int newT = my - grabDy;
            PVPUtils.CONFIG.effectTimerHudOffsetX = clamp(newL - fxDefL, -4000, 4000);
            PVPUtils.CONFIG.effectTimerHudOffsetY = clamp(newT - fxDefT, -4000, 4000);
        } else if (active == Active.NOTIFY && ntValid && PVPUtils.CONFIG != null) {
            int newL = mx - grabDx;
            int newT = my - grabDy;
            PVPUtils.CONFIG.miniHudNotifyOffsetX = clamp(newL - ntDefL, -4000, 4000);
            PVPUtils.CONFIG.miniHudNotifyOffsetY = clamp(newT - ntDefT, -4000, 4000);
        }
    }

    private static void finishActiveDrag() {
        if (active == Active.COOLDOWN) {
            CooldownItemConfig.save();
        } else if (active == Active.EFFECT && PVPUtils.CONFIG != null) {
            PVPUtils.CONFIG.save();
        } else if (active == Active.NOTIFY && PVPUtils.CONFIG != null) {
            PVPUtils.CONFIG.save();
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
