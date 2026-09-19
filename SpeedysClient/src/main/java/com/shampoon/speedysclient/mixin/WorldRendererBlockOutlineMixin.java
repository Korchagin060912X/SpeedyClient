package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Отключает стандартную чёрную обводку блока под прицелом, если включен наш Block Overlay.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererBlockOutlineMixin {

    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), cancellable = true)
    private void speedys$hideVanillaBlockOutline(
            Camera camera,
            VertexConsumerProvider.Immediate vertexConsumers,
            MatrixStack matrices,
            boolean renderTranslucent,
            CallbackInfo ci) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.blockOverlayEnabled) {
            ci.cancel();
        }
    }
}
