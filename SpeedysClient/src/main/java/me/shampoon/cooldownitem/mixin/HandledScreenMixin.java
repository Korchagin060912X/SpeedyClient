/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1735
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_465
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package me.shampoon.cooldownitem.mixin;

import me.shampoon.cooldownitem.client.CooldownLabelRenderer;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_465;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_465.class})
public abstract class HandledScreenMixin {
    @Shadow
    protected int field_2776;
    @Shadow
    protected int field_2800;

    @Inject(method={"method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V"}, at={@At(value="RETURN")})
    private void cooldownitem$afterDrawSlot(class_332 context, class_1735 slot, CallbackInfo ci) {
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) {
            return;
        }
        int itemX = slot.field_7873 + this.field_2776;
        int itemY = slot.field_7872 + this.field_2800;
        CooldownLabelRenderer.render(context, slot.method_7677(), itemX, itemY, (class_1657)player);
    }
}

