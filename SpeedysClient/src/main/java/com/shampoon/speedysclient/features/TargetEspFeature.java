/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import java.util.function.Consumer;

public final class TargetEspFeature {
    /** 5 секунд при 20 TPS */
    private static final int LIFETIME_TICKS = 100;

    public static int getLifetimeTicks() {
        return LIFETIME_TICKS;
    }

    /** Сколько тиков прошло с попадания (для анимации ESP), с учётом tickDelta. */
    public static float elapsedHitTicks(UUID entityId, float tickDelta) {
        int life = TargetEspFeature.getLife(entityId);
        if (life <= 0) {
            return LIFETIME_TICKS;
        }
        return (float) (LIFETIME_TICKS - life) + tickDelta;
    }
    private static final Map<UUID, Integer> hits = new HashMap<UUID, Integer>();

    private TargetEspFeature() {
    }

    public static void onHit(Entity entity) {
        hits.put(entity.getUuid(), LIFETIME_TICKS);
    }

    public static int getLife(UUID entityId) {
        return hits.getOrDefault(entityId, 0);
    }

    public static float alpha01(UUID entityId, float tickDelta) {
        int life = TargetEspFeature.getLife(entityId);
        if (life <= 0) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, ((float)life - tickDelta) / (float)LIFETIME_TICKS));
    }

    /** Все живые цели с активным ESP в радиусе от игрока (по хитбоксу сущности). */
    public static void forEachEspTarget(MinecraftClient client, float tickDelta, Consumer<LivingEntity> action) {
        if (client.world == null || client.player == null) {
            return;
        }
        Box box = client.player.getBoundingBox().expand(256.0);
        for (LivingEntity e : client.world.getEntitiesByClass(LivingEntity.class, box,
                le -> le != client.player && le.isAlive() && TargetEspFeature.alpha01(le.getUuid(), tickDelta) > 0.0f)) {
            action.accept(e);
        }
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, Integer>> it = hits.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            int life = e.getValue() - 1;
            if (life <= 0) {
                it.remove();
                continue;
            }
            e.setValue(life);
        }
    }

    /** Режим «Души»: песок под ногами + пылинки цвета Target ESP (или белые при 255/255/255). */
    public static void tickParticles(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.targetEspEnabled || PVPUtils.CONFIG.targetEspMode != 2) {
            return;
        }
        if (!(client.world instanceof ClientWorld) || client.player == null) {
            return;
        }
        ClientWorld world = (ClientWorld) client.world;
        ParticleEffect sandSteps = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState());
        int dustRgb = ColorHelper.getArgb(255,
                PVPUtils.CONFIG.targetEspR,
                PVPUtils.CONFIG.targetEspG,
                PVPUtils.CONFIG.targetEspB);
        DustParticleEffect dustTint = new DustParticleEffect(dustRgb, 1.08f);
        Box box = client.player.getBoundingBox().expand(256.0);
        for (LivingEntity p : world.getEntitiesByClass(LivingEntity.class, box,
                le -> le != client.player && le.isAlive() && TargetEspFeature.getLife(le.getUuid()) > 0)) {
            var random = world.random;
            double w = Math.max(0.2, p.getWidth());
            double h = Math.max(0.2, p.getHeight());
            for (int k = 0; k < 5; ++k) {
                double px = p.getX() + (random.nextDouble() - 0.5) * w * 1.35;
                double py = p.getY() + random.nextDouble() * h;
                double pz = p.getZ() + (random.nextDouble() - 0.5) * w * 1.35;
                double vx = (random.nextDouble() - 0.5) * 0.035;
                double vy = random.nextDouble() * 0.025;
                double vz = (random.nextDouble() - 0.5) * 0.035;
                if ((k & 1) == 0) {
                    client.particleManager.addParticle(sandSteps, px, py, pz, vx, vy, vz);
                } else {
                    client.particleManager.addParticle(dustTint, px, py, pz, vx * 0.6, vy * 0.5, vz * 0.6);
                }
            }
        }
    }
}

