/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.sound.SoundManager
 *  net.minecraft.client.sound.TickableSoundInstance
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.features.SoundController;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.TickableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={SoundManager.class})
public abstract class SoundManagerMixin {
    @ModifyVariable(method={"method_4873(Lnet/minecraft/class_1113;)Lnet/minecraft/class_1140$class_11518;"}, at=@At(value="HEAD"), argsOnly=true)
    private SoundInstance pvputils$scaleVolumeOnPlay(SoundInstance sound) {
        return SoundController.wrapIfNeeded(sound);
    }

    @ModifyVariable(method={"method_4872(Lnet/minecraft/class_1113;I)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private SoundInstance pvputils$scaleVolumeOnDelayedPlay(SoundInstance sound) {
        return SoundController.wrapIfNeeded(sound);
    }

    @ModifyVariable(method={"method_22140(Lnet/minecraft/class_1117;)V"}, at=@At(value="HEAD"), argsOnly=true)
    private TickableSoundInstance pvputils$scaleVolumeOnNextTick(TickableSoundInstance sound) {
        return SoundController.wrapTickableIfNeeded(sound);
    }
}

