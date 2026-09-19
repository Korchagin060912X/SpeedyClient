/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.text.Text
 */
package com.shampoon.speedysclient.ui;

import com.shampoon.speedysclient.config.SpeedysWatermarkConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class WatermarkModeScreen
extends Screen {
    private static final int PANEL_BG = -805306368;
    private static final int PANEL_OUTLINE = -14996918;
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 200;
    private static final int ROW_H = 18;
    private static final int ROW_GAP = 6;
    private final Screen parent;
    private final List<Row> rows = new ArrayList<Row>();

    public WatermarkModeScreen(Screen parent) {
        super((Text)Text.literal((String)"\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a"));
        this.parent = parent;
    }

    protected void init() {
        this.rows.clear();
        int cx = this.width / 2;
        int y = this.panelTop() + 34;
        int x = cx - 110;
        int w = 220;
        this.rows.add(new Row(x, y, w, 18, "FPS \u0438 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430 (ms)", SpeedysWatermarkConfig.DisplayMode.FPS_MS));
        this.rows.add(new Row(x, y += 24, w, 18, "\u041c\u0443\u0437\u044b\u043a\u0430 (GSMTC)", SpeedysWatermarkConfig.DisplayMode.MUSIC));
        this.rows.add(new Row(x, y += 24, w, 18, "Armor Helper", SpeedysWatermarkConfig.DisplayMode.ARMOR));
        this.rows.add(new Row(x, y += 24, w, 18, "EES Radar", SpeedysWatermarkConfig.DisplayMode.EES_RADAR));
        this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)"\u0417\u0430\u043a\u0440\u044b\u0442\u044c"), b -> this.close()).dimensions(cx - 70, this.panelTop() + 200 - 22, 140, 16).build());
    }

    private static String modeLabel(SpeedysWatermarkConfig.DisplayMode m) {
        String base = switch (m) {
            case FPS_MS -> "FPS \u0438 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430 (ms)";
            case MUSIC -> "\u041c\u0443\u0437\u044b\u043a\u0430 (GSMTC)";
            case ARMOR -> "Armor Helper";
            case EES_RADAR -> "EES Radar";
        };
        if (SpeedysWatermarkConfig.get().mode == m) {
            return "\u00a7a\u25ba " + base;
        }
        return base;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderBackground(context, mouseX, mouseY, deltaTicks);
        int left = this.panelLeft();
        int right = left + 280;
        int top = this.panelTop();
        int bottom = top + 200;
        this.drawRoundedRect(context, left, top, right, bottom, -805306368);
        this.drawRoundedOutline(context, left, top, right, bottom, -14996918);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, top + 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "\u041f\u041a\u041c \u043f\u043e watermark: \u0440\u0435\u0436\u0438\u043c", this.width / 2, top + 22, 0xAAAAAA);
        for (Row row : this.rows) {
            this.drawRoundedRect(context, row.x, row.y, row.x + row.w, row.y + row.h, -15592419);
            this.drawRoundedOutline(context, row.x, row.y, row.x + row.w, row.y + row.h, -14534042);
            boolean active = SpeedysWatermarkConfig.get().mode == row.mode;
            context.drawText(this.textRenderer, (active ? "\u25b6 " : "") + row.label, row.x + 8, row.y + 5, active ? -8585348 : -1, false);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private int panelLeft() {
        return this.width / 2 - 140;
    }

    private int panelTop() {
        return this.height / 2 - 100;
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

    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    public boolean shouldPause() {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Row row : this.rows) {
                if (!(mouseX >= (double)row.x) || !(mouseX <= (double)(row.x + row.w)) || !(mouseY >= (double)row.y) || !(mouseY <= (double)(row.y + row.h))) continue;
                SpeedysWatermarkConfig.get().mode = row.mode;
                SpeedysWatermarkConfig.save();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static final class Row {
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final String label;
        private final SpeedysWatermarkConfig.DisplayMode mode;

        private Row(int x, int y, int w, int h, String label, SpeedysWatermarkConfig.DisplayMode mode) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.mode = mode;
        }
    }
}

