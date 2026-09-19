/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package io.github.musicintegration.config;

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

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("musicintegration.json");
    public int hudYOffset = 6;
    public int maxTitleWidth = 200;
    public boolean showSkipButtons = true;
    private static ModConfig instance = new ModConfig();

    public static ModConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.isRegularFile(PATH, new LinkOption[0])) {
            instance = new ModConfig();
            ModConfig.save();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(PATH);){
            ModConfig loaded = (ModConfig)GSON.fromJson((Reader)reader, ModConfig.class);
            instance = loaded != null ? loaded : new ModConfig();
        }
        catch (IOException e) {
            instance = new ModConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(PATH, new OpenOption[0]);){
                GSON.toJson((Object)instance, (Appendable)writer);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

