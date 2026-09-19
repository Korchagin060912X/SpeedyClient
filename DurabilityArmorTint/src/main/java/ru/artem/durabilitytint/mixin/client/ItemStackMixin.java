/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.artem.durabilitytint.mixin.client;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.artem.durabilitytint.DurabilityTintConfig;

@Mixin(value={ItemStack.class})
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
        ItemStack stack = (ItemStack)(Object)this;
        if (stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return;
        }
        float remaining = 1.0f - (float)this.method_7919() / (float)this.method_7936();
        int rgb = ItemStackMixin.durabilityTint$pickTintRgb(remaining);
        if (rgb != -1) {
            cir.setReturnValue(rgb);
        }
    }

    @Unique
    private static int durabilityTint$pickTintRgb(float remaining) {
        if (remaining < 0.1f) {
            return 0xFF3333;
        }
        if (remaining < 0.25f) {
            return 0xFF66BF;
        }
        if (remaining < 0.5f) {
            return 0x3F73FF;
        }
        if (remaining < 0.75f) {
            return 0x66E6FF;
        }
        return -1;
    }
}

