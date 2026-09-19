/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.shampoon.pvputils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.shampoon.pvputils.features.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    @ModifyReturnValue(method={"method_3196(Lnet/minecraft/class_4184;FZ)F"}, at={@At(value="RETURN")})
    private float pvputils$applyHoldZoom(float original, Camera camera, float tickDelta, boolean changingFov) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Zoom.shouldApplyZoomFov(client)) {
            return original;
        }
        return original / Zoom.multiplier();
    }
}

