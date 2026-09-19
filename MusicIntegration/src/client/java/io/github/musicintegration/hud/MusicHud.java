package io.github.musicintegration.hud;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.musicintegration.config.ModConfig;
import io.github.musicintegration.gsmtc.GsmtcService;
import io.github.musicintegration.gsmtc.MediaState;
import io.github.musicintegration.platform.WindowsSupport;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.io.ByteArrayInputStream;
import java.util.Base64;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public final class MusicHud {

    // colours
    private static final int BG        = 0xC0111318;
    private static final int BORDER_LT = 0xFF606060;
    private static final int BORDER_RB = 0xFF282828;
    private static final int COL_TITLE = 0xFFFFFFFF;
    private static final int COL_SUB   = 0xFFAAAAAA;
    private static final int BAR_BG    = 0xFF3A3A3A;
    private static final int BAR_FG    = 0xFFFFFFFF;
    private static final int BAR_SEEK  = 0xFF1DB954;
    private static final int VIZ_ON    = 0xFF1DB954;
    private static final int VIZ_OFF   = 0xFF444444;

    // layout
    private static final int PAD     = 6;
    private static final int THUMB    = 32;   // display size px (reduced for performance)
    private static final int THUMB_TEX = 64;  // texture size (power of 2)
    private static final int BTN_W   = 18;
    private static final int BTN_H   = 12;
    private static final int BAR_H   = 3;
    private static final int VIZ_N   = 10;
    private static final int VIZ_BW  = 2;
    private static final int VIZ_GAP = 1;
    private static final int VIZ_MH  = 12;  // max bar height

    // animation: 0=compact, 1=expanded
    private static float anim = 0f;

    // visualizer
    private static final float[] vizH   = new float[VIZ_N];
    private static final float[] vizTgt = new float[VIZ_N];
    private static long vizNext = 0;

    // thumbnail — loaded on render thread, flagged dirty from any thread
    private static volatile String  pendingThumbB64 = null;  // set from bg thread
    private static String           cachedKey = "";
    private static DynamicTexture   thumbTex  = null;
    private static ResourceLocation thumbRL   = null;
    private static int[] thumbPixels = null;  // Cached ARGB pixels for fast rendering
    private static final ResourceLocation THUMB_RL =
            ResourceLocation.fromNamespaceAndPath("musicintegration", "album_art");

    // hit areas
    private static int     btnPrevX, btnPrevY, btnPlayX, btnPlayY, btnNextX, btnNextY;
    private static int     barL, barT, barR;
    private static boolean btnsActive = false;
    private static boolean barActive  = false;

    // click state
    private static boolean prevDown = false;

    private MusicHud() {}

    /** Call once at mod init to register screen click hooks */
    public static void registerScreenClicks() {
        ScreenEvents.AFTER_INIT.register((mc, screen, sw, sh) -> {
            ScreenMouseEvents.afterMouseClick(screen).register((s, mx, my, btn) -> {
                if (btn != 0) return;
                // No anim check — buttons should always be clickable when screen is open
                io.github.musicintegration.MusicIntegrationClient.LOGGER
                    .info("[MusicIntegration] click at {},{} btns={},{} play={},{} next={},{} anim={}",
                        (int)mx, (int)my, btnPrevX, btnPrevY, btnPlayX, btnPlayY, btnNextX, btnNextY,
                        String.format("%.2f", anim));
                handleClick(mx, my);
            });
        });
    }

    private static void handleClick(double mx, double my) {
        Minecraft mc = Minecraft.getInstance();
        if (hitBtn(mx, my, btnPrevX, btnPrevY)) {
            io.github.musicintegration.MusicIntegrationClient.LOGGER.info("[MI] -> PREV");
            GsmtcService.skipPrevious(); return;
        }
        if (hitBtn(mx, my, btnPlayX, btnPlayY)) {
            io.github.musicintegration.MusicIntegrationClient.LOGGER.info("[MI] -> PLAY_PAUSE");
            GsmtcService.togglePlayPause(); return;
        }
        if (hitBtn(mx, my, btnNextX, btnNextY)) {
            io.github.musicintegration.MusicIntegrationClient.LOGGER.info("[MI] -> NEXT");
            GsmtcService.skipNext(); return;
        }
        if (barActive && hitBar(mx, my)) {
            MediaState m = GsmtcService.getLatest();
            if (m.durationMs > 0) {
                float f = Mth.clamp((float)(mx - barL) / (barR - barL), 0f, 1f);
                long seekMs = (long)(f * m.durationMs);
                io.github.musicintegration.MusicIntegrationClient.LOGGER.info("[MI] -> SEEK {}", seekMs);
                GsmtcService.seek(seekMs);
            }
        }
    }

    // ── TICK ─────────────────────────────────────────────────────────────────
    public static void tick(Minecraft mc) {
        if (!WindowsSupport.isWindows()) return;
        // visualizer driven by real audio peak
        long now = System.currentTimeMillis();
        if (now >= vizNext) {
            vizNext = now + 50; // update 20x/sec
            MediaState m = GsmtcService.getLatest();
            float peak = m.peak;
            // Don't zero out peak based on state — Python already handles this
            for (int i = 0; i < VIZ_N; i++) {
                float noise = (float)(Math.random() * 0.3 - 0.15);
                vizTgt[i] = Math.max(0.02f, Math.min(1f, peak + noise));
            }
        }
        for (int i = 0; i < VIZ_N; i++)
            vizH[i] += (vizTgt[i] - vizH[i]) * 0.3f;
    }

    // ── RENDER ───────────────────────────────────────────────────────────────
    public static void render(GuiGraphics gfx, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (!shouldRender(mc)) { reset(); return; }
        if (!WindowsSupport.isGsmtcSupportedOs()) { reset(); return; }

        MediaState media = GsmtcService.getLatest();
        if (!media.hasMedia()) { reset(); return; }

        // animate expand: only when a screen is open
        if (mc.screen != null) {
            anim += (1f - anim) * 0.2f;
            if (anim > 0.98f) anim = 1f;
        } else {
            anim = 0f; // instant close when no screen
        }

        Font font = mc.font;
        int sw    = mc.getWindow().getGuiScaledWidth();
        int yTop  = ModConfig.get().hudYOffset;
        double mx = smx(mc), my = smy(mc);

        loadThumb(mc, media.thumbBase64);

        String title  = trim(font, media.title,  ModConfig.get().maxTitleWidth);
        String artist = trim(font, media.artist, ModConfig.get().maxTitleWidth);
        int textW = Math.max(Math.max(font.width(title), font.width(artist)), 80);

        int vizTotalW = VIZ_N * (VIZ_BW + VIZ_GAP) - VIZ_GAP;

        // compact panel: [PAD][THUMB][PAD][text][PAD][viz][PAD]
        int cW = PAD + THUMB + PAD + textW + PAD + vizTotalW + PAD;
        int cH = THUMB + PAD * 2;

        // expanded rows height (animated)
        int expandH = BAR_H + 4          // bar + gap
                    + font.lineHeight + 3 // time
                    + BTN_H + PAD;        // buttons

        int pH = cH + (int)(expandH * anim);
        int pL = (sw - cW) / 2;
        int pT = yTop;
        int pR = pL + cW;
        int pB = pT + pH;

        drawPanel(gfx, pL, pT, pR, pB);

        // album art
        int tX = pL + PAD, tY = pT + PAD;
        if (thumbPixels != null) {
            // Draw cached pixels line by line for better performance
            for (int y = 0; y < THUMB; y++) {
                int rowStart = y * THUMB;
                for (int x = 0; x < THUMB; x++) {
                    int argb = thumbPixels[rowStart + x];
                    // Draw horizontal line of same color pixels together
                    int x2 = x + 1;
                    while (x2 < THUMB && thumbPixels[rowStart + x2] == argb) {
                        x2++;
                    }
                    gfx.fill(tX + x, tY + y, tX + x2, tY + y + 1, argb);
                    x = x2 - 1; // Skip already drawn pixels
                }
            }
        } else {
            MediaSource src = MediaSource.fromAppUserModelId(media.appUserModelId);
            int c = src.argbIconColor();
            gfx.fill(tX, tY, tX + THUMB, tY + THUMB, 0xFF000000 | (c & 0xFFFFFF));
            gfx.hLine(tX, tX + THUMB - 1, tY,             BORDER_LT);
            gfx.vLine(tX, tY, tY + THUMB - 1,             BORDER_LT);
            gfx.hLine(tX, tX + THUMB - 1, tY + THUMB - 1, BORDER_RB);
            gfx.vLine(tX + THUMB - 1, tY, tY + THUMB - 1, BORDER_RB);
        }

        // title + artist — vertically centered in thumb area
        int txX    = tX + THUMB + PAD;
        int titleY = pT + PAD + (THUMB / 2 - font.lineHeight);
        int artY   = titleY + font.lineHeight + 3;
        gfx.drawString(font, title,  txX, titleY, COL_TITLE, false);
        gfx.drawString(font, artist, txX, artY,   COL_SUB,   false);

        // visualizer — bottom of compact panel, below text
        int vizTotalW2 = VIZ_N * (VIZ_BW + VIZ_GAP) - VIZ_GAP;
        int vX    = pR - PAD - vizTotalW2;
        int vBase = pT + cH - PAD;          // bottom of compact area
        int vTop  = pT + cH - PAD - VIZ_MH; // top of viz area
        for (int i = 0; i < VIZ_N; i++) {
            int bh  = Math.max(1, (int)(vizH[i] * VIZ_MH));
            int bx  = vX + i * (VIZ_BW + VIZ_GAP);
            int col = media.isPlaying() ? VIZ_ON : VIZ_OFF;
            gfx.fill(bx, vBase - bh, bx + VIZ_BW, vBase, col);
        }

        // ── expanded content ─────────────────────────────────────────────────
        if (anim < 0.01f) {
            btnsActive = false;
            barActive  = false;
            return;
        }

        int a255 = Math.min(255, (int)(anim * 255));

        // progress bar
        int bY = pT + cH + 2;
        barL = pL + PAD;
        barR = pR - PAD;
        barT = bY;
        barActive = true;

        long pos    = media.extrapolatedPositionMs();
        long dur    = media.durationMs;
        float t     = dur > 0 ? Mth.clamp((float) pos / dur, 0f, 1f) : 0f;
        int filled  = barL + Math.round((barR - barL) * t);

        gfx.fill(barL, bY, barR,   bY + BAR_H, fade(BAR_BG, a255));
        gfx.fill(barL, bY, filled, bY + BAR_H, fade(BAR_FG, a255));
        // seek dot always at current position
        gfx.fill(filled - 1, bY - 1, filled + 2, bY + BAR_H + 1,
                 hitBar(mx, my) ? BAR_SEEK : fade(0xFFCCCCCC, a255));

        // time
        int tmY  = bY + BAR_H + 3;
        String tL = fmtMs(pos);
        String tR = "-" + fmtMs(Math.max(0, dur - pos));
        gfx.drawString(font, tL, barL,                      tmY, fade(COL_SUB, a255), false);
        gfx.drawString(font, tR, barR - font.width(tR),     tmY, fade(COL_SUB, a255), false);

        // buttons  ◄  ▶/▐▐  ►
        int btY    = tmY + font.lineHeight + 3;
        int totalW = BTN_W * 3 + PAD * 2;
        int bsX    = pL + (cW - totalW) / 2;

        btnPrevX = bsX;
        btnPlayX = bsX + BTN_W + PAD;
        btnNextX = bsX + (BTN_W + PAD) * 2;
        btnPrevY = btnPlayY = btnNextY = btY;
        btnsActive = anim > 0.4f;

        boolean hPrev = hitBtn(mx, my, btnPrevX, btnPrevY);
        boolean hPlay = hitBtn(mx, my, btnPlayX, btnPlayY);
        boolean hNext = hitBtn(mx, my, btnNextX, btnNextY);

        drawBtn(gfx, btnPrevX, btnPrevY, hPrev, a255);
        drawBtn(gfx, btnPlayX, btnPlayY, hPlay, a255);
        drawBtn(gfx, btnNextX, btnNextY, hNext, a255);

        iconPrev (gfx, btnPrevX, btnPrevY, fade(hPrev ? COL_TITLE : COL_SUB, a255));
        if (media.isPlaying())
            iconPause(gfx, btnPlayX, btnPlayY, fade(hPlay ? COL_TITLE : COL_SUB, a255));
        else
            iconPlay (gfx, btnPlayX, btnPlayY, fade(hPlay ? COL_TITLE : COL_SUB, a255));
        iconNext (gfx, btnNextX, btnNextY, fade(hNext ? COL_TITLE : COL_SUB, a255));
    }

    private static void reset() {
        btnsActive = false;
        barActive  = false;
        anim = 0f;
    }

    // ── Thumbnail ─────────────────────────────────────────────────────────────
    /** Called from render thread each frame — uploads pending thumbnail if any */
    private static void loadThumb(Minecraft mc, String b64) {
        if (b64 == null || b64.isEmpty()) {
            if (thumbTex != null) { 
                mc.getTextureManager().release(THUMB_RL);
                thumbTex.close(); 
                thumbTex = null; 
            }
            thumbRL = null;
            thumbPixels = null;
            cachedKey = "";
            return;
        }
        if (b64.equals(cachedKey)) return;
        cachedKey = b64; // set immediately to prevent spam retries
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            NativeImage src = NativeImage.read(new ByteArrayInputStream(bytes));
            
            // Create THUMB x THUMB image and cache pixels
            NativeImage scaled = new NativeImage(THUMB, THUMB, false);
            int[] pixels = new int[THUMB * THUMB];
            
            for (int y = 0; y < THUMB; y++) {
                for (int x = 0; x < THUMB; x++) {
                    int sx = x * src.getWidth()  / THUMB;
                    int sy = y * src.getHeight() / THUMB;
                    int pixel = src.getPixel(sx, sy);
                    
                    // NativeImage.getPixel returns ABGR format
                    // But we need to swap R and B for correct colors
                    int a = (pixel >> 24) & 0xFF;
                    if (a == 0) a = 255; // Ensure opaque
                    int b = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int r = (pixel) & 0xFF;
                    
                    // Swap R and B to fix color inversion
                    int argb = (a << 24) | (b << 16) | (g << 8) | r;
                    
                    pixels[y * THUMB + x] = argb;
                    scaled.setPixel(x, y, pixel);
                }
            }
            src.close();
            
            // Cache pixels for fast rendering
            thumbPixels = pixels;
            
            // Upload to GPU as texture (for future use if we find working blit method)
            if (thumbTex != null) {
                mc.getTextureManager().release(THUMB_RL);
                thumbTex.close();
            }
            thumbTex = new DynamicTexture(() -> "album_art", scaled);
            mc.getTextureManager().register(THUMB_RL, thumbTex);
            thumbRL = THUMB_RL;
        } catch (Exception e) {
            if (thumbTex != null) {
                mc.getTextureManager().release(THUMB_RL);
                thumbTex.close();
            }
            thumbTex = null;
            thumbRL  = null;
            thumbPixels = null;
        }
    }

    // ── Draw helpers ──────────────────────────────────────────────────────────
    private static void drawPanel(GuiGraphics g, int l, int t, int r, int b) {
        g.fill(l + 1, t + 1, r - 1, b - 1, BG);
        g.hLine(l, r - 1, t,     BORDER_LT);
        g.vLine(l, t,     b - 1, BORDER_LT);
        g.hLine(l, r - 1, b - 1, BORDER_RB);
        g.vLine(r - 1, t, b - 1, BORDER_RB);
    }

    private static void drawBtn(GuiGraphics g, int x, int y, boolean hov, int a) {
        if (hov) g.fill(x, y, x + BTN_W, y + BTN_H, fade(0x50FFFFFF, a));
        g.hLine(x, x + BTN_W - 1, y,              fade(BORDER_LT, a));
        g.vLine(x, y, y + BTN_H - 1,              fade(BORDER_LT, a));
        g.hLine(x, x + BTN_W - 1, y + BTN_H - 1, fade(BORDER_RB, a));
        g.vLine(x + BTN_W - 1, y, y + BTN_H - 1, fade(BORDER_RB, a));
    }

    /** ◄| skip previous — bar on right, triangle points left */
    private static void iconPrev(GuiGraphics g, int bx, int by, int c) {
        int cx = bx + BTN_W / 2 + 1, cy = by + BTN_H / 2;
        // triangle pointing left: wide on right, narrow on left
        g.fill(cx - 3, cy,     cx - 2, cy + 1, c);
        g.fill(cx - 2, cy - 1, cx - 1, cy + 2, c);
        g.fill(cx - 1, cy - 2, cx,     cy + 3, c);
        g.fill(cx,     cy - 3, cx + 1, cy + 4, c);
        // bar on right
        g.fill(cx + 2, cy - 3, cx + 4, cy + 4, c);
    }

    /** |► skip next — bar on left, triangle points right */
    private static void iconNext(GuiGraphics g, int bx, int by, int c) {
        int cx = bx + BTN_W / 2 - 2, cy = by + BTN_H / 2;
        // bar on left
        g.fill(cx - 2, cy - 3, cx, cy + 4, c);
        // triangle pointing right
        g.fill(cx + 1, cy - 3, cx + 2, cy + 4, c);
        g.fill(cx + 2, cy - 2, cx + 3, cy + 3, c);
        g.fill(cx + 3, cy - 1, cx + 4, cy + 2, c);
        g.fill(cx + 4, cy,     cx + 5, cy + 1, c);
    }

    /** ► play — triangle points right */
    private static void iconPlay(GuiGraphics g, int bx, int by, int c) {
        int cx = bx + BTN_W / 2 - 2, cy = by + BTN_H / 2;
        g.fill(cx,     cy - 3, cx + 1, cy + 4, c);
        g.fill(cx + 1, cy - 2, cx + 2, cy + 3, c);
        g.fill(cx + 2, cy - 1, cx + 3, cy + 2, c);
        g.fill(cx + 3, cy,     cx + 4, cy + 1, c);
    }

    /** ▐▐ pause — two vertical bars */
    private static void iconPause(GuiGraphics g, int bx, int by, int c) {
        int cx = bx + BTN_W / 2, cy = by + BTN_H / 2;
        g.fill(cx - 3, cy - 3, cx - 1, cy + 4, c);
        g.fill(cx + 1, cy - 3, cx + 3, cy + 4, c);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────
    private static boolean shouldRender(Minecraft mc) {
        return WindowsSupport.isWindows()
            && !mc.options.hideGui
            && !mc.options.keyPlayerList.isDown();
    }

    private static boolean hitBtn(double mx, double my, int bx, int by) {
        return mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H;
    }

    private static boolean hitBar(double mx, double my) {
        return barActive
            && mx >= barL && mx <= barR
            && my >= barT - 3 && my <= barT + BAR_H + 3;
    }

    /** Multiply existing alpha by factor 0-255 */
    private static int fade(int argb, int a255) {
        int origA = (argb >>> 24) & 0xFF;
        int newA  = (origA * a255) / 255;
        return (argb & 0x00FFFFFF) | (newA << 24);
    }

    private static String trim(Font font, String text, int maxW) {
        if (text == null || text.isEmpty()) return "";
        if (font.width(text) <= maxW) return text;
        String e = "...";
        int max = maxW - font.width(e);
        if (max <= 0) return e;
        int end = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i), nx = i + Character.charCount(cp);
            if (font.width(text.substring(0, nx)) > max) break;
            end = nx; i = nx;
        }
        return end <= 0 ? e : text.substring(0, end) + e;
    }

    private static String fmtMs(long ms) {
        if (ms < 0) ms = 0;
        long s = ms / 1000L, m = s / 60L; s %= 60L;
        return m + ":" + (s < 10 ? "0" : "") + s;
    }

    private static double smx(Minecraft mc) {
        var w = mc.getWindow();
        return mc.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
    }

    private static double smy(Minecraft mc) {
        var w = mc.getWindow();
        return mc.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
    }
}
