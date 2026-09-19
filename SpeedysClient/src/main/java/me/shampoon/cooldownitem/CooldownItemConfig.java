/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package me.shampoon.cooldownitem;

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

public final class CooldownItemConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("cooldownitem.json");
    public boolean enabled = true;
    /** 0 — только подписи секунд на слотах; 1 — панель HUD (список КД), подписи на слотах скрыты. */
    public int displayMode = 0;
    public int hudPanelOffsetX = 0;
    public int hudPanelOffsetY = 0;
    private static CooldownItemConfig instance = new CooldownItemConfig();

    public static final int DISPLAY_SECONDS = 0;
    public static final int DISPLAY_HUD_LIST = 1;

    public static CooldownItemConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(PATH, new LinkOption[0])) {
            instance = new CooldownItemConfig();
            CooldownItemConfig.save();
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(PATH);){
            CooldownItemConfig c = (CooldownItemConfig)GSON.fromJson((Reader)r, CooldownItemConfig.class);
            instance = c != null ? c : new CooldownItemConfig();
            instance.normalize();
        }
        catch (Exception e) {
            instance = new CooldownItemConfig();
        }
    }

    public void normalize() {
        if (this.displayMode != DISPLAY_SECONDS && this.displayMode != DISPLAY_HUD_LIST) {
            this.displayMode = DISPLAY_SECONDS;
        }
        this.hudPanelOffsetX = Math.max(-4000, Math.min(4000, this.hudPanelOffsetX));
        this.hudPanelOffsetY = Math.max(-4000, Math.min(4000, this.hudPanelOffsetY));
    }

    public static void save() {
        try {
            instance.normalize();
            Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter w = Files.newBufferedWriter(PATH, new OpenOption[0]);){
                GSON.toJson((Object)instance, (Appendable)w);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static boolean isFeatureEnabled() {
        return CooldownItemConfig.instance.enabled;
    }

    public static void setFeatureEnabled(boolean v) {
        CooldownItemConfig.instance.enabled = v;
        CooldownItemConfig.save();
    }

    public boolean isHudListMode() {
        return this.enabled && this.displayMode == DISPLAY_HUD_LIST;
    }
}

