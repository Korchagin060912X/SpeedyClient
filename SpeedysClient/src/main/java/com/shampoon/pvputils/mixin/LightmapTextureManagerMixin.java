/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.LightmapTextureManager
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.shampoon.pvputils.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LightmapTextureManager.class})
public abstract class LightmapTextureManagerMixin {
    @ModifyExpressionValue(method={"method_3313(F)V"}, at={@At(value="INVOKE", target="Ljava/lang/Math;max(FF)F", ordinal=0)})
    private float pvputils$allowNegativeGamma(float original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return original;
        }
        double g = (Double)client.options.getGamma().getValue();
        float gamma = (float)g;
        if (gamma < 0.0f) {
            return gamma;
        }
        return original;
    }

    @Inject(method={"method_42596(Lnet/minecraft/class_1309;FF)F"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$removeBlindnessDarknessVisualOnly(LivingEntity entity, float factor, float tickProgress, CallbackInfoReturnable<Float> cir) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.removeMiscEnabled || !PVPUtils.CONFIG.removeMiscHideBlindnessDarknessOverlay) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entity != client.player) {
            return;
        }
        if (entity.hasStatusEffect(StatusEffects.BLINDNESS) || entity.hasStatusEffect(StatusEffects.DARKNESS)) {
            cir.setReturnValue(Float.valueOf(0.0f));
        }
    }
}

