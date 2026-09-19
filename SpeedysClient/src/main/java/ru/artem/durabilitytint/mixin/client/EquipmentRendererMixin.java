/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10186$class_10190
 *  net.minecraft.class_10197
 *  net.minecraft.class_10394
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_3879
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_5321
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.artem.durabilitytint.mixin.client;

import net.minecraft.class_10186;
import net.minecraft.class_10197;
import net.minecraft.class_10394;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5321;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.artem.durabilitytint.DurabilityTintConfig;

@Mixin(value={class_10197.class})
public abstract class EquipmentRendererMixin {
    @Unique
    private static final ThreadLocal<class_1799> DURABILITY_TINT$CURRENT_STACK = new ThreadLocal();

    @Inject(method={"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_2960;)V"}, at={@At(value="HEAD")})
    private void durabilityTint$captureStack(class_10186.class_10190 layerType, class_5321<class_10394> assetKey, class_3879 model, class_1799 stack, class_4587 matrices, class_4597 vertexConsumers, int light, class_2960 texture, CallbackInfo ci) {
        DURABILITY_TINT$CURRENT_STACK.set(stack);
    }

    @ModifyArg(method={"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_2960;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_3879;method_62100(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V"), index=4)
    private int durabilityTint$modifyArmorColor(int originalColor) {
        if (!DurabilityTintConfig.isEnabled()) {
            return originalColor;
        }
        class_1799 stack = DURABILITY_TINT$CURRENT_STACK.get();
        if (stack == null || stack.method_7960() || !stack.method_7963() || stack.method_7936() <= 0) {
            return originalColor;
        }
        float remaining = 1.0f - (float)stack.method_7919() / (float)stack.method_7936();
        int rgb = EquipmentRendererMixin.pickTintRgb(remaining);
        if (rgb == -1) {
            return originalColor;
        }
        return EquipmentRendererMixin.blendColor(originalColor, rgb, DurabilityTintConfig.getStrength());
    }

    @Inject(method={"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_2960;)V"}, at={@At(value="TAIL")})
    private void durabilityTint$clearStack(class_10186.class_10190 layerType, class_5321<class_10394> assetKey, class_3879 model, class_1799 stack, class_4587 matrices, class_4597 vertexConsumers, int light, class_2960 texture, CallbackInfo ci) {
        DURABILITY_TINT$CURRENT_STACK.remove();
    }

    @Unique
    private static int pickTintRgb(float remaining) {
        if (remaining < 0.15f) {
            return 0xFF3333;
        }
        if (remaining < 0.25f) {
            float t = (remaining - 0.15f) / 0.10f;
            return EquipmentRendererMixin.lerpRgb(0xFF3333, 0xFF66BF, t);
        }
        if (remaining < 0.5f) {
            float t = (remaining - 0.25f) / 0.25f;
            return EquipmentRendererMixin.lerpRgb(0xFF66BF, 0x8A4DFF, t);
        }
        if (remaining < 0.75f) {
            float t = (remaining - 0.5f) / 0.25f;
            return EquipmentRendererMixin.lerpRgb(0x8A4DFF, 0x2F5BFF, t);
        }
        return -1;
    }

    @Unique
    private static int lerpRgb(int a, int b, float t) {
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

    @Unique
    private static int blendColor(int originalArgb, int tintRgb, float strength) {
        int alpha = originalArgb >>> 24 & 0xFF;
        if (alpha == 0) {
            alpha = 255;
        }
        int originalR = originalArgb >>> 16 & 0xFF;
        int originalG = originalArgb >>> 8 & 0xFF;
        int originalB = originalArgb & 0xFF;
        int tintR = tintRgb >>> 16 & 0xFF;
        int tintG = tintRgb >>> 8 & 0xFF;
        int tintB = tintRgb & 0xFF;
        int outR = (int)((float)originalR + (float)(tintR - originalR) * strength);
        int outG = (int)((float)originalG + (float)(tintG - originalG) * strength);
        int outB = (int)((float)originalB + (float)(tintB - originalB) * strength);
        return alpha << 24 | outR << 16 | outG << 8 | outB;
    }
}

