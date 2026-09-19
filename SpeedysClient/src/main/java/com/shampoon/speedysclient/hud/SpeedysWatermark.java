/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gl.RenderPipelines
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.MathHelper
 *  org.lwjgl.glfw.GLFW
 */
package com.shampoon.speedysclient.hud;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.ArmorHelper;
import com.shampoon.speedysclient.config.SpeedysWatermarkConfig;
import com.shampoon.speedysclient.ui.WatermarkModeScreen;
import io.github.musicintegration.gsmtc.GsmtcService;
import io.github.musicintegration.gsmtc.MediaState;
import io.github.musicintegration.platform.WindowsSupport;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class SpeedysWatermark {
    public static final Identifier BOOT_ICON = Identifier.of((String)"speedysclient", (String)"textures/gui/boot_icon.png");
    private static final double EES_RADAR_RANGE = 150.0;
    private static final int PILL_PAD_X = 10;
    private static final int PILL_PAD_Y = 5;
    private static final int ICON_SIZE = 12;
    private static final int GAP = 6;
    private static final int BG = -805306368;
    private static final int OUTLINE = -14996918;
    private static final int TEXT = -1;
    private static boolean wasRightDown;
    private static int lastBarLeft;
    private static int lastBarTop;
    private static int lastBarRight;
    private static int lastBarBottom;
    private static boolean dragging;
    private static int dragGrabDx;
    private static int dragGrabDy;

    private SpeedysWatermark() {
    }

    public static void tick(MinecraftClient client) {
        boolean inGameHud;
        if (client.options.hudHidden || client.player == null || !SpeedysWatermarkConfig.get().visible) {
            return;
        }
        boolean chatCursor = client.currentScreen instanceof ChatScreen;
        double mx = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)Math.max(1, client.getWindow().getFramebufferWidth());
        double my = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)Math.max(1, client.getWindow().getFramebufferHeight());
        int imx = (int)mx;
        int imy = (int)my;
        if (chatCursor && GLFW.glfwGetMouseButton((long)client.getWindow().getHandle(), (int)0) == 1) {
            if (!dragging && imx >= lastBarLeft && imx <= lastBarRight && imy >= lastBarTop && imy <= lastBarBottom) {
                dragging = true;
                dragGrabDx = imx - (lastBarLeft + lastBarRight) / 2;
                dragGrabDy = imy - lastBarTop;
            }
            if (dragging) {
                SpeedysWatermarkConfig c = SpeedysWatermarkConfig.get();
                int sw = client.getWindow().getScaledWidth();
                int cx = sw / 2;
                c.offsetX = imx - dragGrabDx - cx;
                c.offsetY = imy - dragGrabDy;
                c.offsetY = MathHelper.clamp((int)c.offsetY, (int)2, (int)(client.getWindow().getScaledHeight() - 24));
            }
        } else {
            if (dragging) {
                SpeedysWatermarkConfig.save();
            }
            dragging = false;
        }
        boolean rightDownNow = GLFW.glfwGetMouseButton((long)client.getWindow().getHandle(), (int)1) == 1;
        boolean rightClick = rightDownNow && !wasRightDown;
        wasRightDown = rightDownNow;
        boolean bl = inGameHud = client.currentScreen == null || client.currentScreen instanceof ChatScreen;
        if (inGameHud && rightClick && imx >= lastBarLeft && imx <= lastBarRight && imy >= lastBarTop && imy <= lastBarBottom) {
            client.setScreen((Screen)new WatermarkModeScreen(client.currentScreen));
        }
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null || !SpeedysWatermarkConfig.get().visible) {
            return;
        }
        TextRenderer tr = client.textRenderer;
        SpeedysWatermarkConfig cfg = SpeedysWatermarkConfig.get();
        int sw = context.getScaledWindowWidth();
        String brand = "Speedys Client";
        String sep = " / ";
        String tail = switch (cfg.mode) {
            default -> throw new MatchException(null, null);
            case SpeedysWatermarkConfig.DisplayMode.FPS_MS -> SpeedysWatermark.fpsAndPing(client);
            case SpeedysWatermarkConfig.DisplayMode.MUSIC -> SpeedysWatermark.musicLine();
            case SpeedysWatermarkConfig.DisplayMode.ARMOR -> SpeedysWatermark.armorLine(client);
            case SpeedysWatermarkConfig.DisplayMode.EES_RADAR -> SpeedysWatermark.eesRadarLine(client);
        };
        int centerX = sw / 2 + cfg.offsetX;
        int y = cfg.offsetY;
        int iconTotal = 18;
        int wBrand = tr.getWidth(brand);
        int wSep1 = tr.getWidth(sep);
        int wTail = tr.getWidth(SpeedysWatermark.trimTail(tr, tail, sw - 40));
        tail = SpeedysWatermark.trimTail(tr, tail, sw - 40);
        int innerW = iconTotal + wBrand + wSep1 + wTail;
        int barW = innerW + 20;
        Objects.requireNonNull(tr);
        int barH = Math.max(12, 9) + 10;
        int left = centerX - barW / 2;
        int top = y;
        int right = left + barW;
        int bottom = top + barH;
        lastBarLeft = left;
        lastBarTop = top;
        lastBarRight = right;
        lastBarBottom = bottom;
        SpeedysWatermark.drawRoundedRect(context, left, top, right, bottom, -805306368);
        SpeedysWatermark.drawRoundedOutline(context, left, top, right, bottom, -14996918);
        Objects.requireNonNull(tr);
        int textY = top + (barH - 9) / 2;
        int x = left + 10;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BOOT_ICON, x, top + (barH - 12) / 2, 0.0f, 0.0f, 12, 12, 12, 12);
        context.drawText(tr, brand, x += iconTotal, textY, -1, false);
        context.drawText(tr, sep, x += wBrand, textY, -1, false);
        context.drawText(tr, tail, x += wSep1, textY, -1, false);
    }

    private static String trimTail(TextRenderer tr, String tail, int maxPx) {
        if (tr.getWidth(tail) <= maxPx) {
            return tail;
        }
        String ell = "\u2026";
        int ew = tr.getWidth(ell);
        for (int i = tail.length() - 1; i > 0; --i) {
            String s = tail.substring(0, i) + ell;
            if (tr.getWidth(s) > maxPx) continue;
            return s;
        }
        return ell;
    }

    private static String fpsAndPing(MinecraftClient client) {
        int fps = client.getCurrentFps();
        int ms = 0;
        if (client.getNetworkHandler() != null && client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()) != null) {
            ms = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()).getLatency();
        }
        return ms + " ms" + SpeedysWatermark.sepSlash() + fps + " FPS";
    }

    private static String sepSlash() {
        return " / ";
    }

    private static String musicLine() {
        String a;
        if (!WindowsSupport.isWindows() || !WindowsSupport.isGsmtcSupportedOs()) {
            return "Music (Windows)";
        }
        MediaState m = GsmtcService.getLatest();
        if (!m.hasMedia()) {
            return "\u041d\u0435\u0442 \u0442\u0440\u0435\u043a\u0430";
        }
        String t = m.title != null ? m.title : "";
        String string = a = m.artist != null ? m.artist : "";
        if (t.isEmpty() && a.isEmpty()) {
            return "\u2014";
        }
        if (a.isEmpty()) {
            return t;
        }
        return t + " \u2014 " + a;
    }

    private static String armorLine(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.armorHelperEnabled) {
            return "Armor Helper OFF";
        }
        return ArmorHelper.getWatermarkSummary((PlayerEntity)client.player);
    }

    private static String eesRadarLine(MinecraftClient client) {
        ClientPlayerEntity self = client.player;
        if (self == null || client.world == null) {
            return "\u2014";
        }
        PlayerEntity nearest = null;
        double bestSq = 22500.0;
        for (PlayerEntity other : client.world.getPlayers()) {
            double d;
            if (other == self || !((d = self.squaredDistanceTo((Entity)other)) < bestSq)) continue;
            bestSq = d;
            nearest = other;
        }
        if (nearest == null) {
            return "\u043d\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u043e\u0432";
        }
        double blocks = Math.sqrt(bestSq);
        String word = SpeedysWatermark.blockWordRu(blocks);
        return nearest.getGameProfile().getName() + " " + String.format(Locale.US, "%.1f ", blocks) + word;
    }

    private static String blockWordRu(double blocks) {
        if (!Double.isFinite(blocks) || blocks <= 0.0) {
            return "\u0431\u043b\u043e\u043a\u043e\u0432";
        }
        if (blocks < 1.0) {
            return "\u0431\u043b\u043e\u043a\u0430";
        }
        int n = (int)Math.floor(blocks + 1.0E-9);
        double frac = blocks - (double)n;
        if (frac > 1.0E-6) {
            return "\u0431\u043b\u043e\u043a\u0430";
        }
        if (n == 1) {
            return "\u0431\u043b\u043e\u043a";
        }
        int mod100 = n % 100;
        if (mod100 >= 11 && mod100 <= 14) {
            return "\u0431\u043b\u043e\u043a\u043e\u0432";
        }
        return switch (n % 10) {
            case 1 -> "\u0431\u043b\u043e\u043a";
            case 2, 3, 4 -> "\u0431\u043b\u043e\u043a\u0430";
            default -> "\u0431\u043b\u043e\u043a\u043e\u0432";
        };
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

