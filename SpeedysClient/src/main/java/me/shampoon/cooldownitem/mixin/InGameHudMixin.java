/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package me.shampoon.cooldownitem.mixin;

import me.shampoon.cooldownitem.client.CooldownLabelRenderer;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public abstract class InGameHudMixin {
    @Inject(method={"method_1762(Lnet/minecraft/class_332;IILnet/minecraft/class_9779;Lnet/minecraft/class_1657;Lnet/minecraft/class_1799;I)V"}, at={@At(value="RETURN")})
    private void cooldownitem$afterRenderHotbarItem(class_332 context, int x, int y, class_9779 tickCounter, class_1657 player, class_1799 stack, int seed, CallbackInfo ci) {
        if (player != null) {
            CooldownLabelRenderer.render(context, stack, x, y, player);
        }
    }
}

