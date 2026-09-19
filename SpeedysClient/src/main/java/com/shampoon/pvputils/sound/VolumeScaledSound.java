/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.Sound
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.sound.SoundInstance$AttenuationType
 *  net.minecraft.client.sound.SoundManager
 *  net.minecraft.client.sound.WeightedSoundSet
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.util.Identifier
 */
package com.shampoon.pvputils.sound;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public final class VolumeScaledSound
implements SoundInstance {
    private final SoundInstance delegate;
    private final float volumeMultiplier;

    public VolumeScaledSound(SoundInstance delegate, float volumeMultiplier) {
        this.delegate = delegate;
        this.volumeMultiplier = volumeMultiplier;
    }

    public Identifier getId() {
        return this.delegate.getId();
    }

    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return this.delegate.getSoundSet(soundManager);
    }

    public Sound getSound() {
        return this.delegate.getSound();
    }

    public SoundCategory getCategory() {
        return this.delegate.getCategory();
    }

    public boolean isRepeatable() {
        return this.delegate.isRepeatable();
    }

    public int getRepeatDelay() {
        return this.delegate.getRepeatDelay();
    }

    public float getVolume() {
        return this.delegate.getVolume() * this.volumeMultiplier;
    }

    public float getPitch() {
        return this.delegate.getPitch();
    }

    public double getX() {
        return this.delegate.getX();
    }

    public double getY() {
        return this.delegate.getY();
    }

    public double getZ() {
        return this.delegate.getZ();
    }

    public SoundInstance.AttenuationType getAttenuationType() {
        return this.delegate.getAttenuationType();
    }

    public boolean isRelative() {
        return this.delegate.isRelative();
    }

    public boolean shouldAlwaysPlay() {
        return this.delegate.shouldAlwaysPlay();
    }

    public boolean canPlay() {
        return this.delegate.canPlay();
    }
}

