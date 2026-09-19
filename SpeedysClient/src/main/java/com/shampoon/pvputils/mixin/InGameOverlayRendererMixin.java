/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.hud.InGameOverlayRenderer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.pvputils.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameOverlayRenderer.class})
public abstract class InGameOverlayRendererMixin {
    @Shadow
    private ItemStack field_59972;

    @Inject(method={"method_23070(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private static void pvputils$skipFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.removeMiscEnabled && PVPUtils.CONFIG.removeMiscHideFire) {
            ci.cancel();
        }
    }

    @Inject(method={"method_23069(Lnet/minecraft/class_310;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private static void pvputils$skipUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.noFluidEnabled) {
            ci.cancel();
        }
    }

    @Inject(method={"method_70939(Lnet/minecraft/class_4587;F)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pvputils$skipTotemFloatingItem(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.removeMiscEnabled || !PVPUtils.CONFIG.removeMiscHideTotemAnimation) {
            return;
        }
        ItemStack stack = this.field_59972;
        if (stack != null && !stack.isEmpty() && stack.isOf(Items.TOTEM_OF_UNDYING)) {
            ci.cancel();
        }
    }
}

