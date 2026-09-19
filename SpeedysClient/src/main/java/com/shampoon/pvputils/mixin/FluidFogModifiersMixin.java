/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.client.render.fog.FogData
 *  net.minecraft.client.render.fog.LavaFogModifier
 *  net.minecraft.client.render.fog.WaterFogModifier
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WaterFogModifier.class, LavaFogModifier.class})
public abstract class FluidFogModifiersMixin {
    private static final float PVPUTILS$FOG_CLEARED = Float.MAX_VALUE;

    @Inject(method={"method_42591(Lnet/minecraft/class_7285;Lnet/minecraft/class_1297;Lnet/minecraft/class_2338;Lnet/minecraft/class_638;FLnet/minecraft/class_9779;)V"}, at={@At(value="TAIL")})
    private void pvputils$noFluidPushFogAway(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
        float far;
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.noFluidEnabled || MinecraftClient.getInstance().player != cameraEntity) {
            return;
        }
        data.environmentalStart = far = Float.MAX_VALUE;
        data.environmentalEnd = far;
        data.renderDistanceStart = far;
        data.renderDistanceEnd = far;
        data.skyEnd = far;
        data.cloudEnd = far;
    }
}

