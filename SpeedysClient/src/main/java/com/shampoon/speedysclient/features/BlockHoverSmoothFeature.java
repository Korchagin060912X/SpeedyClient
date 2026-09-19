package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Сглаживание попадания прицела по блокам (визуально): луч догоняет цель ванили.
 * Вызывается из {@link net.minecraft.client.render.GameRenderer#updateCrosshairTarget} (каждый кадр).
 */
public final class BlockHoverSmoothFeature {
    private static Vec3d smoothedHitPos;
    /** Доля сближения за один вызов (рендер кадр), не игровой тик. */
    private static final double LERP_PER_UPDATE = 0.22;

    private BlockHoverSmoothFeature() {
    }

    public static HitResult smoothCrosshairTarget(MinecraftClient client, HitResult raw) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.blockHoverSmooth || client.player == null || client.world == null) {
            smoothedHitPos = null;
            return raw;
        }
        if (raw == null || raw.getType() != HitResult.Type.BLOCK) {
            smoothedHitPos = null;
            return raw;
        }
        BlockHitResult blockRaw = (BlockHitResult) raw;
        Vec3d rawPos = blockRaw.getPos();
        PlayerEntity player = client.player;
        Vec3d eye = player.getEyePos();
        if (smoothedHitPos == null) {
            smoothedHitPos = rawPos;
        } else {
            smoothedHitPos = smoothedHitPos.lerp(rawPos, MathHelper.clamp(LERP_PER_UPDATE, 0.05, 1.0));
        }
        Vec3d dir = smoothedHitPos.subtract(eye);
        double len = dir.length();
        if (len < 1.0E-4) {
            return raw;
        }
        dir = dir.multiply(1.0 / len);
        double reach = player.getBlockInteractionRange();
        HitResult cast = client.world.raycast(new RaycastContext(eye, eye.add(dir.multiply(reach)),
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (cast.getType() == HitResult.Type.BLOCK) {
            return cast;
        }
        smoothedHitPos = rawPos;
        return raw;
    }
}
