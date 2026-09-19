/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package io.github.musicintegration.gsmtc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class MediaState {
    public static final MediaState EMPTY = new MediaState("", "", "", 0L, 0L, 0, "", 0.0f, System.currentTimeMillis());
    public final String title;
    public final String artist;
    public final String appUserModelId;
    public final long positionMs;
    public final long durationMs;
    public final int playbackState;
    public final String thumbBase64;
    public final float peak;
    public final long sampledAtEpochMs;

    public MediaState(String title, String artist, String appUserModelId, long positionMs, long durationMs, int playbackState, String thumbBase64, float peak, long sampledAtEpochMs) {
        this.title = title != null ? title : "";
        this.artist = artist != null ? artist : "";
        this.appUserModelId = appUserModelId != null ? appUserModelId : "";
        this.positionMs = Math.max(0L, positionMs);
        this.durationMs = Math.max(0L, durationMs);
        this.playbackState = playbackState;
        this.thumbBase64 = thumbBase64 != null ? thumbBase64 : "";
        this.peak = Math.max(0.0f, Math.min(1.0f, peak));
        this.sampledAtEpochMs = sampledAtEpochMs;
    }

    public boolean hasMedia() {
        return !this.title.isEmpty() || !this.artist.isEmpty();
    }

    public boolean isPlaying() {
        if (this.playbackState == 3) {
            return true;
        }
        boolean isBrowser = this.appUserModelId.toLowerCase().contains("chrome") || this.appUserModelId.toLowerCase().contains("firefox") || this.appUserModelId.toLowerCase().contains("msedge") || this.appUserModelId.toLowerCase().contains("opera") || this.appUserModelId.toLowerCase().contains("brave");
        return isBrowser && this.hasMedia() && this.playbackState != 4;
    }

    public long extrapolatedPositionMs() {
        if (!this.hasMedia()) {
            return 0L;
        }
        if (!this.isPlaying() || this.durationMs <= 0L) {
            return Math.min(this.durationMs, this.positionMs);
        }
        long delta = System.currentTimeMillis() - this.sampledAtEpochMs;
        return Math.min(this.durationMs, this.positionMs + delta);
    }

    public static MediaState parse(String line) {
        if (line == null || line.isBlank() || "{}".equals(line.trim())) {
            return EMPTY;
        }
        try {
            JsonObject o = JsonParser.parseString((String)line).getAsJsonObject();
            return MediaState.fromJsonObject(o, System.currentTimeMillis());
        }
        catch (Exception e) {
            return EMPTY;
        }
    }

    public static MediaState fromJsonObject(JsonObject o, long sampledAt) {
        if (o == null || o.entrySet().isEmpty()) {
            return EMPTY;
        }
        long pyTime = MediaState.getLong(o, "sampledAt");
        long usedAt = pyTime > 0L ? pyTime : sampledAt;
        return new MediaState(MediaState.getString(o, "title"), MediaState.getString(o, "artist"), MediaState.getString(o, "appId"), MediaState.getLong(o, "positionMs"), MediaState.getLong(o, "durationMs"), (int)MediaState.getLong(o, "state"), MediaState.getString(o, "thumb"), MediaState.getFloat(o, "peak"), usedAt);
    }

    private static String getString(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            return "";
        }
        return o.get(key).getAsString();
    }

    private static long getLong(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            return 0L;
        }
        try {
            return o.get(key).getAsLong();
        }
        catch (Exception e) {
            return 0L;
        }
    }

    private static float getFloat(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            return 0.0f;
        }
        try {
            return o.get(key).getAsFloat();
        }
        catch (Exception e) {
            return 0.0f;
        }
    }
}

