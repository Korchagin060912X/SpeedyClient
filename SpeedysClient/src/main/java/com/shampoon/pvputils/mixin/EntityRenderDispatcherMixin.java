/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.EntityRenderDispatcher
 *  net.minecraft.client.render.entity.state.EntityRenderState
 *  net.minecraft.client.util.math.MatrixStack
 *  org.joml.Quaternionf
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderDispatcher.class})
public abstract class EntityRenderDispatcherMixin {
    @Inject(method={"method_23165(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_10017;Lorg/joml/Quaternionf;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$skipEntityFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, CallbackInfo ci) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.removeMiscEnabled && PVPUtils.CONFIG.removeMiscHideFire) {
            ci.cancel();
        }
    }
}

