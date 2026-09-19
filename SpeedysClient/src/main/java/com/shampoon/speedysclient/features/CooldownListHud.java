package com.shampoon.speedysclient.features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import me.shampoon.cooldownitem.CooldownItemConfig;
import me.shampoon.cooldownitem.mixin.CooldownEntryAccessor;
import me.shampoon.cooldownitem.mixin.ItemCooldownManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

/**
 * Панель списка предметов в КД (как панель таймера эффектов): перетаскивание в чате, оффсеты в {@link CooldownItemConfig}.
 */
public final class CooldownListHud {
    private static final int MARGIN = 5;
    private static final int HEADER_ICON = 8;
    private static final int ITEM_ICON = 10;
    private static final int ROW_BASE = 14;
    private static final int LINE_GAP = 9;
    private static final int PAD = 3;
    private static final int BG = -804253158;
    private static final int OUTLINE = -14534042;
    private static final int COLOR_NAME = 0xFFFFFFFF;
    private static final int COLOR_TIME = 0xFF9EB7FF;
    private static final int SEPARATOR = 0x60000000;

    private static final Identifier HEADER_TEX = Identifier.ofVanilla("textures/item/ender_pearl.png");

    private CooldownListHud() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        HudPanelDragCoordinator.resetCooldownPanel();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null
                || client.options.hudHidden
                || !CooldownItemConfig.get().isHudListMode()) {
            return;
        }
        ClientPlayerEntity player = client.player;
        ItemCooldownManager manager = player.getItemCooldownManager();
        float tickDelta = tickCounter.getTickProgress(false);

        List<CooldownRow> rows = new ArrayList<>();
        Set<Identifier> seen = new HashSet<>();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack st = player.getInventory().getStack(i);
            if (st.isEmpty() || !manager.isCoolingDown(st)) {
                continue;
            }
            Identifier gid = manager.getGroup(st);
            if (!seen.add(gid)) {
                continue;
            }
            float sec = remainingSeconds(manager, st, tickDelta);
            if (sec <= 0.05f) {
                continue;
            }
            rows.add(new CooldownRow(st, sec));
        }
        if (rows.isEmpty()) {
            return;
        }
        rows.sort(Comparator.comparingDouble(a -> a.seconds));

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        var tr = client.textRenderer;
        String headerTitle = "\u041a\u0414 (Cooldowns)";
        int headerH = 17;
        int timeReserve = 48;
        int innerMax = tr.getWidth(headerTitle) + HEADER_ICON + PAD * 3;

        List<RowLayout> layouts = new ArrayList<>();
        int totalBodyH = 0;
        for (int i = 0; i < rows.size(); i++) {
            CooldownRow row = rows.get(i);
            String prefix = (i + 1) + ". ";
            int prefixW = tr.getWidth(prefix);
            int nameAvail = Math.max(60, 220 - prefixW - ITEM_ICON - PAD * 3 - timeReserve);
            List<OrderedText> nameLines = tr.wrapLines(row.stack.getName(), nameAvail);
            if (nameLines.isEmpty()) {
                nameLines = List.of(row.stack.getName().asOrderedText());
            }
            int lineCount = nameLines.size();
            int rowH = Math.max(ROW_BASE, 4 + lineCount * LINE_GAP);
            String timeStr = formatSeconds(row.seconds);
            int rowInner = prefixW + ITEM_ICON + PAD + Math.min(nameAvail, maxOrderedWidth(tr, nameLines)) + PAD + tr.getWidth(timeStr);
            innerMax = Math.max(innerMax, rowInner);
            layouts.add(new RowLayout(prefix, nameLines, timeStr, rowH));
            totalBodyH += rowH;
        }

        int panelW = innerMax + PAD * 2;
        int panelH = headerH + 2 + totalBodyH + PAD;

        int defaultLeft = MARGIN;
        int defaultTop = MARGIN + 4;
        CooldownItemConfig cfg = CooldownItemConfig.get();
        int panelLeft = defaultLeft + cfg.hudPanelOffsetX;
        int panelTop = defaultTop + cfg.hudPanelOffsetY;
        panelLeft = Math.max(4, Math.min(panelLeft, sw - panelW - 4));
        panelTop = Math.max(4, Math.min(panelTop, sh - panelH - 4));

        HudPanelDragCoordinator.publishCooldown(defaultLeft, defaultTop, panelLeft, panelTop, panelW, panelH);

        CooldownListHud.drawRoundedRect(context, panelLeft, panelTop, panelLeft + panelW, panelTop + panelH, BG);
        CooldownListHud.drawRoundedOutline(context, panelLeft, panelTop, panelLeft + panelW, panelTop + panelH, OUTLINE);

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

        int ry = sepY + 3;
        for (int i = 0; i < layouts.size(); i++) {
            RowLayout lay = layouts.get(i);
            int rowH = lay.rowHeight;
            context.drawText(tr, lay.prefix, panelLeft + PAD, ry + (rowH - 9) / 2, COLOR_NAME, false);
            int pix = panelLeft + PAD + tr.getWidth(lay.prefix);
            int iy = ry + (rowH - ITEM_ICON) / 2;
            context.drawItem(rows.get(i).stack, pix, iy);
            int tx = pix + ITEM_ICON + PAD;
            int nameY = ry + (rowH - lay.nameLines.size() * LINE_GAP - 2) / 2;
            int ly = nameY;
            for (OrderedText ot : lay.nameLines) {
                context.drawText(tr, ot, tx, ly, COLOR_NAME, false);
                ly += LINE_GAP;
            }
            int tw = tr.getWidth(lay.timeStr);
            context.drawText(tr, lay.timeStr, panelLeft + panelW - PAD - tw, ry + (rowH - 9) / 2, COLOR_TIME, false);
            ry += rowH;
        }
    }

    private static int maxOrderedWidth(net.minecraft.client.font.TextRenderer tr, List<OrderedText> lines) {
        int m = 0;
        for (OrderedText ot : lines) {
            m = Math.max(m, tr.getWidth(ot));
        }
        return m;
    }

    private static String formatSeconds(float sec) {
        if (sec >= 10.0f) {
            return Math.round(sec) + "\u0441.";
        }
        return String.format(Locale.ROOT, "%.1f\u0441.", sec);
    }

    private static float remainingSeconds(ItemCooldownManager manager, ItemStack stack, float tickDelta) {
        ItemCooldownManagerAccessor ma = (ItemCooldownManagerAccessor) manager;
        Identifier gid = manager.getGroup(stack);
        Object raw = ma.cooldownitem$getEntries().get(gid);
        if (raw == null) {
            return 0.0f;
        }
        CooldownEntryAccessor ea = (CooldownEntryAccessor) raw;
        int end = ea.cooldownitem$getEndTick();
        int tick = ma.cooldownitem$getTick();
        float remainingTicks = Math.max(0.0f, (float) (end - tick) - tickDelta);
        return remainingTicks / 20.0f;
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

    private record CooldownRow(ItemStack stack, float seconds) {}

    private record RowLayout(String prefix, List<OrderedText> nameLines, String timeStr, int rowHeight) {}
}
