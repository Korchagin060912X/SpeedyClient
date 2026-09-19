package com.shampoon.speedysclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.render.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererCustomWorldMixin {

    @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
    private Vector4f speedys$customWorldFogColor(Vector4f original) {
        if (PVPUtils.CONFIG == null
                || !PVPUtils.CONFIG.customWorldEnabled
                || !PVPUtils.CONFIG.customWorldFogEnabled
                || original == null) {
            return original;
        }
        return new Vector4f(
                PVPUtils.CONFIG.customWorldFogR / 255.0f,
                PVPUtils.CONFIG.customWorldFogG / 255.0f,
                PVPUtils.CONFIG.customWorldFogB / 255.0f,
                original.w
        );
    }
}
