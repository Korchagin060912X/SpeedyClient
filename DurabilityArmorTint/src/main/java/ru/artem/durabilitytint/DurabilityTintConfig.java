/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package ru.artem.durabilitytint;

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

public final class DurabilityTintConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("durability_tint.json");
    private static Data data = new Data();

    private DurabilityTintConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH, new LinkOption[0])) {
            DurabilityTintConfig.save();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH);){
            Data loaded = (Data)GSON.fromJson((Reader)reader, Data.class);
            if (loaded != null) {
                data = loaded;
            }
        }
        catch (Exception ignored) {
            data = new Data();
            DurabilityTintConfig.save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, new OpenOption[0]);){
                GSON.toJson((Object)data, (Appendable)writer);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static boolean isEnabled() {
        return DurabilityTintConfig.data.enabled;
    }

    public static void setEnabled(boolean enabled) {
        DurabilityTintConfig.data.enabled = enabled;
    }

    public static float getStrength() {
        return DurabilityTintConfig.clamp(DurabilityTintConfig.data.strength, 0.0f, 1.0f);
    }

    public static void setStrength(float strength) {
        DurabilityTintConfig.data.strength = DurabilityTintConfig.clamp(strength, 0.0f, 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Data {
        boolean enabled = true;
        float strength = 0.5f;

        private Data() {
        }
    }
}

