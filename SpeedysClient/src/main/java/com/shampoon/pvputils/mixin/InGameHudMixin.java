/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.ArmorHUD;
import com.shampoon.pvputils.features.ArmorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameHud.class})
public class InGameHudMixin {
    @Inject(method={"method_1735(Lnet/minecraft/class_332;Lnet/minecraft/class_1297;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$skipVignetteWhenHidingBlindness(DrawContext context, Entity entity, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.removeMiscEnabled || !PVPUtils.CONFIG.removeMiscHideBlindnessDarknessOverlay) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (entity != client.player || !(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)entity;
        if (living.hasStatusEffect(StatusEffects.BLINDNESS) || living.hasStatusEffect(StatusEffects.DARKNESS)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}, at={@At(value="TAIL")})
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        ArmorHelper.render(context, sw, sh);
        ArmorHUD.render(context, tickCounter.getTickProgress(false));
    }
}

