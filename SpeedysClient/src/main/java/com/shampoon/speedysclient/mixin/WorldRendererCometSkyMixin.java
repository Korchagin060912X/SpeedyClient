package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.CustomWorldCometsFeature;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererCometSkyMixin {

    @Inject(
            method = "renderClouds(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/option/CloudRenderMode;Lnet/minecraft/util/math/Vec3d;FIF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void speedys$skipVanillaCloudsForComets(
            FrameGraphBuilder frameGraphBuilder,
            CloudRenderMode mode,
            Vec3d cameraPos,
            float cloudPhase,
            int color,
            float cloudHeight,
            CallbackInfo ci) {
        if (PVPUtils.CONFIG != null
                && PVPUtils.CONFIG.customWorldEnabled
                && PVPUtils.CONFIG.customWorldSkyType == CustomWorldCometsFeature.SKY_COMETS) {
            ci.cancel();
        }
    }
}
