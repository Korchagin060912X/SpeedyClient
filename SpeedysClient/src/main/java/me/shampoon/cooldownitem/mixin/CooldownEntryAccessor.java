/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package me.shampoon.cooldownitem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/class_1796$class_1797"})
public interface CooldownEntryAccessor {
    @Accessor(value="comp_3083")
    public int cooldownitem$getStartTick();

    @Accessor(value="comp_3084")
    public int cooldownitem$getEndTick();
}

