package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Мини-HUD: трапка всегда первая снизу (у Inv HUD), выше — свап и тотем (очередь до 3 строк суммарно).
 */
public final class MiniHudNotifications {
    private static final int MAX_PILLS = 3;
    private static final int SWAP_TTL_MS = 4000;
    private static final int TOTEM_TTL_MS = 4500;
    private static final int PAD = 6;
    private static final int SEP_W = 2;
    private static final int GAP = 5;
    private static final int BG = 0xE0101010;
    private static final int OUTLINE = 0xFF3A3A3A;
    private static final int SEP_COLOR = 0xFF6A6A6A;

    private enum EphemeralKind {
        SWAP,
        TOTEM
    }

    private static boolean trapLineActive;
    private static Text trapLineText = Text.empty();

    private static final ArrayDeque<EphemeralEntry> EPHEMERAL = new ArrayDeque<>();

    private static final class EphemeralEntry {
        final Text text;
        final long expireAtMs;
        final EphemeralKind kind;

        EphemeralEntry(Text text, long expireAtMs, EphemeralKind kind) {
            this.text = text;
            this.expireAtMs = expireAtMs;
            this.kind = kind;
        }
    }

    private MiniHudNotifications() {
    }

    /** Сброс при выключении мини-HUD в настройках. */
    public static void clearForMiniHudDisabled() {
        trapLineActive = false;
        trapLineText = Text.empty();
        EPHEMERAL.clear();
    }

    public static void clearTrap() {
        trapLineActive = false;
        trapLineText = Text.empty();
        trimEphemeral();
    }

    public static void updateTrapCountdown(double secondsLeft) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.miniHudNotifications) {
            return;
        }
        if (secondsLeft > 0.05) {
            trapLineActive = true;
            String num = String.format(Locale.US, "%.1f", secondsLeft);
            trapLineText = Text.literal("\u0422\u0440\u0430\u043f\u043a\u0430 \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437: " + num);
            trimEphemeral();
        } else {
            clearTrap();
        }
    }

    public static void pushItemSwap(Text line) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.miniHudNotifications) {
            return;
        }
        long now = System.currentTimeMillis();
        EPHEMERAL.addLast(new EphemeralEntry(line.copy(), now + SWAP_TTL_MS, EphemeralKind.SWAP));
        trimEphemeral();
    }

    public static void pushTotem(Text line) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.miniHudNotifications) {
            return;
        }
        long now = System.currentTimeMillis();
        EPHEMERAL.addLast(new EphemeralEntry(line.copy(), now + TOTEM_TTL_MS, EphemeralKind.TOTEM));
        trimEphemeral();
    }

    public static void tick(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.miniHudNotifications) {
            EPHEMERAL.clear();
            return;
        }
        long now = System.currentTimeMillis();
        EPHEMERAL.removeIf(e -> now >= e.expireAtMs);
        trimEphemeral();
    }

    private static void trimEphemeral() {
        int cap = trapLineActive ? 2 : 3;
        while (EPHEMERAL.size() > cap) {
            EPHEMERAL.removeFirst();
        }
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        HudPanelDragCoordinator.resetNotifyPanel();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null
                || client.options.hudHidden
                || PVPUtils.CONFIG == null
                || !PVPUtils.CONFIG.miniHudNotifications) {
            return;
        }

        List<PillSpec> pills = buildPills();
        if (pills.isEmpty()) {
            return;
        }

        while (pills.size() > MAX_PILLS) {
            int remove = trapLineActive ? 1 : 0;
            if (remove < pills.size()) {
                pills.remove(remove);
            } else {
                pills.remove(pills.size() - 1);
            }
        }

        var tr = client.textRenderer;
        int[] pillW = new int[pills.size()];
        int maxPillW = 0;
        for (int i = 0; i < pills.size(); i++) {
            PillSpec p = pills.get(i);
            int inner = tr.getWidth(p.text);
            int w = PAD + p.iconPx + SEP_W + 4 + inner + PAD;
            pillW[i] = w;
            maxPillW = Math.max(maxPillW, w);
        }

        int totalH = 0;
        for (int i = 0; i < pills.size(); i++) {
            totalH += pills.get(i).pillH;
            if (i < pills.size() - 1) {
                totalH += GAP;
            }
        }

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        float scale = PVPUtils.CONFIG.invHudScale;
        int baseX = sw / 2 + 92 + PVPUtils.CONFIG.invHudOffsetX;
        int baseY = sh - 58 + PVPUtils.CONFIG.invHudOffsetY;
        int defCenterX = baseX + Math.round(9 * 18 * scale * 0.5f);
        int defStackBottom = baseY - GAP;

        int defBlockLeft = defCenterX - maxPillW / 2;
        int defBlockTop = defStackBottom - totalH;
        int ox = PVPUtils.CONFIG.miniHudNotifyOffsetX;
        int oy = PVPUtils.CONFIG.miniHudNotifyOffsetY;
        int blockLeft = Math.max(4, Math.min(defBlockLeft + ox, sw - maxPillW - 4));
        int blockTop = Math.max(4, Math.min(defBlockTop + oy, sh - totalH - 4));
        int anchorX = blockLeft + maxPillW / 2;

        HudPanelDragCoordinator.publishNotify(defBlockLeft, defBlockTop, blockLeft, blockTop, maxPillW, totalH);

        if (client.currentScreen instanceof ChatScreen) {
            drawRoundedOutline(context, blockLeft - 1, blockTop - 1, blockLeft + maxPillW + 1, blockTop + totalH + 1, 0x4080FF80);
        }

        int offsetFromBottom = 0;
        for (int i = 0; i < pills.size(); i++) {
            PillSpec p = pills.get(i);
            int h = p.pillH;
            int w = pillW[i];
            int pillBottom = blockTop + totalH - offsetFromBottom;
            int pillTop = pillBottom - h;
            offsetFromBottom += h + GAP;

            int pillLeft = anchorX - w / 2;
            drawRoundedRect(context, pillLeft, pillTop, pillLeft + w, pillBottom, BG);
            drawRoundedOutline(context, pillLeft, pillTop, pillLeft + w, pillBottom, OUTLINE);

            int iconX = pillLeft + PAD;
            int iconY = pillTop + (h - p.iconPx) / 2;
            drawItemScaled(context, p.iconStack, iconX, iconY, p.iconPx);

            int sepX = pillLeft + PAD + p.iconPx + 2;
            int sepTop = pillTop + Math.max(3, (h - 10) / 2);
            int sepBottom = pillBottom - Math.max(3, (h - 10) / 2);
            context.fill(sepX, sepTop, sepX + 1, sepBottom, SEP_COLOR);

            int textX = sepX + SEP_W + 3;
            int textY = pillTop + (h - 8) / 2;
            context.drawTextWithShadow(tr, p.text, textX, textY, 0xFFFFFFFF);
        }
    }

    private static List<PillSpec> buildPills() {
        List<PillSpec> out = new ArrayList<>();
        if (trapLineActive && trapLineText != null && !trapLineText.getString().isEmpty()) {
            int h = PVPUtils.CONFIG.miniNotifyTrapPillH;
            int ic = PVPUtils.CONFIG.miniNotifyTrapIconPx;
            out.add(new PillSpec(trapLineText, new ItemStack(Items.CLOCK), h, ic));
        }
        for (EphemeralEntry e : EPHEMERAL) {
            if (e.kind == EphemeralKind.SWAP) {
                int h = PVPUtils.CONFIG.miniNotifySwapPillH;
                int ic = PVPUtils.CONFIG.miniNotifySwapIconPx;
                out.add(new PillSpec(e.text, new ItemStack(Items.ARROW), h, ic));
            } else {
                int h = PVPUtils.CONFIG.miniNotifyTotemPillH;
                int ic = PVPUtils.CONFIG.miniNotifyTotemIconPx;
                out.add(new PillSpec(e.text, new ItemStack(Items.TOTEM_OF_UNDYING), h, ic));
            }
        }
        return out;
    }

    private static final class PillSpec {
        final Text text;
        final ItemStack iconStack;
        final int pillH;
        final int iconPx;

        PillSpec(Text text, ItemStack iconStack, int pillH, int iconPx) {
            this.text = text;
            this.iconStack = iconStack;
            this.pillH = pillH;
            this.iconPx = iconPx;
        }
    }

    private static void drawItemScaled(DrawContext context, ItemStack stack, int x, int y, int targetPx) {
        if (stack.isEmpty()) {
            return;
        }
        float s = targetPx / 16.0f;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(s, s);
        context.drawItem(stack, 0, 0);
        context.getMatrices().popMatrix();
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
