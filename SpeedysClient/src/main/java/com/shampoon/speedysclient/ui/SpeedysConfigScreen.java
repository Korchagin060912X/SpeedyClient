package com.shampoon.speedysclient.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.config.PVPUtilsConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * Экран конфигов как в legacy: панель, папка {@code speedys-configs} в game dir, список, «Поделиться».
 */
public final class SpeedysConfigScreen extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter CONFIG_DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
    /** Как в jar: {@code drawRoundedRect} / список. */
    private static final int BG = -805306368;
    private static final int OUTLINE = -14996918;
    private static final int LIST_SELECTED = -15063228;
    private static final int LIST_ROW = -15592419;
    private static final int LIST_OUTLINE = -14534042;
    private static final int HINT_COLOR = -6309633;
    private static final int META_COLOR = 0xFFAAAAAA;

    private static Path configDir() {
        return FabricLoader.getInstance().getGameDir().resolve("speedys-configs");
    }

    private final Screen parent;
    private TextFieldWidget nameField;
    private TextFieldWidget authorField;
    private final List<ConfigEntry> configEntries = new ArrayList<>();
    private int selected = -1;

    private record ConfigEntry(Path path, String stem, String author, long createdAtMs) {}

    public SpeedysConfigScreen(Screen parent) {
        super(Text.literal("Конфиги"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();
        int cx = this.width / 2;
        int top = this.height / 2 - 125;

        this.nameField = new TextFieldWidget(this.textRenderer, cx - 150, top + 24, 145, 16, Text.literal("name"));
        this.nameField.setPlaceholder(Text.literal("Название"));
        this.authorField = new TextFieldWidget(this.textRenderer, cx + 5, top + 24, 145, 16, Text.literal("author"));
        this.authorField.setPlaceholder(Text.literal("Автор (ник)"));
        this.addDrawableChild(this.nameField);
        this.addDrawableChild(this.authorField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Загрузить (папка)"), b -> this.openFolder())
                .dimensions(cx - 150, top + 48, 145, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Поделиться"), b -> this.shareSelected())
                .dimensions(cx + 5, top + 48, 145, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Сохранить новый конфиг"), b -> this.saveNewConfig())
                .dimensions(cx - 150, top + 250 - 42, 300, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> this.close())
                .dimensions(cx - 55, top + 250 - 22, 110, 16).build());

        this.reloadList();
    }

    private void reloadList() {
        this.configEntries.clear();
        try {
            Files.createDirectories(configDir());
            try (Stream<Path> stream = Files.list(configDir())) {
                stream.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().forEach(p -> this.configEntries.add(this.readEntry(p)));
            }
        } catch (IOException ignored) {
        }
        if (this.selected >= this.configEntries.size()) {
            this.selected = this.configEntries.isEmpty() ? -1 : 0;
        }
    }

    private ConfigEntry readEntry(Path p) {
        String stem = p.getFileName().toString().replace(".json", "");
        try {
            JsonObject jo = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            String author = "";
            if (jo.has("author") && jo.get("author").isJsonPrimitive()) {
                author = jo.get("author").getAsString();
            }
            long created = -1L;
            if (jo.has("createdAt") && jo.get("createdAt").isJsonPrimitive()) {
                try {
                    created = jo.get("createdAt").getAsLong();
                } catch (Exception ignored) {
                }
            }
            if (created <= 0L) {
                FileTime ft = Files.getLastModifiedTime(p);
                created = ft.toMillis();
            }
            return new ConfigEntry(p, stem, author == null ? "" : author.trim(), created);
        } catch (Exception e) {
            try {
                return new ConfigEntry(p, stem, "", Files.getLastModifiedTime(p).toMillis());
            } catch (IOException ex) {
                return new ConfigEntry(p, stem, "", System.currentTimeMillis());
            }
        }
    }

    private void saveNewConfig() {
        String name = sanitize(this.nameField.getText());
        if (name.isEmpty()) {
            name = "config_" + System.currentTimeMillis();
        }
        String author = this.authorField.getText().trim();
        StoredConfig stored = new StoredConfig();
        stored.name = name;
        stored.author = author;
        stored.createdAt = System.currentTimeMillis();
        stored.config = PVPUtils.CONFIG;
        try {
            Files.createDirectories(configDir());
            Path out = configDir().resolve(name + ".json");
            Files.writeString(out, GSON.toJson(stored));
            this.reloadList();
        } catch (IOException ignored) {
        }
    }

    private void shareSelected() {
        if (this.selected < 0 || this.selected >= this.configEntries.size()) {
            this.openFolder();
            return;
        }
        Util.getOperatingSystem().open(this.configEntries.get(this.selected).path.toFile());
    }

    private void openFolder() {
        try {
            Files.createDirectories(configDir());
        } catch (IOException ignored) {
        }
        Util.getOperatingSystem().open(configDir().toFile());
    }

    private void loadSelected() {
        if (this.selected < 0 || this.selected >= this.configEntries.size()) {
            return;
        }
        try {
            Path path = this.configEntries.get(this.selected).path;
            String raw = Files.readString(path);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            StoredConfig cfg = GSON.fromJson(raw, StoredConfig.class);
            if (cfg != null && cfg.config != null) {
                PVPUtils.CONFIG = cfg.config;
                if (root.has("config") && root.get("config").isJsonObject()) {
                    PVPUtilsConfig.applyMissingCometColorFields(PVPUtils.CONFIG, root.getAsJsonObject("config"));
                }
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                if (cfg.name != null) {
                    this.nameField.setText(cfg.name);
                }
                if (cfg.author != null) {
                    this.authorField.setText(cfg.author);
                }
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = this.width / 2;
        int t = this.height / 2 - 125;
        int listX = cx - 150;
        int listY = t + 84;
        int rowH = 16;
        for (int i = 0; i < this.configEntries.size() && i < 8; i++) {
            int y = listY + i * (rowH + 2);
            if (mouseX >= listX && mouseX <= listX + 300 && mouseY >= y && mouseY <= y + rowH) {
                this.selected = i;
                if (button == 0) {
                    this.loadSelected();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int l = this.width / 2 - 165;
        int t = this.height / 2 - 125;
        int r = this.width / 2 + 165;
        int b = this.height / 2 + 125;
        this.drawRoundedRect(context, l, t, r, b, BG);
        this.drawRoundedOutline(context, l, t, r, b, OUTLINE);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, t + 6, -1);
        context.drawTextWithShadow(this.textRenderer, "Клик по конфигу = загрузить", l + 10, t + 72, HINT_COLOR);
        int listY = t + 84;
        for (int i = 0; i < this.configEntries.size() && i < 8; i++) {
            int y = listY + i * 18;
            int bg = i == this.selected ? LIST_SELECTED : LIST_ROW;
            this.drawRoundedRect(context, l + 10, y, r - 10, y + 16, bg);
            this.drawRoundedOutline(context, l + 10, y, r - 10, y + 16, LIST_OUTLINE);
            ConfigEntry e = this.configEntries.get(i);
            String dateStr = CONFIG_DATE.format(Instant.ofEpochMilli(e.createdAtMs));
            String meta = (e.author.isEmpty() ? "\u2014" : e.author) + " \u00b7 " + dateStr;
            int metaW = this.textRenderer.getWidth(meta);
            int metaX = Math.max(l + 16, r - 16 - metaW);
            int maxStemW = metaX - (l + 16) - 6;
            String stemDraw = e.stem;
            if (maxStemW > 20) {
                while (stemDraw.length() > 1 && this.textRenderer.getWidth(stemDraw + "\u2026") > maxStemW) {
                    stemDraw = stemDraw.substring(0, stemDraw.length() - 1);
                }
                if (!stemDraw.equals(e.stem)) {
                    stemDraw = stemDraw + "\u2026";
                }
            }
            context.drawTextWithShadow(this.textRenderer, stemDraw, l + 16, y + 4, -1);
            context.drawTextWithShadow(this.textRenderer, meta, metaX, y + 4, META_COLOR);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private static String sanitize(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("[^a-zA-Z0-9_\\-\\u0430-\\u044f\\u0410-\\u042f]", "_");
    }

    private void drawRoundedRect(DrawContext context, int left, int top, int right, int bottom, int color) {
        context.fill(left + 3, top, right - 3, bottom, color);
        context.fill(left, top + 3, right, bottom - 3, color);
        context.fill(left + 1, top + 1, left + 3, top + 3, color);
        context.fill(right - 3, top + 1, right - 1, top + 3, color);
        context.fill(left + 1, bottom - 3, left + 3, bottom - 1, color);
        context.fill(right - 3, bottom - 3, right - 1, bottom - 1, color);
    }

    private void drawRoundedOutline(DrawContext context, int left, int top, int right, int bottom, int color) {
        context.fill(left + 3, top, right - 3, top + 1, color);
        context.fill(left + 3, bottom - 1, right - 3, bottom, color);
        context.fill(left, top + 3, left + 1, bottom - 3, color);
        context.fill(right - 1, top + 3, right, bottom - 3, color);
        context.fill(left + 1, top + 1, left + 3, top + 2, color);
        context.fill(right - 3, top + 1, right - 1, top + 2, color);
        context.fill(left + 1, bottom - 2, left + 3, bottom - 1, color);
        context.fill(right - 3, bottom - 2, right - 1, bottom - 1, color);
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

    private static class StoredConfig {
        String name;
        String author;
        Long createdAt;
        PVPUtilsConfig config;
    }
}
