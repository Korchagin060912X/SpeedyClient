/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.screen.slot.Slot
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package me.shampoon.cooldownitem.mixin;

import me.shampoon.cooldownitem.client.CooldownLabelRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenMixin {
    @Shadow
    protected int field_2776;
    @Shadow
    protected int field_2800;

    @Inject(method={"method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V"}, at={@At(value="RETURN")})
    private void cooldownitem$afterDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        int itemX = slot.x + this.field_2776;
        int itemY = slot.y + this.field_2800;
        CooldownLabelRenderer.render(context, slot.getStack(), itemX, itemY, (PlayerEntity)player);
    }
}

