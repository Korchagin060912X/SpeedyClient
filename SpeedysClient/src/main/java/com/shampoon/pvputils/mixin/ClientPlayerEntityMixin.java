/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.client.network.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.AutoSprint;
import com.shampoon.pvputils.features.FastXP;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayerEntity.class})
public class ClientPlayerEntityMixin {
    @ModifyReturnValue(method={"method_3140()F"}, at={@At(value="RETURN")})
    private float pvputils$noFluidFullWaterVisibility(float original) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.noFluidEnabled) {
            return 1.0f;
        }
        return original;
    }

    @Inject(method={"method_5773()V"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        AutoSprint.tick(player);
        FastXP.tick(player);
    }
}

