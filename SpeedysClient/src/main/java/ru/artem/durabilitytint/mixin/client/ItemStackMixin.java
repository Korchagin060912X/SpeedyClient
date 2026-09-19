/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.artem.durabilitytint.mixin.client;

import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.artem.durabilitytint.DurabilityTintConfig;

@Mixin(value={class_1799.class})
public abstract class ItemStackMixin {
    @Shadow
    public abstract int method_7919();

    @Shadow
    public abstract int method_7936();

    @Shadow
    public abstract boolean method_7963();

    @Inject(method={"method_31580()I"}, at={@At(value="RETURN")}, cancellable=true)
    private void durabilityTint$modifyItemBarColor(CallbackInfoReturnable<Integer> cir) {
        if (!DurabilityTintConfig.isEnabled()) {
            return;
        }
        class_1799 stack = (class_1799)this;
        if (stack.method_7960() || !stack.method_7963() || stack.method_7936() <= 0) {
            return;
        }
        float remaining = 1.0f - (float)this.method_7919() / (float)this.method_7936();
        int rgb = ItemStackMixin.durabilityTint$pickTintRgb(remaining);
        if (rgb != -1) {
            cir.setReturnValue((Object)rgb);
        }
    }

    @Unique
    private static int durabilityTint$pickTintRgb(float remaining) {
        if (remaining < 0.15f) {
            return 0xFF3333;
        }
        if (remaining < 0.25f) {
            float t = (remaining - 0.15f) / 0.10f;
            return ItemStackMixin.durabilityTint$lerpRgb(0xFF3333, 0xFF66BF, t);
        }
        if (remaining < 0.5f) {
            float t = (remaining - 0.25f) / 0.25f;
            return ItemStackMixin.durabilityTint$lerpRgb(0xFF66BF, 0x8A4DFF, t);
        }
        if (remaining < 0.75f) {
            float t = (remaining - 0.5f) / 0.25f;
            return ItemStackMixin.durabilityTint$lerpRgb(0x8A4DFF, 0x2F5BFF, t);
        }
        return -1;
    }

    @Unique
    private static int durabilityTint$lerpRgb(int a, int b, float t) {
        float k = Math.max(0.0f, Math.min(1.0f, t));
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        int r = (int)((float)ar + (float)(br - ar) * k);
        int g = (int)((float)ag + (float)(bg - ag) * k);
        int bCh = (int)((float)ab + (float)(bb - ab) * k);
        return r << 16 | g << 8 | bCh;
    }
}

