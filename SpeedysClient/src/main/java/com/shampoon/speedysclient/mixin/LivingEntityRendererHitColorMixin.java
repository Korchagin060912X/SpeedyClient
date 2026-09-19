package com.shampoon.speedysclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Заменяет красную вспышку урона на цвет из настроек (RGB mix + нейтральный overlay-текстура).
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererHitColorMixin {

    @ModifyReturnValue(method = "getMixColor", at = @At("RETURN"))
    private int speedys$hitMixColor(int original, LivingEntityRenderState state) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.hitColorEnabled) {
            return original;
        }
        if (!state.hurt) {
            return original;
        }
        int r = PVPUtils.CONFIG.hitColorR;
        int g = PVPUtils.CONFIG.hitColorG;
        int b = PVPUtils.CONFIG.hitColorB;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @ModifyReturnValue(method = "getOverlay", at = @At("RETURN"))
    private static int speedys$hitOverlay(int original, LivingEntityRenderState state, float tickDelta) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.hitColorEnabled) {
            return original;
        }
        if (!state.hurt) {
            return original;
        }
        // Hurt-flag must stay true, otherwise mix color is not applied in render path.
        return OverlayTexture.getUv(0.0F, true);
    }
}
