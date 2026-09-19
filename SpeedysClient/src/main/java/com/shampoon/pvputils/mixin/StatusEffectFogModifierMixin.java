/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.enums.CameraSubmersionType
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.fog.BlindnessEffectFogModifier
 *  net.minecraft.client.render.fog.DarknessEffectFogModifier
 *  net.minecraft.client.render.fog.StatusEffectFogModifier
 *  net.minecraft.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.fog.BlindnessEffectFogModifier;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={StatusEffectFogModifier.class})
public abstract class StatusEffectFogModifierMixin {
    @Inject(method={"method_42593(Lnet/minecraft/class_5636;Lnet/minecraft/class_1297;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$skipBlindnessDarknessFogForLocalPlayer(CameraSubmersionType cameraSubmersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (!(self instanceof BlindnessEffectFogModifier) && !(self instanceof DarknessEffectFogModifier)) {
            return;
        }
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.removeMiscEnabled && PVPUtils.CONFIG.removeMiscHideBlindnessDarknessOverlay && MinecraftClient.getInstance().player == cameraEntity) {
            cir.setReturnValue(false);
        }
    }
}

