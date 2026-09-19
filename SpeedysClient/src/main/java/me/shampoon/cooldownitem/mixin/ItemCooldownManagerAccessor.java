/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1796
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package me.shampoon.cooldownitem.mixin;

import java.util.Map;
import net.minecraft.class_1796;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1796.class})
public interface ItemCooldownManagerAccessor {
    @Accessor(value="field_8024")
    public Map<Object, Object> cooldownitem$getEntries();

    @Accessor(value="field_8025")
    public int cooldownitem$getTick();
}

