package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererCustomHandMixin {

    @Unique
    private static final ThreadLocal<Float> SPEEDYS$ORIGINAL_SWING = ThreadLocal.withInitial(() -> 0.0f);

    @Invoker("applySwingOffset")
    protected abstract void speedys$vanillaSwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

    @Unique
    private static float speedys$smoothstep(float x) {
        float t = MathHelper.clamp(x, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    /**
     * Фаза удара короче, обратный ход дольше и сглажен (чтобы было заметно «замедление» возврата).
     */
    @Unique
    private static float speedys$swingOutAndBack(float swing01) {
        float t = MathHelper.clamp(swing01, 0.0f, 1.0f);
        float peakAt = 0.28f;
        if (t <= peakAt) {
            return speedys$smoothstep(t / peakAt);
        }
        float u = (t - peakAt) / (1.0f - peakAt);
        return speedys$smoothstep(1.0f - u * u);
    }

    @Unique
    private static boolean speedys$fixedMainHandPose() {
        return PVPUtils.CONFIG != null
                && PVPUtils.CONFIG.customHandEnabled
                && PVPUtils.CONFIG.customHandFixedPose;
    }

    @ModifyVariable(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private float speedys$lockPitchForFixedHand(float original, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (speedys$fixedMainHandPose() && hand == Hand.MAIN_HAND) {
            return 0.0f;
        }
        return original;
    }

    @ModifyVariable(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            ordinal = 3,
            argsOnly = true)
    private float speedys$lockEquipForFixedHand(float original, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (speedys$fixedMainHandPose() && hand == Hand.MAIN_HAND) {
            return 0.0f;
        }
        return original;
    }

    @ModifyVariable(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true)
    private float speedys$freezeVanillaSwingProgress(float original, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.customHandEnabled && PVPUtils.CONFIG.customHandSwingEnabled && hand == Hand.MAIN_HAND) {
            SPEEDYS$ORIGINAL_SWING.set(original);
            return 0.0f;
        }
        return original;
    }

    @Inject(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    shift = At.Shift.BEFORE))
    private void speedys$applyCustomHandBeforeItemDraw(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.customHandEnabled) {
            return;
        }
        if (hand != Hand.MAIN_HAND) {
            return;
        }
        Arm renderedArm = player.getMainArm();
        float sideSign = renderedArm == Arm.RIGHT ? 1.0f : -1.0f;
        matrices.translate(PVPUtils.CONFIG.customHandOffsetX * sideSign, PVPUtils.CONFIG.customHandOffsetY, PVPUtils.CONFIG.customHandOffsetZ);
        float scale = PVPUtils.CONFIG.customHandScale;
        matrices.scale(scale, scale, scale);
        if (PVPUtils.CONFIG.customHandPoseX != 0) {
            matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_X.rotationDegrees((float) PVPUtils.CONFIG.customHandPoseX * sideSign));
        }
        if (PVPUtils.CONFIG.customHandPoseY != 0) {
            matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Y.rotationDegrees((float) PVPUtils.CONFIG.customHandPoseY * sideSign));
        }
        if (PVPUtils.CONFIG.customHandPoseZ != 0) {
            matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Z.rotationDegrees((float) PVPUtils.CONFIG.customHandPoseZ * sideSign));
        }
        float rawSwing = PVPUtils.CONFIG.customHandSwingEnabled ? SPEEDYS$ORIGINAL_SWING.get() : swingProgress;
        float appliedSwing = PVPUtils.CONFIG.customHandSwingEnabled ? speedys$swingOutAndBack(rawSwing) : rawSwing;
        if (appliedSwing <= 0.0f) {
            return;
        }
        boolean customAxes = PVPUtils.CONFIG.customHandSwingX != 0
                || PVPUtils.CONFIG.customHandSwingY != 0
                || PVPUtils.CONFIG.customHandSwingZ != 0;
        if (PVPUtils.CONFIG.customHandSwingEnabled && !customAxes) {
            this.speedys$vanillaSwingOffset(matrices, renderedArm, appliedSwing);
            return;
        }
        if (PVPUtils.CONFIG.customHandSwingX != 0) {
            matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_X.rotationDegrees((float) PVPUtils.CONFIG.customHandSwingX * appliedSwing * sideSign));
        }
        if (PVPUtils.CONFIG.customHandSwingY != 0) {
            matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Y.rotationDegrees((float) PVPUtils.CONFIG.customHandSwingY * appliedSwing * sideSign));
        }
        if (PVPUtils.CONFIG.customHandSwingZ != 0) {
            matrices.multiply((Quaternionfc) RotationAxis.POSITIVE_Z.rotationDegrees((float) PVPUtils.CONFIG.customHandSwingZ * appliedSwing * sideSign));
        }
    }

    @Inject(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL"))
    private void speedys$clearSwingProgress(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        SPEEDYS$ORIGINAL_SWING.remove();
    }
}
