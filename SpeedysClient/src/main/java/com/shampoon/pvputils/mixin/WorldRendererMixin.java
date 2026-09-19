/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.client.render.FrameGraphBuilder
 *  net.minecraft.client.render.WorldRenderer
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WorldRenderer.class})
public abstract class WorldRendererMixin {
    @Inject(method={"method_62203(Lnet/minecraft/class_9909;Lnet/minecraft/class_243;FLcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$skipPrecipitation(FrameGraphBuilder frameGraphBuilder, Vec3d cameraPos, float tickProgress, GpuBufferSlice fog, CallbackInfo ci) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.removeMiscEnabled && PVPUtils.CONFIG.removeMiscHidePrecipitation) {
            ci.cancel();
        }
    }
}

