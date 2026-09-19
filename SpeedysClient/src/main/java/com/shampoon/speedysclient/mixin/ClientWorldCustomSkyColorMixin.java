package com.shampoon.speedysclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public abstract class ClientWorldCustomSkyColorMixin {

    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private int speedys$customWorldSkyColor(int original) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.customWorldEnabled) {
            return original;
        }
        int r = PVPUtils.CONFIG.customWorldSkyR & 255;
        int g = PVPUtils.CONFIG.customWorldSkyG & 255;
        int b = PVPUtils.CONFIG.customWorldSkyB & 255;
        return (r << 16) | (g << 8) | b;
    }
}
