package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Режим неба «кометы» для Custom World: без ванильных облаков; хвост и ядро с заливкой (DebugQuads), контур линиями.
 */
public final class CustomWorldCometsFeature {
    public static final int SKY_CLOUDS = 0;
    public static final int SKY_COMETS = 1;
    private static final int FADE_TICKS = 60;
    private static final int TICKS_PER_MINUTE = 1200;
    private static final int TRAIL_STEP = 2;
    private static final int TRAIL_MAX = 36;
    private static final Random RNG = new Random();

    private static int spawnCooldownTicks;
    private static final List<Comet> COMETS = new ArrayList<>();

    private CustomWorldCometsFeature() {
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || PVPUtils.CONFIG == null) {
            COMETS.clear();
            spawnCooldownTicks = 0;
            return;
        }
        if (!PVPUtils.CONFIG.customWorldEnabled || PVPUtils.CONFIG.customWorldSkyType != SKY_COMETS) {
            COMETS.clear();
            spawnCooldownTicks = 0;
            return;
        }
        int perMin = Math.max(1, PVPUtils.CONFIG.customWorldCometsPerMinute);
        int interval = Math.max(1, TICKS_PER_MINUTE / perMin);
        if (--spawnCooldownTicks <= 0) {
            spawnCooldownTicks = interval;
            spawnComet(client);
        }
        Iterator<Comet> it = COMETS.iterator();
        while (it.hasNext()) {
            Comet c = it.next();
            c.tick();
            if (c.dead) {
                it.remove();
            }
        }
    }

    public static void render(
            MinecraftClient client,
            VertexConsumer lines,
            VertexConsumer quads,
            Matrix4f mat,
            Vec3d cam,
            float tickDelta) {
        if (PVPUtils.CONFIG == null
                || !PVPUtils.CONFIG.customWorldEnabled
                || PVPUtils.CONFIG.customWorldSkyType != SKY_COMETS
                || client.player == null) {
            return;
        }
        int ir = PVPUtils.CONFIG.customWorldCometR;
        int ig = PVPUtils.CONFIG.customWorldCometG;
        int ib = PVPUtils.CONFIG.customWorldCometB;
        float cr = ir / 255.0f;
        float cg = ig / 255.0f;
        float cb = ib / 255.0f;
        float lineR = Math.min(1.0f, cr * 1.05f + 0.08f);
        float lineG = Math.min(1.0f, cg * 1.05f + 0.08f);
        float lineB = Math.min(1.0f, cb * 1.05f + 0.08f);
        for (Comet c : COMETS) {
            float fade = c.fadeAlpha();
            if (fade <= 0.02f) {
                continue;
            }
            float fillA = fade * 0.78f;
            float lineA = fade * 0.95f;
            Vec3d prevW = null;
            int idx = 0;
            int n = c.trail.size();
            for (Vec3d tp : c.trail) {
                float t = n <= 1 ? 1.0f : (float) idx / (float) (n - 1);
                float segA = fillA * (0.35f + 0.65f * t);
                if (prevW != null) {
                    double hw = 0.22 + 0.38 * t;
                    fillRibbon(quads, mat, cam, prevW, tp, hw, cr, cg, cb, segA);
                    drawLine(lines, mat, cam, prevW, tp, cr, cg, cb, lineA * (0.45f + 0.5f * t));
                }
                prevW = tp;
                idx++;
            }
            Vec3d head = c.displayPos(tickDelta);
            if (prevW != null) {
                fillRibbon(quads, mat, cam, prevW, head, 0.55, cr, cg, cb, fillA * 0.92f);
                drawLine(lines, mat, cam, prevW, head, cr, cg, cb, lineA);
            }
            Vec3d dir = c.vel.lengthSquared() > 1.0e-8 ? c.vel.normalize() : new Vec3d(0.0, 1.0, 0.0);
            if (c.headStar) {
                fillEllipsoid(quads, mat, cam, head, dir, 3.15, 3.15, 3.15, 7, 12, cr, cg, cb, fillA * 0.92f);
                drawStarHead(lines, mat, cam, head, lineR, lineG, lineB, lineA, 3.35);
            } else {
                fillEllipsoid(quads, mat, cam, head, dir, 3.45, 3.45, 0.62, 6, 14, cr, cg, cb, fillA * 0.9f);
                drawCometDiscHead(lines, mat, cam, head, dir, lineR, lineG, lineB, lineA, 3.55, 1.55);
            }
        }
    }

    private static void spawnComet(MinecraftClient client) {
        if (COMETS.size() >= 48) {
            return;
        }
        ClientPlayerEntity p = client.player;
        Vec3d eye = p.getEyePos();
        double yaw = RNG.nextDouble() * Math.PI * 2;
        double pitch = (RNG.nextDouble() - 0.5) * 0.55;
        double distH = 55.0 + RNG.nextDouble() * 75.0;
        double dx = Math.cos(yaw) * Math.cos(pitch) * distH;
        double dz = Math.sin(yaw) * Math.cos(pitch) * distH;
        double dy = 28.0 + RNG.nextDouble() * 55.0 + Math.sin(pitch) * distH * 0.35;
        Vec3d pos = eye.add(dx, dy, dz);

        Vec3d dir = new Vec3d(RNG.nextGaussian(), RNG.nextGaussian() * 0.22, RNG.nextGaussian()).normalize();
        double speed = 0.72 + RNG.nextDouble() * 1.35;
        Vec3d vel = dir.multiply(speed);

        int flightTicks = Math.max(120, PVPUtils.CONFIG.customWorldCometFlightSeconds * 20);
        boolean headStar = RNG.nextBoolean();
        COMETS.add(new Comet(pos, vel, flightTicks, headStar));
    }

    private static void addQuad(
            VertexConsumer vc,
            Matrix4f mat,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float r,
            float g,
            float b,
            float a) {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x3, y3, z3).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
    }

    private static void fillRibbon(
            VertexConsumer quads,
            Matrix4f mat,
            Vec3d cam,
            Vec3d w0,
            Vec3d w1,
            double halfW,
            float r,
            float g,
            float b,
            float a) {
        Vec3d seg = w1.subtract(w0);
        double len = seg.length();
        if (len < 1.0e-4) {
            return;
        }
        Vec3d t = seg.multiply(1.0 / len);
        Vec3d up = new Vec3d(0.0, 1.0, 0.0);
        Vec3d side = t.crossProduct(up);
        if (side.lengthSquared() < 1.0e-10) {
            side = t.crossProduct(new Vec3d(1.0, 0.0, 0.0));
        }
        side = side.normalize().multiply(halfW);
        Vec3d a0w = w0.subtract(side);
        Vec3d b0w = w0.add(side);
        Vec3d a1w = w1.subtract(side);
        Vec3d b1w = w1.add(side);
        float ax0 = (float) (a0w.x - cam.x);
        float ay0 = (float) (a0w.y - cam.y);
        float az0 = (float) (a0w.z - cam.z);
        float bx0 = (float) (b0w.x - cam.x);
        float by0 = (float) (b0w.y - cam.y);
        float bz0 = (float) (b0w.z - cam.z);
        float bx1 = (float) (b1w.x - cam.x);
        float by1 = (float) (b1w.y - cam.y);
        float bz1 = (float) (b1w.z - cam.z);
        float ax1 = (float) (a1w.x - cam.x);
        float ay1 = (float) (a1w.y - cam.y);
        float az1 = (float) (a1w.z - cam.z);
        addQuad(quads, mat, ax0, ay0, az0, bx0, by0, bz0, bx1, by1, bz1, ax1, ay1, az1, r, g, b, a);
    }

    private static void fillEllipsoid(
            VertexConsumer quads,
            Matrix4f mat,
            Vec3d cam,
            Vec3d center,
            Vec3d dir,
            double ru,
            double rv,
            double rd,
            int lat,
            int lon,
            float r,
            float g,
            float b,
            float a) {
        Vec3d d = dir.normalize();
        Vec3d aux = Math.abs(d.y) < 0.92 ? new Vec3d(0.0, 1.0, 0.0) : new Vec3d(1.0, 0.0, 0.0);
        Vec3d uu = d.crossProduct(aux);
        if (uu.lengthSquared() < 1.0e-10) {
            uu = d.crossProduct(new Vec3d(0.0, 0.0, 1.0));
        }
        uu = uu.normalize();
        Vec3d vv = d.crossProduct(uu).normalize();
        double ox = center.x - cam.x;
        double oy = center.y - cam.y;
        double oz = center.z - cam.z;
        for (int i = 0; i < lat; ++i) {
            double v0 = Math.PI * ((double) i / (double) lat - 0.5);
            double v1 = Math.PI * ((double) (i + 1) / (double) lat - 0.5);
            double sv0 = Math.sin(v0);
            double sv1 = Math.sin(v1);
            double cv0 = Math.cos(v0);
            double cv1 = Math.cos(v1);
            for (int j = 0; j < lon; ++j) {
                double u0 = Math.PI * 2 * (double) j / (double) lon;
                double u1 = Math.PI * 2 * (double) (j + 1) / (double) lon;
                Vec3d o00 =
                        uu.multiply(ru * cv0 * Math.cos(u0))
                                .add(vv.multiply(rv * cv0 * Math.sin(u0)))
                                .add(d.multiply(rd * sv0));
                Vec3d o01 =
                        uu.multiply(ru * cv0 * Math.cos(u1))
                                .add(vv.multiply(rv * cv0 * Math.sin(u1)))
                                .add(d.multiply(rd * sv0));
                Vec3d o11 =
                        uu.multiply(ru * cv1 * Math.cos(u1))
                                .add(vv.multiply(rv * cv1 * Math.sin(u1)))
                                .add(d.multiply(rd * sv1));
                Vec3d o10 =
                        uu.multiply(ru * cv1 * Math.cos(u0))
                                .add(vv.multiply(rv * cv1 * Math.sin(u0)))
                                .add(d.multiply(rd * sv1));
                float x00 = (float) (ox + o00.x);
                float y00 = (float) (oy + o00.y);
                float z00 = (float) (oz + o00.z);
                float x01 = (float) (ox + o01.x);
                float y01 = (float) (oy + o01.y);
                float z01 = (float) (oz + o01.z);
                float x11 = (float) (ox + o11.x);
                float y11 = (float) (oy + o11.y);
                float z11 = (float) (oz + o11.z);
                float x10 = (float) (ox + o10.x);
                float y10 = (float) (oy + o10.y);
                float z10 = (float) (oz + o10.z);
                addQuad(quads, mat, x00, y00, z00, x01, y01, z01, x11, y11, z11, x10, y10, z10, r, g, b, a);
            }
        }
    }

    private static void drawLine(
            VertexConsumer vc,
            Matrix4f mat,
            Vec3d cam,
            Vec3d a,
            Vec3d b,
            float r,
            float g,
            float bl,
            float alpha) {
        float ax = (float) (a.x - cam.x);
        float ay = (float) (a.y - cam.y);
        float az = (float) (a.z - cam.z);
        float bx = (float) (b.x - cam.x);
        float by = (float) (b.y - cam.y);
        float bz = (float) (b.z - cam.z);
        vc.vertex(mat, ax, ay, az).color(r, g, bl, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, bx, by, bz).color(r, g, bl, alpha).normal(0.0f, 1.0f, 0.0f);
    }

    private static void drawStarHead(
            VertexConsumer vc,
            Matrix4f mat,
            Vec3d cam,
            Vec3d center,
            float r,
            float g,
            float b,
            float alpha,
            double limb) {
        double cx = center.x;
        double cy = center.y;
        double cz = center.z;
        double s = limb;
        drawLine(vc, mat, cam, center, new Vec3d(cx + s, cy, cz), r, g, b, alpha);
        drawLine(vc, mat, cam, center, new Vec3d(cx - s, cy, cz), r, g, b, alpha);
        drawLine(vc, mat, cam, center, new Vec3d(cx, cy + s * 0.88, cz), r, g, b, alpha);
        drawLine(vc, mat, cam, center, new Vec3d(cx, cy - s * 0.88, cz), r, g, b, alpha);
        drawLine(vc, mat, cam, center, new Vec3d(cx, cy, cz + s), r, g, b, alpha);
        drawLine(vc, mat, cam, center, new Vec3d(cx, cy, cz - s), r, g, b, alpha);
        double d = s * 0.74;
        drawLine(
                vc,
                mat,
                cam,
                new Vec3d(cx + d, cy + d, cz + d),
                new Vec3d(cx - d, cy - d, cz - d),
                r,
                g,
                b,
                alpha * 0.78f);
        drawLine(
                vc,
                mat,
                cam,
                new Vec3d(cx + d, cy - d, cz + d),
                new Vec3d(cx - d, cy + d, cz - d),
                r,
                g,
                b,
                alpha * 0.78f);
    }

    private static void drawCometDiscHead(
            VertexConsumer vc,
            Matrix4f mat,
            Vec3d cam,
            Vec3d center,
            Vec3d velDir,
            float r,
            float g,
            float b,
            float alpha,
            double outerR,
            double innerR) {
        Vec3d dir = velDir.normalize();
        Vec3d aux = Math.abs(dir.y) < 0.92 ? new Vec3d(0.0, 1.0, 0.0) : new Vec3d(1.0, 0.0, 0.0);
        Vec3d u = dir.crossProduct(aux);
        if (u.lengthSquared() < 1.0e-8) {
            aux = new Vec3d(0.0, 0.0, 1.0);
            u = dir.crossProduct(aux);
        }
        u = u.normalize();
        Vec3d v = dir.crossProduct(u);
        drawDiscRing(vc, mat, cam, center, u, v, outerR, 26, r, g, b, alpha);
        drawDiscRing(vc, mat, cam, center, u, v, innerR, 20, r, g, b, alpha * 0.82f);
    }

    private static void drawDiscRing(
            VertexConsumer vc,
            Matrix4f mat,
            Vec3d cam,
            Vec3d center,
            Vec3d u,
            Vec3d v,
            double radius,
            int segs,
            float r,
            float g,
            float b,
            float a) {
        Vec3d prev =
                center
                        .add(u.multiply(Math.cos(-Math.PI * 2.0 / segs) * radius))
                        .add(v.multiply(Math.sin(-Math.PI * 2.0 / segs) * radius));
        for (int i = 0; i <= segs; i++) {
            double ang = Math.PI * 2.0 * (double) i / (double) segs;
            Vec3d p = center.add(u.multiply(Math.cos(ang) * radius)).add(v.multiply(Math.sin(ang) * radius));
            drawLine(vc, mat, cam, prev, p, r, g, b, a);
            prev = p;
        }
    }

    private static final class Comet {
        Vec3d pos;
        final Vec3d vel;
        final boolean headStar;
        int ageTicks;
        final int flightTicks;
        int fadeTick;
        boolean dead;
        final ArrayDeque<Vec3d> trail = new ArrayDeque<>();
        int trailCooldown;

        Comet(Vec3d pos, Vec3d vel, int flightTicks, boolean headStar) {
            this.pos = pos;
            this.vel = vel;
            this.flightTicks = flightTicks;
            this.headStar = headStar;
            this.trail.addLast(pos);
        }

        void tick() {
            if (fadeTick == 0) {
                pos = pos.add(vel);
                ageTicks++;
                if (--trailCooldown <= 0) {
                    trailCooldown = TRAIL_STEP;
                    trail.addLast(pos);
                    while (trail.size() > TRAIL_MAX) {
                        trail.removeFirst();
                    }
                }
                if (ageTicks >= flightTicks) {
                    fadeTick = 1;
                }
            } else {
                pos = pos.add(vel.multiply(0.28));
                fadeTick++;
                if (fadeTick > FADE_TICKS) {
                    dead = true;
                }
            }
        }

        float fadeAlpha() {
            if (fadeTick == 0) {
                return 1.0f;
            }
            return Math.max(0.0f, 1.0f - (float) fadeTick / (float) FADE_TICKS);
        }

        Vec3d displayPos(float tickDelta) {
            if (fadeTick == 0) {
                return pos.add(vel.multiply(tickDelta));
            }
            return pos.add(vel.multiply(0.28 * tickDelta));
        }
    }
}
