package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Простые клиентские "мировые партиклы" (без регистрации новых vanilla-particles).
 */
public final class WorldParticlesFeature {
    private static final Random RNG = new Random();
    private static final List<Particle> PARTICLES = new ArrayList<>();
    private static final int MAX_PARTICLES_TOTAL = 180;
    private static long lastWorldTick = Long.MIN_VALUE;
    private static int spawnCooldownTicks = 0;

    private WorldParticlesFeature() {
    }

    public static void render(MinecraftClient client, VertexConsumer lines, Matrix4f mat, Vec3d cam) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.worldParticlesEnabled || client.world == null || client.player == null) {
            PARTICLES.clear();
            lastWorldTick = Long.MIN_VALUE;
            return;
        }
        long now = client.world.getTime();
        if (lastWorldTick == Long.MIN_VALUE) {
            lastWorldTick = now;
        }
        long delta = Math.max(0L, Math.min(5L, now - lastWorldTick));
        for (long i = 0L; i < delta; i++) {
            tick(client);
        }
        lastWorldTick = now;

        float r = PVPUtils.CONFIG.worldParticlesR / 255.0f;
        float g = PVPUtils.CONFIG.worldParticlesG / 255.0f;
        float b = PVPUtils.CONFIG.worldParticlesB / 255.0f;
        int glyphType = PVPUtils.CONFIG.worldParticlesType;
        for (Particle p : PARTICLES) {
            float life = Math.max(1.0f, p.lifetimeTicks);
            float t = Math.max(0.0f, Math.min(1.0f, p.ageTicks / life));
            float fadeIn = smoothStep(Math.min(1.0f, t / 0.18f));
            float fadeOut = 1.0f - smoothStep(Math.max(0.0f, (t - 0.68f) / 0.32f));
            float alpha = Math.max(0.0f, fadeIn * fadeOut);
            drawGlyph(lines, mat, cam, p.pos.x, p.pos.y, p.pos.z, 0.218f, r, g, b, alpha, glyphType, p.angleRad, p.flipRad);
        }
    }

    private static void tick(MinecraftClient client) {
        float fallSpeed = PVPUtils.CONFIG.worldParticlesFallSpeed;
        boolean falling = PVPUtils.CONFIG.worldParticlesFalling;
        for (Iterator<Particle> it = PARTICLES.iterator(); it.hasNext(); ) {
            Particle p = it.next();
            if (falling) {
                double targetFall = -0.016 * fallSpeed;
                double vy = p.vel.y + (targetFall - p.vel.y) * 0.045; // ещё мягче
                p.vel = new Vec3d(p.vel.x * 0.985, vy, p.vel.z * 0.985);
            } else {
                p.vel = new Vec3d(p.vel.x * 0.992, p.vel.y * 0.992, p.vel.z * 0.992);
            }
            p.driftPhase += p.driftSpeed;
            double driftX = Math.cos(p.driftPhase) * 0.0012;
            double driftZ = Math.sin(p.driftPhase) * 0.0012;
            p.pos = p.pos.add(p.vel.x + driftX, p.vel.y, p.vel.z + driftZ);
            p.angleRad += p.angleSpeedRad;
            p.flipRad += p.flipSpeedRad;
            p.ageTicks += 1.0f;
            if (p.ageTicks >= p.lifetimeTicks) {
                it.remove();
            }
        }

        spawnCooldownTicks--;
        if (spawnCooldownTicks > 0) {
            return;
        }
        spawnCooldownTicks = 2;
        if (PARTICLES.size() >= MAX_PARTICLES_TOTAL) {
            return;
        }

        float lifeSeconds = PVPUtils.CONFIG.worldParticlesLifetimeSeconds;
        float lifetimeTicks = Math.max(20.0f, Math.min(200.0f, lifeSeconds * 20.0f));
        int chunks = Math.max(1, Math.min(4, PVPUtils.CONFIG.worldParticlesChunks));
        int centerChunkX = (client.player.getBlockX() >> 4);
        int centerChunkZ = (client.player.getBlockZ() >> 4);
        int startChunkX = centerChunkX - chunks / 2;
        int startChunkZ = centerChunkZ - chunks / 2;
        int cx = startChunkX + RNG.nextInt(chunks);
        int cz = startChunkZ + RNG.nextInt(chunks);
        int baseY = (client.player.getBlockY() >> 4) << 4; // 1 chunk по высоте (16 блоков)
        double sx = cx * 16.0 + randomRange(0.0, 16.0);
        double sy = baseY + randomRange(0.0, 16.0);
        double sz = cz * 16.0 + randomRange(0.0, 16.0);
        Vec3d vel;
        if (falling) {
            vel = new Vec3d(randomRange(-0.004, 0.004), randomRange(-0.004, -0.001) * fallSpeed, randomRange(-0.004, 0.004));
        } else {
            vel = new Vec3d(randomRange(-0.005, 0.005), randomRange(-0.001, 0.003), randomRange(-0.005, 0.005));
        }
        float spin = (float) randomRange(0.015, 0.045);
        if (RNG.nextBoolean()) {
            spin = -spin;
        }
        float flipSpeed = (float) randomRange(0.02, 0.06);
        if (RNG.nextBoolean()) {
            flipSpeed = -flipSpeed;
        }
        PARTICLES.add(new Particle(
                new Vec3d(sx, sy, sz),
                vel,
                lifetimeTicks,
                (float) randomRange(0.0, Math.PI * 2.0),
                spin,
                (float) randomRange(0.0, Math.PI * 2.0),
                flipSpeed,
                (float) randomRange(0.0, Math.PI * 2.0),
                (float) randomRange(0.02, 0.05)));
    }

    private static void drawGlyph(
            VertexConsumer vc,
            Matrix4f mat,
            Vec3d cam,
            double x,
            double y,
            double z,
            float size,
            float r,
            float g,
            float b,
            float a,
            int type,
            float angleRad,
            float flipRad) {
        float cx = (float) (x - cam.x);
        float cy = (float) (y - cam.y);
        float cz = (float) (z - cam.z);
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        float yScale = (float) Math.cos(flipRad); // плавный "переворот"
        switch (type) {
            case 1 -> drawSnowflake(vc, mat, cx, cy, cz, size, c, s, yScale, r, g, b, a);
            case 2 -> drawDollar(vc, mat, cx, cy, cz, size, c, s, yScale, r, g, b, a);
            default -> drawStar(vc, mat, cx, cy, cz, size, c, s, yScale, r, g, b, a);
        }
    }

    private static void drawStar(
            VertexConsumer vc, Matrix4f mat, float cx, float cy, float cz, float size,
            float c, float s, float yScale, float r, float g, float b, float a) {
        float[][] p = new float[5][2];
        for (int i = 0; i < 5; i++) {
            double ang = -Math.PI / 2.0 + i * (Math.PI * 2.0 / 5.0);
            p[i][0] = (float) (Math.cos(ang) * size);
            p[i][1] = (float) (Math.sin(ang) * size);
        }
        // Классическая 5-конечная звезда (пентаграмма)
        lineRot(vc, mat, cx, cy, cz, p[0][0], p[0][1], p[2][0], p[2][1], c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, p[2][0], p[2][1], p[4][0], p[4][1], c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, p[4][0], p[4][1], p[1][0], p[1][1], c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, p[1][0], p[1][1], p[3][0], p[3][1], c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, p[3][0], p[3][1], p[0][0], p[0][1], c, s, yScale, r, g, b, a);
    }

    private static void drawDollar(
            VertexConsumer vc, Matrix4f mat, float cx, float cy, float cz, float size,
            float c, float s, float yScale, float r, float g, float b, float a) {
        lineRot(vc, mat, cx, cy, cz, 0.0f, -size, 0.0f, size, c, s, yScale, r, g, b, a);
        float rx = size * 0.64f;
        float ry = size * 0.55f;
        lineRot(vc, mat, cx, cy, cz, -rx, ry, rx * 0.8f, ry, c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, -rx * 0.8f, 0.0f, rx, 0.0f, c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, -rx, -ry, rx * 0.8f, -ry, c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, -rx, ry, -rx * 0.55f, 0.0f, c, s, yScale, r, g, b, a);
        lineRot(vc, mat, cx, cy, cz, rx, 0.0f, rx * 0.55f, -ry, c, s, yScale, r, g, b, a);
    }

    private static void drawSnowflake(
            VertexConsumer vc,
            Matrix4f mat,
            float cx,
            float cy,
            float cz,
            float size,
            float c,
            float s,
            float yScale,
            float r,
            float g,
            float b,
            float a) {
        // 6 лучей снежинки
        drawSnowArm(vc, mat, cx, cy, cz, size, 1.0f, 0.0f, c, s, yScale, r, g, b, a);
        drawSnowArm(vc, mat, cx, cy, cz, size, -1.0f, 0.0f, c, s, yScale, r, g, b, a);
        drawSnowArm(vc, mat, cx, cy, cz, size, 0.5f, 0.866f, c, s, yScale, r, g, b, a);
        drawSnowArm(vc, mat, cx, cy, cz, size, -0.5f, -0.866f, c, s, yScale, r, g, b, a);
        drawSnowArm(vc, mat, cx, cy, cz, size, 0.5f, -0.866f, c, s, yScale, r, g, b, a);
        drawSnowArm(vc, mat, cx, cy, cz, size, -0.5f, 0.866f, c, s, yScale, r, g, b, a);
    }

    private static void drawSnowArm(
            VertexConsumer vc,
            Matrix4f mat,
            float cx,
            float cy,
            float cz,
            float size,
            float ax,
            float ay,
            float c,
            float s,
            float yScale,
            float r,
            float g,
            float b,
            float a) {
        float len = size;
        lineRot(vc, mat, cx, cy, cz, 0.0f, 0.0f, ax * len, ay * len, c, s, yScale, r, g, b, a);
        // "лапочки" на концах
        float px = -ay;
        float py = ax;
        float tipX = ax * len * 0.78f;
        float tipY = ay * len * 0.78f;
        float twig = size * 0.34f;
        lineRot(
                vc, mat, cx, cy, cz,
                tipX, tipY,
                tipX - ax * twig + px * twig * 0.8f,
                tipY - ay * twig + py * twig * 0.8f,
                c, s, yScale, r, g, b, a);
        lineRot(
                vc, mat, cx, cy, cz,
                tipX, tipY,
                tipX - ax * twig - px * twig * 0.8f,
                tipY - ay * twig - py * twig * 0.8f,
                c, s, yScale, r, g, b, a);
    }

    private static void lineRot(
            VertexConsumer vc,
            Matrix4f mat,
            float cx,
            float cy,
            float cz,
            float x0,
            float y0,
            float x1,
            float y1,
            float c,
            float s,
            float yScale,
            float r,
            float g,
            float b,
            float a) {
        float sx0 = x0;
        float sy0 = y0 * yScale;
        float sx1 = x1;
        float sy1 = y1 * yScale;
        float rx0 = sx0 * c - sy0 * s;
        float ry0 = sx0 * s + sy0 * c;
        float rx1 = sx1 * c - sy1 * s;
        float ry1 = sx1 * s + sy1 * c;
        line(vc, mat, cx + rx0, cy + ry0, cz, cx + rx1, cy + ry1, cz, r, g, b, a);
    }

    private static void line(
            VertexConsumer vc,
            Matrix4f mat,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float r,
            float g,
            float b,
            float a) {
        emitLine(vc, mat, x0, y0, z0, x1, y1, z1, r, g, b, a);
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0e-5f) {
            return;
        }
        float k = 0.016f;
        float ox = -(dy / len) * k;
        float oy = (dx / len) * k;
        emitLine(vc, mat, x0 + ox, y0 + oy, z0, x1 + ox, y1 + oy, z1, r, g, b, a);
        emitLine(vc, mat, x0 - ox, y0 - oy, z0, x1 - ox, y1 - oy, z1, r, g, b, a);
    }

    private static void emitLine(
            VertexConsumer vc,
            Matrix4f mat,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float r,
            float g,
            float b,
            float a) {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
    }

    private static double randomRange(double min, double max) {
        return min + RNG.nextDouble() * (max - min);
    }

    private static float smoothStep(float t) {
        float x = Math.max(0.0f, Math.min(1.0f, t));
        return x * x * (3.0f - 2.0f * x);
    }

    private static final class Particle {
        private Vec3d pos;
        private Vec3d vel;
        private float ageTicks;
        private final float lifetimeTicks;
        private float angleRad;
        private final float angleSpeedRad;
        private float flipRad;
        private final float flipSpeedRad;
        private float driftPhase;
        private final float driftSpeed;

        private Particle(
                Vec3d pos,
                Vec3d vel,
                float lifetimeTicks,
                float angleRad,
                float angleSpeedRad,
                float flipRad,
                float flipSpeedRad,
                float driftPhase,
                float driftSpeed) {
            this.pos = pos;
            this.vel = vel;
            this.lifetimeTicks = lifetimeTicks;
            this.angleRad = angleRad;
            this.angleSpeedRad = angleSpeedRad;
            this.flipRad = flipRad;
            this.flipSpeedRad = flipSpeedRad;
            this.driftPhase = driftPhase;
            this.driftSpeed = driftSpeed;
            this.ageTicks = 0.0f;
        }
    }
}
