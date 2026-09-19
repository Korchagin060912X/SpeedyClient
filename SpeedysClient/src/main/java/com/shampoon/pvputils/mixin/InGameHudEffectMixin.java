/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.EffectTimer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameHud.class})
public abstract class InGameHudEffectMixin {
    @Shadow
    private MinecraftClient field_2035;

    @Inject(method={"method_1765(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void speedys$skipVanillaStatusOverlayForListPanel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.effectTimerEnabled && this.field_2035.player != null) {
            ci.cancel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_1765(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}, at={@At(value="RETURN")})
    private void afterRenderStatusEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.field_2035.player == null) {
            return;
        }
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.effectTimerEnabled) {
            return;
        }
        if (PVPUtils.CONFIG.effectTimerListPanel) {
            return;
        }
        ArrayList<StatusEffectInstance> beneficial = new ArrayList<StatusEffectInstance>();
        ArrayList<StatusEffectInstance> other = new ArrayList<StatusEffectInstance>();
        for (StatusEffectInstance effect : this.field_2035.player.getStatusEffects()) {
            if (!effect.shouldShowIcon()) continue;
            if (((StatusEffect)effect.getEffectType().value()).isBeneficial()) {
                beneficial.add(effect);
                continue;
            }
            other.add(effect);
        }
        if (beneficial.isEmpty() && other.isEmpty()) {
            return;
        }
        int ox = PVPUtils.CONFIG.effectTimerHudOffsetX;
        int oy = PVPUtils.CONFIG.effectTimerHudOffsetY;
        int rightOrigin = context.getScaledWindowWidth() - EffectTimer.HUD_RIGHT_INSET + ox;
        EffectTimer.beginHudEffectFrame();
        try {
            if (!beneficial.isEmpty() && !other.isEmpty()) {
                InGameHudEffectMixin.renderEffectTimersForRow(context, beneficial, 1 + oy, rightOrigin);
                InGameHudEffectMixin.renderEffectTimersForRow(context, other, 26 + oy, rightOrigin);
            } else if (!beneficial.isEmpty()) {
                InGameHudEffectMixin.renderEffectTimersForRow(context, beneficial, 1 + oy, rightOrigin);
            } else {
                InGameHudEffectMixin.renderEffectTimersForRow(context, other, 26 + oy, rightOrigin);
            }
        }
        finally {
            EffectTimer.endHudEffectFrame();
        }
    }

    private static void renderEffectTimersForRow(DrawContext context, List<StatusEffectInstance> row, int y, int rightOriginX) {
        row.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());
        int alignRight = rightOriginX;
        for (StatusEffectInstance effect : row) {
            alignRight -= EffectTimer.renderEffectTimer(context, effect, alignRight, y);
        }
    }
}

