/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.option.SimpleOption
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.shampoon.pvputils.mixin;

import com.mojang.serialization.Codec;
import com.shampoon.pvputils.features.FullBright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SimpleOption.class})
public abstract class SimpleOptionGammaMixin {
    private static boolean pvputils$isGammaOption(SimpleOption<?> self) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return false;
        }
        return client.options.getGamma() == self;
    }

    @Inject(method={"method_41753()Ljava/lang/Object;"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$fullBrightGetValue(CallbackInfoReturnable<Object> cir) {
        SimpleOption<?> self = (SimpleOption<?>)(Object)this;
        if (!SimpleOptionGammaMixin.pvputils$isGammaOption(self)) {
            return;
        }
        cir.setReturnValue(FullBright.getAppliedGammaDouble());
    }

    @Inject(method={"method_42404()Lcom/mojang/serialization/Codec;"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$fullBrightCodec(CallbackInfoReturnable<Codec<?>> cir) {
        SimpleOption<?> self = (SimpleOption<?>)(Object)this;
        if (!SimpleOptionGammaMixin.pvputils$isGammaOption(self)) {
            return;
        }
        cir.setReturnValue(Codec.DOUBLE);
    }
}

