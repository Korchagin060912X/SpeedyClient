package com.shampoon.speedysclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.shampoon.speedysclient.features.CustomRatioFeature;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public abstract class WindowCustomRatioMixin {

    @ModifyReturnValue(method = "getFramebufferWidth", at = @At("RETURN"))
    private int speedys$customRatioFramebufferWidth(int original) {
        Window self = (Window) (Object) this;
        return CustomRatioFeature.applyToWidth(original, self.getFramebufferHeight());
    }

    @ModifyReturnValue(method = "getScaledWidth", at = @At("RETURN"))
    private int speedys$customRatioScaledWidth(int original) {
        Window self = (Window) (Object) this;
        return CustomRatioFeature.applyToWidth(original, self.getScaledHeight());
    }
}
