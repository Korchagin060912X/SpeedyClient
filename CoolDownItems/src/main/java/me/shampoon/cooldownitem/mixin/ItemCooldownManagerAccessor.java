/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.ItemCooldownManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package me.shampoon.cooldownitem.mixin;

import java.util.Map;
import net.minecraft.entity.player.ItemCooldownManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemCooldownManager.class})
public interface ItemCooldownManagerAccessor {
    @Accessor(value="field_8024")
    public Map<Object, Object> cooldownitem$getEntries();

    @Accessor(value="field_8025")
    public int cooldownitem$getTick();
}

