/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.shampoon.pvputils.PVPUtils
 *  com.shampoon.speedysclient.ui.SpeedysConfigScreen$StoredConfig
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 */
package com.shampoon.speedysclient.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.ui.SpeedysConfigScreen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class SpeedysConfigScreen
extends class_437 {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int BG = -805306368;
    private static final int OUTLINE = -14996918;
    private static final int W = 330;
    private static final int H = 250;
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getGameDir().resolve("speedys-configs");
    private final class_437 parent;
    private class_342 nameField;
    private class_342 authorField;
    private final List<Path> listed = new ArrayList<Path>();
    private int selected = -1;

    public SpeedysConfigScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"\u041a\u043e\u043d\u0444\u0438\u0433\u0438"));
        this.parent = parent;
    }

    protected void method_25426() {
        this.method_37067();
        int cx = this.field_22789 / 2;
        int top = this.field_22790 / 2 - 125;
        this.nameField = new class_342(this.field_22793, cx - 150, top + 24, 145, 16, (class_2561)class_2561.method_43470((String)"name"));
        this.nameField.method_47404((class_2561)class_2561.method_43470((String)"\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
        this.authorField = new class_342(this.field_22793, cx + 5, top + 24, 145, 16, (class_2561)class_2561.method_43470((String)"author"));
        this.authorField.method_47404((class_2561)class_2561.method_43470((String)"\u0410\u0432\u0442\u043e\u0440 (\u043d\u0438\u043a)"));
        this.method_37063((class_364)this.nameField);
        this.method_37063((class_364)this.authorField);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c (\u043f\u0430\u043f\u043a\u0430)"), b -> this.openFolder()).method_46434(cx - 150, top + 48, 145, 16).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f"), b -> this.shareSelected()).method_46434(cx + 5, top + 48, 145, 16).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043d\u043e\u0432\u044b\u0439 \u043a\u043e\u043d\u0444\u0438\u0433"), b -> this.saveNewConfig()).method_46434(cx - 150, top + 250 - 42, 300, 16).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u041d\u0430\u0437\u0430\u0434"), b -> this.method_25419()).method_46434(cx - 55, top + 250 - 22, 110, 16).method_46431());
        this.reloadList();
    }

    private void reloadList() {
        this.listed.clear();
        try {
            Files.createDirectories(CONFIG_DIR, new FileAttribute[0]);
            try (Stream<Path> stream = Files.list(CONFIG_DIR);){
                stream.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().forEach(this.listed::add);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (this.selected >= this.listed.size()) {
            this.selected = this.listed.isEmpty() ? -1 : 0;
        }
    }

    private void saveNewConfig() {
        Object name = SpeedysConfigScreen.sanitize(this.nameField.method_1882());
        if (((String)name).isEmpty()) {
            name = "config_" + System.currentTimeMillis();
        }
        String author = this.authorField.method_1882().trim();
        StoredConfig stored = new StoredConfig();
        stored.name = name;
        stored.author = author;
        stored.config = PVPUtils.CONFIG;
        try {
            Files.createDirectories(CONFIG_DIR, new FileAttribute[0]);
            Path out = CONFIG_DIR.resolve((String)name + ".json");
            Files.writeString(out, (CharSequence)GSON.toJson((Object)stored), new OpenOption[0]);
            this.reloadList();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void shareSelected() {
        if (this.selected < 0 || this.selected >= this.listed.size()) {
            this.openFolder();
            return;
        }
        class_156.method_668().method_672(this.listed.get(this.selected).toFile());
    }

    private void openFolder() {
        try {
            Files.createDirectories(CONFIG_DIR, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        class_156.method_668().method_672(CONFIG_DIR.toFile());
    }

    private void loadSelected() {
        if (this.selected < 0 || this.selected >= this.listed.size()) {
            return;
        }
        try {
            String raw = Files.readString(this.listed.get(this.selected));
            StoredConfig cfg = (StoredConfig)GSON.fromJson(raw, StoredConfig.class);
            if (cfg != null && cfg.config != null) {
                PVPUtils.CONFIG = cfg.config;
                PVPUtils.CONFIG.save();
                if (cfg.name != null) {
                    this.nameField.method_1852(cfg.name);
                }
                if (cfg.author != null) {
                    this.authorField.method_1852(cfg.author);
                }
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        int cx = this.field_22789 / 2;
        int top = this.field_22790 / 2 - 125;
        int listX = cx - 150;
        int listY = top + 72;
        int rowH = 16;
        for (int i = 0; i < this.listed.size() && i < 8; ++i) {
            int y = listY + i * (rowH + 2);
            if (!(mouseX >= (double)listX) || !(mouseX <= (double)(listX + 300)) || !(mouseY >= (double)y) || !(mouseY <= (double)(y + rowH))) continue;
            this.selected = i;
            if (button == 0) {
                this.loadSelected();
            }
            return true;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.method_25420(context, mouseX, mouseY, delta);
        int l = this.field_22789 / 2 - 165;
        int t = this.field_22790 / 2 - 125;
        int r = this.field_22789 / 2 + 165;
        int b = this.field_22790 / 2 + 125;
        this.drawRoundedRect(context, l, t, r, b, -805306368);
        this.drawRoundedOutline(context, l, t, r, b, -14996918);
        context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, t + 6, 0xFFFFFF);
        context.method_51433(this.field_22793, "\u041a\u043b\u0438\u043a \u043f\u043e \u043a\u043e\u043d\u0444\u0438\u0433\u0443 = \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c", l + 10, t + 72, -6309633, false);
        int listY = t + 84;
        for (int i = 0; i < this.listed.size() && i < 8; ++i) {
            int y = listY + i * 18;
            int bg = i == this.selected ? -15063228 : -15592419;
            this.drawRoundedRect(context, l + 10, y, r - 10, y + 16, bg);
            this.drawRoundedOutline(context, l + 10, y, r - 10, y + 16, -14534042);
            String file = this.listed.get(i).getFileName().toString().replace(".json", "");
            context.method_51433(this.field_22793, file, l + 16, y + 4, -1, false);
        }
        super.method_25394(context, mouseX, mouseY, delta);
    }

    private static String sanitize(String s) {
        return s == null ? "" : s.trim().replaceAll("[^a-zA-Z0-9_\\-\u0430-\u044f\u0410-\u042f]", "_");
    }

    private void drawRoundedRect(class_332 context, int l, int t, int r, int b, int color) {
        context.method_25294(l + 3, t, r - 3, b, color);
        context.method_25294(l, t + 3, r, b - 3, color);
        context.method_25294(l + 1, t + 1, l + 3, t + 3, color);
        context.method_25294(r - 3, t + 1, r - 1, t + 3, color);
        context.method_25294(l + 1, b - 3, l + 3, b - 1, color);
        context.method_25294(r - 3, b - 3, r - 1, b - 1, color);
    }

    private void drawRoundedOutline(class_332 context, int l, int t, int r, int b, int color) {
        context.method_25294(l + 3, t, r - 3, t + 1, color);
        context.method_25294(l + 3, b - 1, r - 3, b, color);
        context.method_25294(l, t + 3, l + 1, b - 3, color);
        context.method_25294(r - 1, t + 3, r, b - 3, color);
        context.method_25294(l + 1, t + 1, l + 3, t + 2, color);
        context.method_25294(r - 3, t + 1, r - 1, t + 2, color);
        context.method_25294(l + 1, b - 2, l + 3, b - 1, color);
        context.method_25294(r - 3, b - 2, r - 1, b - 1, color);
    }

    public void method_25419() {
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
        }
    }
}
