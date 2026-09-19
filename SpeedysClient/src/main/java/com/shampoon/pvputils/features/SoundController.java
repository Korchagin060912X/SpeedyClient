/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.Sound
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.sound.TickableSoundInstance
 *  net.minecraft.util.Identifier
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.sound.VolumeScaledSound;
import com.shampoon.pvputils.sound.VolumeScaledTickableSound;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.util.Identifier;

public final class SoundController {
    private static final float UNITY = 1.0f;
    private static final float EPS = 5.0E-4f;

    private SoundController() {
    }

    public static float clampUnit(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    public static float volumeMultiplierFor(Identifier soundId) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.soundControllerEnabled) {
            return 1.0f;
        }
        String path = soundId.getPath();
        if (path.equals("entity.experience_orb.pickup") || path.equals("random/orb") || path.equals("entity.experience_bottle.throw") || path.equals("entity.splash_potion.throw") || path.equals("entity.splash_potion.break")) {
            return PVPUtils.CONFIG.soundControllerExpOrbVolume;
        }
        if (path.startsWith("entity.wither.")) {
            return PVPUtils.CONFIG.soundControllerWitherVolume;
        }
        if (path.startsWith("item.trident.")) {
            return PVPUtils.CONFIG.soundControllerTridentVolume;
        }
        return 1.0f;
    }

    public static float volumeMultiplierForSoundInstance(SoundInstance sound) {
        Sound resolved;
        float m = SoundController.volumeMultiplierFor(sound.getId());
        if (Math.abs(m - 1.0f) >= 5.0E-4f) {
            return m;
        }
        try {
            resolved = sound.getSound();
        }
        catch (RuntimeException ignored) {
            return 1.0f;
        }
        if (resolved == null) {
            return 1.0f;
        }
        m = SoundController.volumeMultiplierFor(resolved.getLocation());
        if (Math.abs(m - 1.0f) >= 5.0E-4f) {
            return m;
        }
        m = SoundController.volumeMultiplierFor(resolved.getIdentifier());
        if (Math.abs(m - 1.0f) >= 5.0E-4f) {
            return m;
        }
        return 1.0f;
    }

    public static SoundInstance wrapIfNeeded(SoundInstance sound) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.soundControllerEnabled) {
            return sound;
        }
        if (sound instanceof VolumeScaledSound || sound instanceof VolumeScaledTickableSound) {
            return sound;
        }
        float m = SoundController.volumeMultiplierForSoundInstance(sound);
        if (Math.abs(m - 1.0f) < 5.0E-4f) {
            return sound;
        }
        if (sound instanceof TickableSoundInstance) {
            TickableSoundInstance tickable = (TickableSoundInstance)sound;
            return new VolumeScaledTickableSound(tickable, m);
        }
        return new VolumeScaledSound(sound, m);
    }

    public static TickableSoundInstance wrapTickableIfNeeded(TickableSoundInstance sound) {
        SoundInstance w = SoundController.wrapIfNeeded((SoundInstance)sound);
        if (w instanceof TickableSoundInstance) {
            TickableSoundInstance t = (TickableSoundInstance)w;
            return t;
        }
        return sound;
    }
}

