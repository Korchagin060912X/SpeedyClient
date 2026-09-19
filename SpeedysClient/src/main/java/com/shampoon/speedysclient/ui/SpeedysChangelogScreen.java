package com.shampoon.speedysclient.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Экран списка изменений (кнопка Logs в главном меню мода).
 */
public final class SpeedysChangelogScreen extends Screen {
    private static final int PANEL_BG = -805306368;
    private static final int PANEL_OUTLINE = -14996918;
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 260;
    private static final int LINE_H = 11;
    private static final int CONTENT_PAD = 10;

    private final Screen parent;
    private int scroll;
    private List<String> lines = List.of();
    private int contentTop;
    private int contentBottom;
    private int maxScroll;

    public SpeedysChangelogScreen(Screen parent) {
        super(Text.literal("ChangeLogs"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();
        int pl = this.panelLeft();
        int pt = this.panelTop();
        int cx = pl + PANEL_W / 2;
        this.contentTop = pt + 34;
        this.contentBottom = pt + PANEL_H - 28;
        this.rebuildLines();
        this.clampScroll();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u0413\u043e\u0442\u043e\u0432\u043e"), b -> this.close())
                .dimensions(cx - 85, pt + PANEL_H - 22, 170, 16).build());
    }

    private void rebuildLines() {
        int maxW = PANEL_W - CONTENT_PAD * 2;
        ArrayList<String> out = new ArrayList<>();
        for (String[] block : SpeedysChangelogData.BLOCKS) {
            for (String line : block) {
                if (line.isEmpty()) {
                    out.add("");
                    continue;
                }
                out.addAll(this.wrapLine(line, maxW));
            }
            out.add("");
        }
        this.lines = out;
        int viewH = this.contentBottom - this.contentTop;
        int total = this.lines.size() * LINE_H;
        this.maxScroll = Math.max(0, total - viewH);
    }

    private List<String> wrapLine(String text, int maxWidth) {
        ArrayList<String> out = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (this.textRenderer.getWidth(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if (!current.isEmpty()) {
                out.add(current.toString());
                current.setLength(0);
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            out.add(current.toString());
        }
        return out;
    }

    private void clampScroll() {
        this.scroll = Math.max(0, Math.min(this.maxScroll, this.scroll));
    }

    private int panelLeft() {
        return this.width / 2 - PANEL_W / 2;
    }

    private int panelTop() {
        return this.height / 2 - PANEL_H / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, this.width, this.height, -1610612736);
        int pl = this.panelLeft();
        int pt = this.panelTop();
        this.drawRoundedRect(context, pl, pt, pl + PANEL_W, pt + PANEL_H, PANEL_BG);
        this.drawRoundedOutline(context, pl, pt, pl + PANEL_W, pt + PANEL_H, PANEL_OUTLINE);
        String hdr = "ChangeLogs";
        context.drawText(this.textRenderer, hdr, pl + (PANEL_W - this.textRenderer.getWidth(hdr)) / 2, pt + 10, -6309633, false);
        super.render(context, mouseX, mouseY, deltaTicks);
        int lx = pl + CONTENT_PAD;
        int y = this.contentTop - this.scroll;
        for (String line : this.lines) {
            if (y + LINE_H >= this.contentTop && y <= this.contentBottom) {
                int color = line.startsWith("--") ? -8882056 : (line.startsWith("-") ? -7566196 : -1);
                context.drawText(this.textRenderer, line, lx, y + 2, color, false);
            }
            y += LINE_H;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= this.panelLeft() && mouseX <= this.panelLeft() + PANEL_W
                && mouseY >= this.contentTop && mouseY <= this.contentBottom) {
            this.scroll = (int) ((double) this.scroll - verticalAmount * 18.0);
            this.clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void drawRoundedRect(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, b, color);
        context.fill(l, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 3, color);
        context.fill(r - 3, t + 1, r - 1, t + 3, color);
        context.fill(l + 1, b - 3, l + 3, b - 1, color);
        context.fill(r - 3, b - 3, r - 1, b - 1, color);
    }

    private void drawRoundedOutline(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, t + 1, color);
        context.fill(l + 3, b - 1, r - 3, b, color);
        context.fill(l, t + 3, l + 1, b - 3, color);
        context.fill(r - 1, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 2, color);
        context.fill(r - 3, t + 1, r - 1, t + 2, color);
        context.fill(l + 1, b - 2, l + 3, b - 1, color);
        context.fill(r - 3, b - 2, r - 1, b - 1, color);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
