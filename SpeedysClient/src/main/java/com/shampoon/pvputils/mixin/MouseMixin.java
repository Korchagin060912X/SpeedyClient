/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.Mouse
 *  net.minecraft.client.option.KeyBinding
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Mouse.class})
public abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient field_1779;

    @Inject(method={"method_1598(JDD)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$adjustZoomWithScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.zoomEnabled) {
            return;
        }
        if (this.field_1779.currentScreen != null) {
            return;
        }
        KeyBinding key = PVPUtils.getZoomHoldKeyBinding();
        if (key == null || key.isUnbound() || !key.isPressed()) {
            return;
        }
        if (vertical == 0.0) {
            return;
        }
        int delta = vertical > 0.0 ? 1 : -1;
        PVPUtils.CONFIG.zoomLevel = Zoom.clampLevel(PVPUtils.CONFIG.zoomLevel + delta);
        PVPUtils.CONFIG.save();
        ci.cancel();
    }
}

