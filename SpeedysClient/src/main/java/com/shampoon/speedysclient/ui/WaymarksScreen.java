package com.shampoon.speedysclient.ui;

import com.shampoon.speedysclient.config.WaymarksConfig;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Экран «Метки»: список и редактор; «Настройки» открывают отдельный вид только с двумя переключателями.
 */
public final class WaymarksScreen extends Screen {
    private static final int PANEL_BG = -805306368;
    private static final int PANEL_OUTLINE = -14996918;
    private static final int BTN_BG = -15592419;
    private static final int BTN_OUTLINE = -14534042;
    private static final int SWITCH_ON = -9545473;
    private static final int SWITCH_OFF = -11709847;
    private static final int SWITCH_KNOB = -1184001;
    /** Кнопка «+» (только обводка) и зазор до «Настройки». */
    private static final int TOP_SQUARE = 16;
    private static final int TOP_GAP = 4;
    private static final int PANEL_W = 468;
    private static final int PANEL_H = 278;
    /** Высота строки списка: иконка + две строки текста слева, кнопка справа без наложения. */
    private static final int ROW_H = 44;
    private static final int ICON_STRIDE = 26;
    private static final int ICON_COLS = 5;
    private static final Identifier ID_ICON = Identifier.of("speedysclient", "textures/gui/id.png");

    private final Screen parent;
    private int listScroll;
    private int selectedIndex = -1;
    private boolean iconPickerOpen;
    /** true — только две настройки; список и редактор скрыты. */
    private boolean settingsView;
    private TextFieldWidget nameField;
    private TextFieldWidget coordField;
    private SliderWidget rSlider;
    private SliderWidget gSlider;
    private SliderWidget bSlider;
    private int addBtnX;
    private int addBtnY;
    private int settingsBtnX;
    private int settingsBtnY;
    private int listLeft;
    private int listTop;
    private int listW;
    private int listH;
    private int settingsTogglePlayerY;
    private int settingsToggleDeathY;
    /** Левый верх сетки иконок (клики обрабатываются вручную). */
    private int iconGridLeft;
    private int iconGridTop;
    private int editLeft;
    private int editTop;
    private int editW;
    private int syIconButton;
    /** Кнопка «удалить метку» (иконка корзины), только при выбранной метке. */
    private int deleteBtnX;
    private int deleteBtnY;

    public WaymarksScreen(Screen parent) {
        super(Text.literal("\u041c\u0435\u0442\u043a\u0438"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();
        int pl = this.panelLeft();
        int pt = this.panelTop();
        int cx = pl + PANEL_W / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u0413\u043e\u0442\u043e\u0432\u043e"), b -> this.close())
                .dimensions(cx - 85, pt + PANEL_H - 24, 170, 16).build());

        if (this.settingsView) {
            this.initSettingsView(pl, pt);
            return;
        }

        this.listLeft = pl + 10;
        this.listTop = pt + 34;
        this.listW = 168;
        this.listH = 188;
        this.editLeft = this.listLeft + this.listW + 10;
        this.editTop = this.listTop;
        this.editW = PANEL_W - (this.editLeft - pl) - 10;

        this.addBtnX = pl + 10;
        this.addBtnY = pt + 8;
        this.settingsBtnX = this.addBtnX + TOP_SQUARE + TOP_GAP;
        this.settingsBtnY = pt + 8;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"), b -> {
            this.settingsView = true;
            this.iconPickerOpen = false;
            this.init();
        }).dimensions(this.settingsBtnX, this.settingsBtnY, 88, 16).build());

        WaymarksConfig cfg = WaymarksConfig.get();
        if (this.selectedIndex >= cfg.markers.size()) {
            this.selectedIndex = cfg.markers.size() - 1;
        }

        int fieldW = this.editW;
        if (this.selectedIndex >= 0 && this.selectedIndex < cfg.markers.size()) {
            fieldW = this.editW - 22;
        }
        this.nameField = new TextFieldWidget(this.textRenderer, this.editLeft, this.editTop + 20, fieldW, 18, Text.literal("name"));
        this.nameField.setMaxLength(64);
        this.nameField.setPlaceholder(Text.literal("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043c\u0435\u0442\u043a\u0438"));
        this.coordField = new TextFieldWidget(this.textRenderer, this.editLeft, this.editTop + 54, fieldW, 18, Text.literal("xyz"));
        this.coordField.setMaxLength(64);
        this.coordField.setPlaceholder(Text.literal("X Y Z"));

        if (this.selectedIndex >= 0 && this.selectedIndex < cfg.markers.size()) {
            WaymarksConfig.WaymarkEntry e = cfg.markers.get(this.selectedIndex);
            this.nameField.setText(e.name);
            this.coordField.setText(String.format(java.util.Locale.ROOT, "%.1f %.1f %.1f", e.x, e.y, e.z));
            this.nameField.setChangedListener(s -> this.applyEditor());
            this.coordField.setChangedListener(s -> this.applyEditor());
            this.addDrawableChild(this.nameField);
            this.addDrawableChild(this.coordField);
            this.deleteBtnX = this.editLeft + this.editW - 18;
            this.deleteBtnY = this.editTop + 2;
            Text bin = Text.literal("\uD83D\uDDD1").styled(s -> s.withColor(Formatting.RED));
            this.addDrawableChild(ButtonWidget.builder(bin, b -> this.deleteSelectedMarker())
                    .dimensions(this.deleteBtnX, this.deleteBtnY, 18, 16)
                    .build());
        }

        this.syIconButton = this.editTop + 92;
        int iconBtnW = this.selectedIndex >= 0 && this.selectedIndex < cfg.markers.size() ? this.editW - 22 : this.editW;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(this.iconPickerOpen
                        ? "\u0417\u0430\u043a\u0440\u044b\u0442\u044c \u0438\u043a\u043e\u043d\u043a\u0438"
                        : "\u0412\u044b\u0431\u0440\u0430\u0442\u044c \u0438\u043a\u043e\u043d\u043a\u0443"),
                b -> {
                    this.iconPickerOpen = !this.iconPickerOpen;
                    this.init();
                }).dimensions(this.editLeft, this.syIconButton, iconBtnW, 16).build());

        int pickerGap = this.iconPickerOpen ? 58 : 0;
        this.iconGridLeft = this.editLeft;
        this.iconGridTop = this.syIconButton + 18;
        int slideBase = this.syIconButton + 32 + pickerGap;

        if (this.selectedIndex >= 0 && this.selectedIndex < cfg.markers.size()) {
            WaymarksConfig.WaymarkEntry e = cfg.markers.get(this.selectedIndex);
            int y0 = slideBase;
            int sw = iconBtnW;
            this.rSlider = rgbSlider(this.editLeft, y0, sw, "R", e.r, v -> {
                e.r = v;
                WaymarksConfig.save();
            });
            this.gSlider = rgbSlider(this.editLeft, y0 + 20, sw, "G", e.g, v -> {
                e.g = v;
                WaymarksConfig.save();
            });
            this.bSlider = rgbSlider(this.editLeft, y0 + 40, sw, "B", e.b, v -> {
                e.b = v;
                WaymarksConfig.save();
            });
            this.addDrawableChild(this.rSlider);
            this.addDrawableChild(this.gSlider);
            this.addDrawableChild(this.bSlider);
        }
    }

    private void initSettingsView(int pl, int pt) {
        WaymarksConfig cfg = WaymarksConfig.get();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u041a \u043c\u0435\u0442\u043a\u0430\u043c"), b -> {
            this.settingsView = false;
            this.init();
        }).dimensions(pl + 10, pt + 8, 100, 16).build());

        int midY = pt + PANEL_H / 2 - 28;
        this.settingsTogglePlayerY = midY;
        this.settingsToggleDeathY = midY + 24;
    }

    private static SliderWidget rgbSlider(int x, int y, int w, String label, int value, java.util.function.IntConsumer apply) {
        double v01 = value / 255.0;
        return new SliderWidget(x, y, w, 14, Text.literal(""), Math.max(0.0, Math.min(1.0, v01))) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                int v = (int) Math.round(this.value * 255.0);
                this.setMessage(Text.literal(label + ": " + v));
            }

            @Override
            protected void applyValue() {
                int v = (int) Math.round(this.value * 255.0);
                apply.accept(v);
            }
        };
    }

    private void applyEditor() {
        if (this.selectedIndex < 0) {
            return;
        }
        WaymarksConfig cfg = WaymarksConfig.get();
        if (this.selectedIndex >= cfg.markers.size()) {
            return;
        }
        WaymarksConfig.WaymarkEntry e = cfg.markers.get(this.selectedIndex);
        e.name = this.nameField.getText();
        String[] p = this.coordField.getText().trim().split("\\s+");
        if (p.length >= 3) {
            try {
                e.x = Double.parseDouble(p[0]);
                e.y = Double.parseDouble(p[1]);
                e.z = Double.parseDouble(p[2]);
            } catch (NumberFormatException ignored) {
            }
        }
        WaymarksConfig.save();
    }

    private void addNewMarker() {
        MinecraftClient c = this.client;
        if (c == null) {
            return;
        }
        WaymarksConfig cfg = WaymarksConfig.get();
        WaymarksConfig.WaymarkEntry e = new WaymarksConfig.WaymarkEntry();
        e.id = UUID.randomUUID().toString();
        e.name = "\u041d\u043e\u0432\u0430\u044f \u043c\u0435\u0442\u043a\u0430";
        e.dimension = WaymarksConfig.currentDimensionId(c);
        if (cfg.placeAtPlayerPosition && c.player != null) {
            e.x = Math.floor(c.player.getX()) + 0.5;
            e.y = Math.floor(c.player.getY());
            e.z = Math.floor(c.player.getZ()) + 0.5;
        }
        e.iconIndex = 0;
        e.r = 45;
        e.g = 48;
        e.b = 58;
        e.deathMarker = false;
        cfg.markers.add(e);
        WaymarksConfig.save();
        this.selectedIndex = cfg.markers.size() - 1;
        this.iconPickerOpen = false;
        this.init();
    }

    private void deleteSelectedMarker() {
        if (this.selectedIndex < 0) {
            return;
        }
        WaymarksConfig cfg = WaymarksConfig.get();
        if (this.selectedIndex >= cfg.markers.size()) {
            return;
        }
        cfg.markers.remove(this.selectedIndex);
        WaymarksConfig.save();
        this.iconPickerOpen = false;
        if (cfg.markers.isEmpty()) {
            this.selectedIndex = -1;
        } else {
            this.selectedIndex = Math.min(this.selectedIndex, cfg.markers.size() - 1);
        }
        this.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, this.width, this.height, -1610612736);
        int pl = this.panelLeft();
        int pt = this.panelTop();
        this.drawRoundedRect(context, pl, pt, pl + PANEL_W, pt + PANEL_H, PANEL_BG);
        this.drawRoundedOutline(context, pl, pt, pl + PANEL_W, pt + PANEL_H, PANEL_OUTLINE);

        int cx = this.width / 2;
        int titleY = pt - 14;
        int iconS = 16;
        float titleScale = 1.15f;
        String titleLeft = "\u041c\u0435\u0442\u043a\u0438";
        int scaledW = (int) Math.ceil(this.textRenderer.getWidth(titleLeft) * titleScale);
        int totalW = iconS + 6 + scaledW;
        int startX = cx - totalW / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ID_ICON, startX, titleY - 1, 0.0f, 0.0f, iconS, iconS, iconS, iconS);
        int textX = startX + iconS + 6;
        int textY = titleY + 2;
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(titleScale, titleScale);
        context.drawText(this.textRenderer, titleLeft, (int) ((float) textX / titleScale), (int) ((float) textY / titleScale), -14996918, false);
        context.getMatrices().popMatrix();

        WaymarksConfig cfg = WaymarksConfig.get();

        if (this.settingsView) {
            context.drawText(this.textRenderer, "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u043c\u0435\u0442\u043e\u043a", pl + 12, pt + 36, -6309633, false);
            this.drawToggleRow(context, pl + 10, this.settingsTogglePlayerY, PANEL_W - 20,
                    "\u0421\u0442\u0430\u0432\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0443 \u043d\u0430 \u043c\u0435\u0441\u0442\u0435 \u0438\u0433\u0440\u043e\u043a\u0430", cfg.placeAtPlayerPosition,
                    () -> {
                    });
            this.drawToggleRow(context, pl + 10, this.settingsToggleDeathY, PANEL_W - 20,
                    "\u0421\u0442\u0430\u0432\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0443 \u043d\u0430 \u043c\u0435\u0441\u0442\u0435 \u0441\u043c\u0435\u0440\u0442\u0438", cfg.deathMarkerEnabled,
                    () -> {
                    });
            super.render(context, mouseX, mouseY, deltaTicks);
            return;
        }

        this.drawRoundedOutline(context, this.addBtnX, this.addBtnY, this.addBtnX + TOP_SQUARE, this.addBtnY + TOP_SQUARE, PANEL_OUTLINE);
        WaymarksScreen.drawPlusCross(context, this.addBtnX, this.addBtnY, TOP_SQUARE, 0xFFFFFFFF);

        context.drawText(this.textRenderer, "1. \u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043c\u0435\u0442\u043a\u0438", this.editLeft, this.listTop + 6, -6309633, false);
        context.drawText(this.textRenderer, "2. \u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b (X Y Z)", this.editLeft, this.listTop + 40, -6309633, false);
        context.drawText(this.textRenderer, "3. \u0418\u043a\u043e\u043d\u043a\u0430", this.editLeft, this.listTop + 78, -6309633, false);
        int slideBase = this.syIconButton + 32 + (this.iconPickerOpen ? 58 : 0);
        context.drawText(this.textRenderer, "4. \u0426\u0432\u0435\u0442 \u0437\u0430\u043b\u0438\u0432\u043a\u0438 (RGB)", this.editLeft, slideBase - 12, -6309633, false);

        List<WaymarksConfig.WaymarkEntry> markers = cfg.markers;
        if (markers.isEmpty()) {
            context.drawText(this.textRenderer, "\u0421\u043e\u0437\u0434\u0430\u0439\u0442\u0435 \u043d\u043e\u0432\u0443\u044e \u043c\u0435\u0442\u043a\u0443.", this.listLeft + 2, this.listTop + 24, -1, false);
            int rx = this.listLeft + this.listW + 20;
            context.drawText(this.textRenderer, "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 +, \u0447\u0442\u043e\u0431\u044b \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0443.", rx, this.listTop + 60, -1, false);
        } else {
            int bw = 72;
            int textLeft = this.listLeft + 20;
            int btnLeft = this.listLeft + this.listW - bw - 6;
            int maxNameW = btnLeft - textLeft - 4;
            for (int i = 0; i < markers.size(); i++) {
                int ry = this.listTop + i * ROW_H - this.listScroll;
                if (ry + ROW_H < this.listTop || ry > this.listTop + this.listH) {
                    continue;
                }
                WaymarksConfig.WaymarkEntry e = markers.get(i);
                boolean sel = i == this.selectedIndex;
                int bg = sel ? -15063228 : BTN_BG;
                this.drawRoundedRect(context, this.listLeft, ry, this.listLeft + this.listW - 2, ry + ROW_H - 4, bg);
                this.drawRoundedOutline(context, this.listLeft, ry, this.listLeft + this.listW - 2, ry + ROW_H - 4, sel ? -11901272 : BTN_OUTLINE);
                String ic = WaymarksConfig.ICONS[Math.min(Math.max(e.iconIndex, 0), WaymarksConfig.ICONS.length - 1)];
                context.drawText(this.textRenderer, ic, this.listLeft + 4, ry + 8, -1, false);
                String nm = e.name.isEmpty() ? "\u2014" : e.name;
                if (maxNameW > 8 && this.textRenderer.getWidth(nm) > maxNameW) {
                    nm = this.textRenderer.trimToWidth(nm, maxNameW) + "\u2026";
                }
                context.drawText(this.textRenderer, nm, textLeft, ry + 6, -1, false);
                String cr = String.format(java.util.Locale.ROOT, "%.0f %.0f %.0f", e.x, e.y, e.z);
                String crDisp = cr;
                if (maxNameW > 8 && this.textRenderer.getWidth(cr) > maxNameW) {
                    crDisp = this.textRenderer.trimToWidth(cr, maxNameW) + "\u2026";
                }
                context.drawText(this.textRenderer, crDisp, textLeft, ry + 18, 0xFFAAAAAA, false);
                int btnTop = ry + 9;
                int btnBottom = btnTop + 22;
                this.drawRoundedRect(context, btnLeft, btnTop, btnLeft + bw, btnBottom, -11709847);
                String cfgLabel = "\u041d\u0430\u0441\u0442\u0440\u043e\u0438\u0442\u044c";
                int tw = this.textRenderer.getWidth(cfgLabel);
                context.drawText(this.textRenderer, cfgLabel, btnLeft + (bw - tw) / 2, btnTop + 7, -1, false);
            }
        }

        super.render(context, mouseX, mouseY, deltaTicks);

        if (this.iconPickerOpen && this.selectedIndex >= 0 && this.selectedIndex < cfg.markers.size()) {
            this.drawIconPicker(context, mouseX, mouseY);
        }
    }

    private void drawIconPicker(DrawContext context, int mouseX, int mouseY) {
        int gx = this.iconGridLeft;
        int gy = this.iconGridTop;
        int gridW = ICON_COLS * ICON_STRIDE + 4;
        int gridH = 2 * ICON_STRIDE + 4;
        this.drawRoundedRect(context, gx - 4, gy - 4, gx + gridW, gy + gridH, -1426063360);
        this.drawRoundedOutline(context, gx - 4, gy - 4, gx + gridW, gy + gridH, PANEL_OUTLINE);
        for (int i = 0; i < WaymarksConfig.ICONS.length; i++) {
            int col = i % ICON_COLS;
            int row = i / ICON_COLS;
            int ix = gx + col * ICON_STRIDE;
            int iy = gy + row * ICON_STRIDE;
            boolean hover = mouseX >= ix && mouseX < ix + ICON_STRIDE - 2 && mouseY >= iy && mouseY < iy + ICON_STRIDE - 2;
            this.drawRoundedRect(context, ix, iy, ix + ICON_STRIDE - 2, iy + ICON_STRIDE - 2, hover ? -12040119 : BTN_BG);
            this.drawRoundedOutline(context, ix, iy, ix + ICON_STRIDE - 2, iy + ICON_STRIDE - 2, BTN_OUTLINE);
            String label = (i + 1) + "." + WaymarksConfig.ICONS[i];
            int lw = this.textRenderer.getWidth(label);
            context.drawText(this.textRenderer, label, ix + (ICON_STRIDE - 2 - lw) / 2, iy + 6, -1, false);
        }
    }

    private void drawToggleRow(DrawContext context, int x, int y, int w, String label, boolean on, Runnable flip) {
        this.drawRoundedRect(context, x, y, x + w, y + 16, BTN_BG);
        this.drawRoundedOutline(context, x, y, x + w, y + 16, BTN_OUTLINE);
        context.drawText(this.textRenderer, label, x + 6, y + 4, -1, false);
        int sw = 28;
        int sh = 10;
        int sx = x + w - sw - 5;
        int sy = y + (16 - sh) / 2;
        this.drawRoundedRect(context, sx, sy, sx + sw, sy + sh, on ? SWITCH_ON : SWITCH_OFF);
        int kx = on ? sx + sw - sh + 1 : sx + 1;
        this.drawRoundedRect(context, kx, sy + 1, kx + sh - 2, sy + sh - 1, SWITCH_KNOB);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.settingsView) {
                int pl = this.panelLeft();
                if (this.hitToggle(mouseX, mouseY, pl + 10, this.settingsTogglePlayerY, PANEL_W - 20)) {
                    WaymarksConfig cfg = WaymarksConfig.get();
                    cfg.placeAtPlayerPosition = !cfg.placeAtPlayerPosition;
                    WaymarksConfig.save();
                    return true;
                }
                if (this.hitToggle(mouseX, mouseY, pl + 10, this.settingsToggleDeathY, PANEL_W - 20)) {
                    WaymarksConfig cfg = WaymarksConfig.get();
                    cfg.deathMarkerEnabled = !cfg.deathMarkerEnabled;
                    WaymarksConfig.save();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            if (mouseX >= this.addBtnX && mouseX <= this.addBtnX + TOP_SQUARE && mouseY >= this.addBtnY && mouseY <= this.addBtnY + TOP_SQUARE) {
                this.addNewMarker();
                return true;
            }

            if (this.iconPickerOpen && this.selectedIndex >= 0) {
                int gx = this.iconGridLeft;
                int gy = this.iconGridTop;
                for (int i = 0; i < WaymarksConfig.ICONS.length; i++) {
                    int col = i % ICON_COLS;
                    int row = i / ICON_COLS;
                    int ix = gx + col * ICON_STRIDE;
                    int iy = gy + row * ICON_STRIDE;
                    if (mouseX >= ix && mouseX <= ix + ICON_STRIDE - 2 && mouseY >= iy && mouseY <= iy + ICON_STRIDE - 2) {
                        WaymarksConfig.WaymarkEntry en = WaymarksConfig.get().markers.get(this.selectedIndex);
                        en.iconIndex = i;
                        WaymarksConfig.save();
                        this.iconPickerOpen = false;
                        this.init();
                        return true;
                    }
                }
            }

            WaymarksConfig cfg = WaymarksConfig.get();
            List<WaymarksConfig.WaymarkEntry> markers = cfg.markers;
            if (mouseX >= this.listLeft && mouseX <= this.listLeft + this.listW
                    && mouseY >= this.listTop && mouseY <= this.listTop + this.listH) {
                int bw = 72;
                int btnLeft = this.listLeft + this.listW - bw - 6;
                for (int i = 0; i < markers.size(); i++) {
                    int ry = this.listTop + i * ROW_H - this.listScroll;
                    if (mouseY < ry || mouseY > ry + ROW_H - 4) {
                        continue;
                    }
                    int btnTop = ry + 9;
                    int btnBottom = btnTop + 22;
                    if (mouseX >= btnLeft && mouseX <= btnLeft + bw && mouseY >= btnTop && mouseY <= btnBottom) {
                        this.selectedIndex = i;
                        this.iconPickerOpen = false;
                        this.init();
                    } else if (mouseX < btnLeft - 2) {
                        this.selectedIndex = i;
                        this.init();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean hitToggle(double mx, double my, int x, int y, int w) {
        return mx >= x && mx <= x + w && my >= y && my <= y + 16;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.settingsView) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (mouseX >= this.listLeft && mouseX <= this.listLeft + this.listW
                && mouseY >= this.listTop && mouseY <= this.listTop + this.listH) {
            this.listScroll = Math.max(0, this.listScroll - (int) (verticalAmount * 12));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int panelLeft() {
        return this.width / 2 - PANEL_W / 2;
    }

    private int panelTop() {
        return this.height / 2 - PANEL_H / 2;
    }

    /**
     * Ровный «+» из прямоугольников (без шрифта — глиф «+» в MC часто визуально смещён).
     * {@code fill}: правый/нижний край не включительно.
     */
    private static void drawPlusCross(DrawContext context, int x, int y, int size, int color) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        int halfArm = size / 2 - 3;
        int t = 2;
        context.fill(cx - halfArm, cy - t / 2, cx + halfArm, cy - t / 2 + t, color);
        context.fill(cx - t / 2, cy - halfArm, cx - t / 2 + t, cy + halfArm, color);
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
