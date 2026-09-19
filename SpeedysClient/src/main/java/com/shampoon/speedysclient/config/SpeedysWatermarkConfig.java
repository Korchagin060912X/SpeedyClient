/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.shampoon.speedysclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;

public final class SpeedysWatermarkConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("speedys_watermark.json");
    public DisplayMode mode = DisplayMode.FPS_MS;
    /** Показывать полоску Speedys Client на HUD (вкл/выкл из меню Huds или настроек). */
    public boolean visible = true;
    public int offsetX = 0;
    public int offsetY = 8;
    private static SpeedysWatermarkConfig instance = new SpeedysWatermarkConfig();

    public static SpeedysWatermarkConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(PATH, new LinkOption[0])) {
            instance = new SpeedysWatermarkConfig();
            SpeedysWatermarkConfig.save();
            return;
        }
        try {
            String json = Files.readString(PATH);
            JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
            SpeedysWatermarkConfig c = GSON.fromJson(json, SpeedysWatermarkConfig.class);
            instance = c != null ? c : new SpeedysWatermarkConfig();
            if (instance.mode == null) {
                instance.mode = DisplayMode.FPS_MS;
            }
            if (!jo.has("visible")) {
                instance.visible = true;
            }
        }
        catch (Exception e) {
            instance = new SpeedysWatermarkConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter w = Files.newBufferedWriter(PATH, new OpenOption[0]);){
                GSON.toJson((Object)instance, (Appendable)w);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static enum DisplayMode {
        FPS_MS,
        MUSIC,
        ARMOR,
        EES_RADAR;

    }
}

