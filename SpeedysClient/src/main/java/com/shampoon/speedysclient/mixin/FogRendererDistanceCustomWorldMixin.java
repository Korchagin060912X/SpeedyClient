package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FogRenderer.class)
public abstract class FogRendererDistanceCustomWorldMixin {

    private static boolean speedys$enabled() {
        return PVPUtils.CONFIG != null
                && PVPUtils.CONFIG.customWorldEnabled
                && PVPUtils.CONFIG.customWorldFogEnabled;
    }

    @ModifyVariable(method = "method_3211", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int speedys$overrideFogViewDistance(int original) {
        if (!speedys$enabled()) {
            return original;
        }
        // Делаем фог "сочнее" без новых настроек:
        // берём текущую дальность и сжимаем её, чтобы туман был плотнее и ближе.
        int baseChunks = Math.max(1, (int) Math.ceil(PVPUtils.CONFIG.customWorldFogDistanceBlocks / 16.0));
        int juicyChunks = Math.max(1, (int) Math.floor(baseChunks * 0.55));
        return Math.min(32, juicyChunks);
    }
}
