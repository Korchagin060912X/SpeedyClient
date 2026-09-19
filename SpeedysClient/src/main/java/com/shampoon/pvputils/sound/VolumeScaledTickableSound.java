/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.Sound
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.sound.SoundInstance$AttenuationType
 *  net.minecraft.client.sound.SoundManager
 *  net.minecraft.client.sound.TickableSoundInstance
 *  net.minecraft.client.sound.WeightedSoundSet
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.util.Identifier
 */
package com.shampoon.pvputils.sound;

import com.shampoon.pvputils.sound.VolumeScaledSound;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public final class VolumeScaledTickableSound
implements TickableSoundInstance {
    private final TickableSoundInstance delegate;
    private final VolumeScaledSound scaledView;

    public VolumeScaledTickableSound(TickableSoundInstance delegate, float volumeMultiplier) {
        this.delegate = delegate;
        this.scaledView = new VolumeScaledSound((SoundInstance)delegate, volumeMultiplier);
    }

    public void tick() {
        this.delegate.tick();
    }

    public boolean isDone() {
        return this.delegate.isDone();
    }

    public Identifier getId() {
        return this.scaledView.getId();
    }

    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return this.scaledView.getSoundSet(soundManager);
    }

    public Sound getSound() {
        return this.scaledView.getSound();
    }

    public SoundCategory getCategory() {
        return this.scaledView.getCategory();
    }

    public boolean isRepeatable() {
        return this.scaledView.isRepeatable();
    }

    public int getRepeatDelay() {
        return this.scaledView.getRepeatDelay();
    }

    public float getVolume() {
        return this.scaledView.getVolume();
    }

    public float getPitch() {
        return this.scaledView.getPitch();
    }

    public double getX() {
        return this.scaledView.getX();
    }

    public double getY() {
        return this.scaledView.getY();
    }

    public double getZ() {
        return this.scaledView.getZ();
    }

    public SoundInstance.AttenuationType getAttenuationType() {
        return this.scaledView.getAttenuationType();
    }

    public boolean isRelative() {
        return this.scaledView.isRelative();
    }

    public boolean shouldAlwaysPlay() {
        return this.scaledView.shouldAlwaysPlay();
    }

    public boolean canPlay() {
        return this.scaledView.canPlay();
    }
}

