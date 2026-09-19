package com.shampoon.speedysclient.mixin;

import com.shampoon.speedysclient.features.BlockHoverSmoothFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * В 1.21+ прицел обновляется в {@link GameRenderer#updateCrosshairTarget} каждый кадр,
 * а не в {@link MinecraftClient#tick} — сглаживание должно быть здесь, иначе ванила перезаписывает.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererCrosshairSmoothMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
    private void speedys$smoothBlockCrosshair(float tickProgress, CallbackInfo ci) {
        this.client.crosshairTarget = BlockHoverSmoothFeature.smoothCrosshairTarget(this.client, this.client.crosshairTarget);
    }
}
