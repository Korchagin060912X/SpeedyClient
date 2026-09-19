/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.equipment.EquipmentModel$LayerType
 *  net.minecraft.client.render.entity.equipment.EquipmentRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.equipment.EquipmentAsset
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.util.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.artem.durabilitytint.mixin.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.artem.durabilitytint.DurabilityTintConfig;

@Mixin(value={EquipmentRenderer.class})
public abstract class EquipmentRendererMixin {
    @Unique
    private static final ThreadLocal<ItemStack> DURABILITY_TINT$CURRENT_STACK = new ThreadLocal();

    @Inject(method={"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_2960;)V"}, at={@At(value="HEAD")})
    private void durabilityTint$captureStack(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model model, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Identifier texture, CallbackInfo ci) {
        DURABILITY_TINT$CURRENT_STACK.set(stack);
    }

    @ModifyArg(method={"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_2960;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_3879;method_62100(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V"), index=4)
    private int durabilityTint$modifyArmorColor(int originalColor) {
        ItemStack stack = DURABILITY_TINT$CURRENT_STACK.get();
        if (!DurabilityTintConfig.isEnabled()) {
            return originalColor;
        }
        if (stack == null || stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return originalColor;
        }
        float remaining = 1.0f - (float)stack.getDamage() / (float)stack.getMaxDamage();
        int rgb = EquipmentRendererMixin.pickTintRgb(remaining);
        if (rgb == -1) {
            return originalColor;
        }
        return EquipmentRendererMixin.blendColor(originalColor, rgb, DurabilityTintConfig.getStrength());
    }

    @Inject(method={"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_2960;)V"}, at={@At(value="TAIL")})
    private void durabilityTint$clearStack(EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> assetKey, Model model, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Identifier texture, CallbackInfo ci) {
        DURABILITY_TINT$CURRENT_STACK.remove();
    }

    @Unique
    private static int pickTintRgb(float remaining) {
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

