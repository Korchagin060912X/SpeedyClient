/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.shampoon.speedysclient.mixin;

import com.shampoon.speedysclient.features.SpeedysUtilsFeature;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientPlayerEntity.class})
public class ClientPlayerEntityDropMixin {
    @Inject(method={"method_7290(Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void speedys$lockSlots(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
        int selected = self.getInventory().getSelectedSlot();
        if (SpeedysUtilsFeature.isLockedHotbarSlot(selected)) {
            cir.setReturnValue(false);
        }
    }
}

