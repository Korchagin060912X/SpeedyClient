/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package me.shampoon.attackindicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"attackindicator");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("attackindicator.json");
    private static ModConfig cached;
    public boolean enabled = true;
    public CrosshairColor color = CrosshairColor.RED;
    public float scale = 1.0f;
    /** −1 = подставить из {@link #color} при {@link #ensureCrosshairRgbFromEnum()}. */
    public int crosshairRgbR = -1;
    public int crosshairRgbG = -1;
    public int crosshairRgbB = -1;

    public static ModConfig getConfig() {
        if (cached == null) {
            ModConfig.load();
        }
        return cached;
    }

    public static ModConfig load() {
        if (!Files.exists(CONFIG_PATH, new LinkOption[0])) {
            cached = new ModConfig();
            cached.ensureCrosshairRgbFromEnum();
            cached.save();
            return cached;
        }
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH);) {
            ModConfig config = (ModConfig)GSON.fromJson((Reader)reader, ModConfig.class);
            if (config == null) {
                config = new ModConfig();
            }
            config.scale = ModConfig.clampScale(config.scale);
            if (config.color == null) {
                config.color = CrosshairColor.RED;
            }
            config.ensureCrosshairRgbFromEnum();
            cached = config;
            return cached;
        }
        catch (Exception e) {
            LOGGER.warn("Failed to load config, using defaults", (Throwable)e);
            cached = new ModConfig();
            cached.ensureCrosshairRgbFromEnum();
            cached.save();
            return cached;
        }
    }

    public void save() {
        try {
            this.ensureCrosshairRgbFromEnum();
            Files.createDirectories(CONFIG_PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, new OpenOption[0]);) {
                GSON.toJson((Object)this, (Appendable)writer);
            }
            cached = this;
        }
        catch (IOException e) {
            LOGGER.error("Failed to save config", (Throwable)e);
        }
    }

    public void ensureCrosshairRgbFromEnum() {
        if (this.crosshairRgbR < 0 || this.crosshairRgbG < 0 || this.crosshairRgbB < 0) {
            int rgb = this.color != null ? this.color.rgb : 0xFF0000;
            this.crosshairRgbR = rgb >> 16 & 255;
            this.crosshairRgbG = rgb >> 8 & 255;
            this.crosshairRgbB = rgb & 255;
        }
    }

    public void applyCrosshairPreset(CrosshairColor preset) {
        this.color = preset;
        int rgb = preset.rgb;
        this.crosshairRgbR = rgb >> 16 & 255;
        this.crosshairRgbG = rgb >> 8 & 255;
        this.crosshairRgbB = rgb & 255;
    }

    public int selectedColorRgb() {
        this.ensureCrosshairRgbFromEnum();
        return ModConfig.clamp255(this.crosshairRgbR) << 16
                | ModConfig.clamp255(this.crosshairRgbG) << 8
                | ModConfig.clamp255(this.crosshairRgbB);
    }

    public float effectiveScale() {
        return ModConfig.clampScale(this.scale);
    }

    private static float clampScale(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 1.0f;
        }
        return Math.max(0.5f, Math.min(3.0f, value));
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static enum CrosshairColor {
        RED(0xFF0000),
        GREEN(65280),
        YELLOW(0xFFFF00);

        private final int rgb;

        private CrosshairColor(int rgb) {
            this.rgb = rgb;
        }
    }
}
