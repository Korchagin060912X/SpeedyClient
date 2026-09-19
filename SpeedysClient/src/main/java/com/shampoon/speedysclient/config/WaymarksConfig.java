package com.shampoon.speedysclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

/**
 * Пользовательские метки в мире + настройки «на месте игрока / смерти».
 */
public final class WaymarksConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("speedys_waymarks.json");

    public static final String[] ICONS = {"\u262f", "\u2604", "\u221e", "\u2717", "\u271d", "\u2605", "\u2623", "\u2740", "\u30c4", "\u2726"};

    public List<WaymarkEntry> markers = new ArrayList<>();
    /** Новая метка (+) подставляет координаты игрока. */
    public boolean placeAtPlayerPosition = true;
    /** При смерти — метка ✝ красная «Смерть» (старая такая же удаляется). */
    public boolean deathMarkerEnabled = true;

    private static WaymarksConfig instance = new WaymarksConfig();

    public static WaymarksConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            instance = new WaymarksConfig();
            save();
            return;
        }
        try {
            String json = Files.readString(PATH);
            WaymarksConfig c = GSON.fromJson(json, WaymarksConfig.class);
            instance = c != null ? c : new WaymarksConfig();
            if (instance.markers == null) {
                instance.markers = new ArrayList<>();
            }
            instance.normalize();
        } catch (Exception e) {
            instance = new WaymarksConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            instance.normalize();
            Files.writeString(PATH, GSON.toJson(instance));
        } catch (IOException ignored) {
        }
    }

    private void normalize() {
        if (markers == null) {
            markers = new ArrayList<>();
        }
        Iterator<WaymarkEntry> it = markers.iterator();
        while (it.hasNext()) {
            WaymarkEntry e = it.next();
            if (e == null || e.id == null || e.id.isBlank()) {
                it.remove();
                continue;
            }
            if (e.name == null) {
                e.name = "";
            }
            if (e.dimension == null) {
                e.dimension = "minecraft:overworld";
            }
            e.iconIndex = clamp(e.iconIndex, 0, ICONS.length - 1);
            e.r = clamp255(e.r);
            e.g = clamp255(e.g);
            e.b = clamp255(e.b);
        }
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static String currentDimensionId(MinecraftClient client) {
        if (client.world == null) {
            return "minecraft:overworld";
        }
        return client.world.getRegistryKey().getValue().toString();
    }

    public static void addDeathMarker(MinecraftClient client, double x, double y, double z, String dimensionId) {
        WaymarksConfig cfg = get();
        if (!cfg.deathMarkerEnabled) {
            return;
        }
        cfg.markers.removeIf(e -> e.deathMarker && dimensionId.equals(e.dimension));
        WaymarkEntry e = new WaymarkEntry();
        e.id = UUID.randomUUID().toString();
        e.name = "\u0421\u043c\u0435\u0440\u0442\u044c";
        e.x = x;
        e.y = y;
        e.z = z;
        e.dimension = dimensionId;
        e.iconIndex = 4;
        e.r = 220;
        e.g = 40;
        e.b = 40;
        e.deathMarker = true;
        cfg.markers.add(e);
        save();
    }

    public static final class WaymarkEntry {
        public String id = "";
        public String name = "";
        public double x;
        public double y;
        public double z;
        public String dimension = "minecraft:overworld";
        public int iconIndex;
        public int r = 40;
        public int g = 40;
        public int b = 55;
        public boolean deathMarker;
    }
}
