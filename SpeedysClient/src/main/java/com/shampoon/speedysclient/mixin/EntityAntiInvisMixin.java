/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.player.PlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public class EntityAntiInvisMixin {
    @Inject(method={"method_5756(Lnet/minecraft/class_1657;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void speedys$antiInvis(PlayerEntity viewer, CallbackInfoReturnable<Boolean> cir) {
        boolean hasHands;
        PlayerEntity target;
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.antiInvisEnabled) {
            return;
        }
        Entity self = (Entity)(Object)this;
        if (!(self instanceof PlayerEntity) || (target = (PlayerEntity)self) == viewer) {
            return;
        }
        if (!self.isInvisible()) {
            return;
        }
        boolean hasArmor = !target.getEquippedStack(EquipmentSlot.HEAD).isEmpty() || !target.getEquippedStack(EquipmentSlot.CHEST).isEmpty() || !target.getEquippedStack(EquipmentSlot.LEGS).isEmpty() || !target.getEquippedStack(EquipmentSlot.FEET).isEmpty();
        boolean bl = hasHands = !target.getMainHandStack().isEmpty() || !target.getOffHandStack().isEmpty();
        if (hasArmor || hasHands) {
            cir.setReturnValue(false);
        }
    }
}

