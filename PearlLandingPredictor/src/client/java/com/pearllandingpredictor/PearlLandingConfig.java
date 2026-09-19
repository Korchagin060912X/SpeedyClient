/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.pearllandingpredictor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pearllandingpredictor.PearlTrajectoryRenderer;
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

public final class PearlLandingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pearl_landing_predictor.json");
    public boolean enabled = true;

    public static void load() {
        if (!Files.exists(PATH, new LinkOption[0])) {
            PearlLandingConfig.save(new PearlLandingConfig());
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(PATH);){
            PearlLandingConfig c = (PearlLandingConfig)GSON.fromJson((Reader)r, PearlLandingConfig.class);
            if (c != null) {
                PearlTrajectoryRenderer.setEnabled(c.enabled);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void saveFromState() {
        PearlLandingConfig c = new PearlLandingConfig();
        c.enabled = PearlTrajectoryRenderer.isEnabled();
        PearlLandingConfig.save(c);
    }

    private static void save(PearlLandingConfig c) {
        try {
            Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter w = Files.newBufferedWriter(PATH, new OpenOption[0]);){
                GSON.toJson((Object)c, (Appendable)w);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void setEnabledPersist(boolean enabled) {
        PearlTrajectoryRenderer.setEnabled(enabled);
        PearlLandingConfig c = new PearlLandingConfig();
        c.enabled = enabled;
        PearlLandingConfig.save(c);
    }
}

