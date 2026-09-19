/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.option.Perspective
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 */
package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.BlockOverlayFeature;
import com.shampoon.speedysclient.features.CustomHitboxesFeature;
import com.shampoon.speedysclient.features.FtHelperFeature;
import com.shampoon.speedysclient.features.SpeedysVisualsTick;
import com.shampoon.speedysclient.features.TargetEspFeature;
import com.shampoon.speedysclient.features.TrapTimerFeature;
import com.shampoon.speedysclient.features.WorldParticlesFeature;
import java.util.ArrayDeque;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class SpeedysVisualsRenderer {
    private static final float TRAIL_FADE_SCALE = 20.0f;
    private static final int[] GRADIENT_RGB = new int[3];

    private SpeedysVisualsRenderer() {
    }

    public static void render(WorldRenderContext context) {
        if (PVPUtils.CONFIG == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        float tickDelta = context.tickCounter().getTickProgress(false);
        Vec3d cam = context.camera().getPos();
        Matrix4f m = context.matrixStack().peek().getPositionMatrix();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) {
            return;
        }
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        VertexConsumer quads = consumers.getBuffer(RenderLayer.getDebugQuads());
        if (PVPUtils.CONFIG.blockOverlayEnabled) {
            BlockOverlayFeature.render(client, lines, quads, m, cam);
        }
        if (PVPUtils.CONFIG.customHitboxesEnabled) {
            CustomHitboxesFeature.render(client, lines, quads, m, context.camera(), tickDelta);
        }
        if (PVPUtils.CONFIG.trailEnabled && client.options.getPerspective() != Perspective.FIRST_PERSON) {
            VisualRgbGradient.sampleRgb(
                    System.currentTimeMillis(),
                    PVPUtils.CONFIG.trailR,
                    PVPUtils.CONFIG.trailG,
                    PVPUtils.CONFIG.trailB,
                    PVPUtils.CONFIG.trailR2,
                    PVPUtils.CONFIG.trailG2,
                    PVPUtils.CONFIG.trailB2,
                    PVPUtils.CONFIG.trailR3,
                    PVPUtils.CONFIG.trailG3,
                    PVPUtils.CONFIG.trailB3,
                    PVPUtils.CONFIG.trailColorStops,
                    PVPUtils.CONFIG.trailGradientAnim,
                    GRADIENT_RGB);
            SpeedysVisualsRenderer.drawTrailLineStrip(lines, quads, m, cam, GRADIENT_RGB[0], GRADIENT_RGB[1], GRADIENT_RGB[2]);
        }
        if (PVPUtils.CONFIG.chinaHatEnabled && client.options.getPerspective() != Perspective.FIRST_PERSON) {
            VisualRgbGradient.sampleRgb(
                    System.currentTimeMillis(),
                    PVPUtils.CONFIG.chinaHatR,
                    PVPUtils.CONFIG.chinaHatG,
                    PVPUtils.CONFIG.chinaHatB,
                    PVPUtils.CONFIG.chinaHatR2,
                    PVPUtils.CONFIG.chinaHatG2,
                    PVPUtils.CONFIG.chinaHatB2,
                    PVPUtils.CONFIG.chinaHatR3,
                    PVPUtils.CONFIG.chinaHatG3,
                    PVPUtils.CONFIG.chinaHatB3,
                    PVPUtils.CONFIG.chinaHatColorStops,
                    PVPUtils.CONFIG.chinaHatGradientAnim,
                    GRADIENT_RGB);
            SpeedysVisualsRenderer.drawChinaHat(client, lines, quads, m, cam, tickDelta, GRADIENT_RGB[0], GRADIENT_RGB[1], GRADIENT_RGB[2]);
        }
        if (PVPUtils.CONFIG.jumpCircleEnabled) {
            SpeedysVisualsRenderer.drawJumpRings(client, lines, m, cam, tickDelta, PVPUtils.CONFIG.jumpCircleR, PVPUtils.CONFIG.jumpCircleG, PVPUtils.CONFIG.jumpCircleB);
        }
        if (PVPUtils.CONFIG.targetEspEnabled) {
            SpeedysVisualsRenderer.drawTargetEsp(client, quads, m, cam, tickDelta, PVPUtils.CONFIG.targetEspR, PVPUtils.CONFIG.targetEspG, PVPUtils.CONFIG.targetEspB);
        }
        if (PVPUtils.CONFIG.nimbEnabled) {
            VisualRgbGradient.sampleRgb(
                    System.currentTimeMillis(),
                    PVPUtils.CONFIG.nimbR,
                    PVPUtils.CONFIG.nimbG,
                    PVPUtils.CONFIG.nimbB,
                    PVPUtils.CONFIG.nimbR2,
                    PVPUtils.CONFIG.nimbG2,
                    PVPUtils.CONFIG.nimbB2,
                    PVPUtils.CONFIG.nimbR3,
                    PVPUtils.CONFIG.nimbG3,
                    PVPUtils.CONFIG.nimbB3,
                    PVPUtils.CONFIG.nimbColorStops,
                    PVPUtils.CONFIG.nimbGradientAnim,
                    GRADIENT_RGB);
            SpeedysVisualsRenderer.drawNimb(client, lines, m, cam, tickDelta, GRADIENT_RGB[0], GRADIENT_RGB[1], GRADIENT_RGB[2]);
        }
        if (PVPUtils.CONFIG.worldParticlesEnabled) {
            WorldParticlesFeature.render(client, lines, m, cam);
        }
        if (PVPUtils.CONFIG.customWorldEnabled && PVPUtils.CONFIG.customWorldSkyType == CustomWorldCometsFeature.SKY_COMETS) {
            CustomWorldCometsFeature.render(client, lines, quads, m, cam, tickDelta);
        }
        if (PVPUtils.CONFIG.ftHelperEnabled) {
            SpeedysVisualsRenderer.drawFtHelper(client, lines, m, cam);
        }
        if (PVPUtils.CONFIG.trapTimerEnabled && TrapTimerFeature.isCountdownActive()
                && SpeedysVisualsRenderer.holdsNetheriteScrap(client.player)) {
            SpeedysVisualsRenderer.drawTrapZone(client, lines, m, cam, tickDelta);
        }
    }

    private static boolean holdsNetheriteScrap(PlayerEntity player) {
        return player.getMainHandStack().isOf(Items.NETHERITE_SCRAP)
                || player.getOffHandStack().isOf(Items.NETHERITE_SCRAP);
    }

    private static void drawTrailLineStrip(VertexConsumer vc, VertexConsumer fill, Matrix4f mat, Vec3d cam, int cr, int cg, int cb) {
        ArrayDeque<SpeedysVisualsTick.TrailPoint> pts = SpeedysVisualsTick.getTrailPoints();
        if (pts.size() < 2) {
            return;
        }
        float r = (float)cr / 255.0f;
        float g = (float)cg / 255.0f;
        float b = (float)cb / 255.0f;
        SpeedysVisualsTick.TrailPoint prev = null;
        for (SpeedysVisualsTick.TrailPoint pt : pts) {
            float yBottom = (float)(pt.y - cam.y);
            float yTop = yBottom + 1.78f;
            float x = (float)(pt.x - cam.x);
            float z = (float)(pt.z - cam.z);
            float selfA = Math.max(0.08f, 1.0f - (float)pt.age / 20.0f);
            SpeedysVisualsRenderer.drawMiniCircle(vc, mat, x, yBottom, z, 0.16f, 14, r, g, b, selfA);
            SpeedysVisualsRenderer.drawMiniCircle(vc, mat, x, yTop, z, 0.16f, 14, r, g, b, selfA);
            if (prev != null) {
                float a = Math.max(0.06f, 1.0f - (float)pt.age / 20.0f);
                Vec3d p0 = new Vec3d(prev.x, prev.y, prev.z).subtract(cam);
                Vec3d p1 = new Vec3d(pt.x, pt.y, pt.z).subtract(cam);
                SpeedysVisualsRenderer.drawSegmentColumn(vc, mat, (float)p0.x, (float)p0.y, (float)p0.z, (float)p1.x, (float)p1.y, (float)p1.z, 1.78f, r, g, b, a);
                SpeedysVisualsRenderer.drawFilledTrailSegment(fill, mat, (float)p0.x, (float)p0.y, (float)p0.z, (float)p1.x, (float)p1.y, (float)p1.z, 1.78f, 0.08f, r, g, b, a * 0.45f);
            }
            prev = pt;
        }
    }

    private static void drawChinaHat(MinecraftClient client, VertexConsumer vc, VertexConsumer fill, Matrix4f mat, Vec3d cam, float tickDelta, int cr, int cg, int cb) {
        ClientPlayerEntity p = client.player;
        Vec3d lerped = p.getLerpedPos(tickDelta);
        double hx = lerped.x;
        double hz = lerped.z;
        double hy = lerped.y + (double)p.getEyeHeight(p.getPose()) + 0.26;
        float r = (float)cr / 255.0f;
        float g = (float)cg / 255.0f;
        float b = (float)cb / 255.0f;
        float a = 0.9f;
        float baseR = 0.55f;
        float apexY = 0.42f;
        int segments = 24;
        float ax = (float)(hx - cam.x);
        float ay = (float)(hy - cam.y);
        float az = (float)(hz - cam.z);
        float topX = ax;
        float topY = ay + apexY;
        float topZ = az;
        int rings = 24;
        int radial = 36;
        for (int ring = 1; ring <= rings; ++ring) {
            float t = (float)ring / (float)rings;
            float y = topY - t * apexY;
            float rr = baseR * t;
            for (int i = 0; i < radial; ++i) {
                double t0 = Math.PI * 2 * (double)i / (double)radial;
                double t1 = Math.PI * 2 * (double)(i + 1) / (double)radial;
                float x0 = ax + rr * (float)Math.cos(t0);
                float z0 = az + rr * (float)Math.sin(t0);
                float x1 = ax + rr * (float)Math.cos(t1);
                float z1 = az + rr * (float)Math.sin(t1);
                vc.vertex(mat, x0, y, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
                vc.vertex(mat, x1, y, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
            }
        }
        for (int i = 0; i < radial; ++i) {
            double t0 = Math.PI * 2 * (double)i / (double)radial;
            float x = ax + baseR * (float)Math.cos(t0);
            float z = az + baseR * (float)Math.sin(t0);
            vc.vertex(mat, topX, topY, topZ).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(mat, x, ay, z).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        }
        SpeedysVisualsRenderer.drawFilledCone(fill, mat, ax, ay, az, topY, baseR, radial, r, g, b, 0.3f);
    }

    private static void drawJumpRings(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam, float tickDelta, int cr, int cg, int cb) {
        List<SpeedysVisualsTick.JumpRing> rings = SpeedysVisualsTick.getJumpRings();
        float fr = (float)cr / 255.0f;
        float fg = (float)cg / 255.0f;
        float fb = (float)cb / 255.0f;
        float lift = 0.14f;
        for (SpeedysVisualsTick.JumpRing ring : rings) {
            float age = (float)ring.ticks + tickDelta;
            if (age >= 22.0f) continue;
            float expand = Math.min(1.0f, age / 10.0f);
            double radius = 4.0 * (double)expand;
            float fade = Math.max(0.08f, 1.0f - age / 22.0f);
            if (PVPUtils.CONFIG.jumpCircleSpreadByBlocks) {
                SpeedysVisualsRenderer.drawBlockSpreadJumpRing(client, vc, mat, cam, ring.x, ring.z, ring.y + (double)lift, radius, fr, fg, fb, fade);
                continue;
            }
            int segs = 40;
            double cx = ring.x - cam.x;
            double cy = ring.y - cam.y + (double)lift;
            double cz = ring.z - cam.z;
            for (int i = 0; i < segs; ++i) {
                double u0 = Math.PI * 2 * (double)i / (double)segs;
                double u1 = Math.PI * 2 * (double)(i + 1) / (double)segs;
                float x0 = (float)(cx + radius * Math.cos(u0));
                float z0 = (float)(cz + radius * Math.sin(u0));
                float x1 = (float)(cx + radius * Math.cos(u1));
                float z1 = (float)(cz + radius * Math.sin(u1));
                vc.vertex(mat, x0, (float)cy, z0).color(fr, fg, fb, fade).normal(0.0f, 1.0f, 0.0f);
                vc.vertex(mat, x1, (float)cy, z1).color(fr, fg, fb, fade).normal(0.0f, 1.0f, 0.0f);
            }
        }
    }

    /**
     * Кольцо «по блокам»: каркас 1×1×1 для каждого блока в кольце (как wire box в block overlay), без заливки «сеткой» по центрам.
     */
    private static void drawBlockSpreadJumpRing(
            MinecraftClient client,
            VertexConsumer lines,
            Matrix4f mat,
            Vec3d cam,
            double feetX,
            double feetZ,
            double refY,
            double radius,
            float r,
            float g,
            float b,
            float a) {
        if (client.world == null) {
            return;
        }
        int bx0 = MathHelper.floor(feetX);
        int bz0 = MathHelper.floor(feetZ);
        int by = MathHelper.floor(refY) - 1;
        int ir = Math.max(1, (int)Math.round(radius));
        float ox = (float) -cam.x;
        float oy = (float) -cam.y;
        float oz = (float) -cam.z;
        BlockPos.Mutable mut = new BlockPos.Mutable();
        for (int dx = -ir; dx <= ir; ++dx) {
            for (int dz = -ir; dz <= ir; ++dz) {
                double d2 = dx * dx + dz * dz;
                if (d2 > radius * radius || d2 < (radius - 1.3) * (radius - 1.3)) {
                    continue;
                }
                int bx = bx0 + dx;
                int bz = bz0 + dz;
                mut.set(bx, by, bz);
                BlockState state = client.world.getBlockState(mut);
                if (state.isAir()) {
                    continue;
                }
                float x0 = (float) bx + ox;
                float y0 = (float) by + oy;
                float z0 = (float) bz + oz;
                float x1 = (float) (bx + 1) + ox;
                float y1 = (float) (by + 1) + oy;
                float z1 = (float) (bz + 1) + oz;
                BlockOverlayFeature.drawWireCube(lines, mat, x0, y0, z0, x1, y1, z1, r, g, b, a);
            }
        }
    }

    private static void drawSegmentColumn(VertexConsumer vc, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float height, float r, float g, float b, float a) {
        int slices = 8;
        for (int i = 0; i <= slices; ++i) {
            float t = (float)i / (float)slices;
            float ax = x0 + (x1 - x0) * t;
            float ay = y0 + (y1 - y0) * t;
            float az = z0 + (z1 - z0) * t;
            vc.vertex(mat, ax, ay, az).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(mat, ax, ay + height, az).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        }
    }

    private static void drawMiniCircle(VertexConsumer vc, Matrix4f mat, float cx, float cy, float cz, float rad, int segs, float r, float g, float b, float a) {
        for (int i = 0; i < segs; ++i) {
            double t0 = Math.PI * 2 * (double)i / (double)segs;
            double t1 = Math.PI * 2 * (double)(i + 1) / (double)segs;
            float x0 = cx + rad * (float)Math.cos(t0);
            float z0 = cz + rad * (float)Math.sin(t0);
            float x1 = cx + rad * (float)Math.cos(t1);
            float z1 = cz + rad * (float)Math.sin(t1);
            vc.vertex(mat, x0, cy, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(mat, x1, cy, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        }
    }

    private static void drawFilledTrailSegment(VertexConsumer vc, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float height, float width, float r, float g, float b, float a) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float len = (float)Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0E-4f) {
            return;
        }
        float nx = -dz / len * width;
        float nz = dx / len * width;
        float ax0 = x0 - nx;
        float az0 = z0 - nz;
        float bx0 = x0 + nx;
        float bz0 = z0 + nz;
        float ax1 = x1 - nx;
        float az1 = z1 - nz;
        float bx1 = x1 + nx;
        float bz1 = z1 + nz;
        SpeedysVisualsRenderer.addQuad(vc, mat, ax0, y0, az0, bx0, y0, bz0, bx1, y1, bz1, ax1, y1, az1, r, g, b, a);
        SpeedysVisualsRenderer.addQuad(vc, mat, ax0, y0 + height, az0, bx0, y0 + height, bz0, bx1, y1 + height, bz1, ax1, y1 + height, az1, r, g, b, a);
    }

    private static void drawFilledCone(VertexConsumer vc, Matrix4f mat, float baseX, float baseY, float baseZ, float topY, float baseR, int segs, float r, float g, float b, float a) {
        float topX = baseX;
        float topZ = baseZ;
        for (int i = 0; i < segs; ++i) {
            double t0 = Math.PI * 2 * (double)i / (double)segs;
            double t1 = Math.PI * 2 * (double)(i + 1) / (double)segs;
            float x0 = baseX + baseR * (float)Math.cos(t0);
            float z0 = baseZ + baseR * (float)Math.sin(t0);
            float x1 = baseX + baseR * (float)Math.cos(t1);
            float z1 = baseZ + baseR * (float)Math.sin(t1);
            SpeedysVisualsRenderer.addQuad(vc, mat, topX, topY, topZ, x0, baseY, z0, x1, baseY, z1, topX, topY, topZ, r, g, b, a);
            SpeedysVisualsRenderer.addQuad(vc, mat, baseX, baseY, baseZ, x0, baseY, z0, x1, baseY, z1, baseX, baseY, baseZ, r, g, b, a * 0.85f);
        }
    }

    private static void addQuad(VertexConsumer vc, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float b, float a) {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x3, y3, z3).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
    }

    private static final int TARGET_ESP_MODE_CIRCLE = 0;

    private static void drawTargetEsp(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam, float tickDelta, int cr, int cg, int cb) {
        int mode = PVPUtils.CONFIG.targetEspMode;
        if (mode == 2) {
            return;
        }
        if (mode == TARGET_ESP_MODE_CIRCLE) {
            SpeedysVisualsRenderer.drawTargetEspCircle(client, vc, mat, cam, tickDelta, cr, cg, cb);
            return;
        }
        SpeedysVisualsRenderer.drawTargetEspSpheres(client, vc, mat, cam, tickDelta, cr, cg, cb);
    }

    /** Радиус горизонтального кольца чуть больше половины ширины хитбокса. */
    private static double targetEspRingRadius(LivingEntity e) {
        double half = e.getWidth() * 0.5;
        return Math.max(0.22, half + 0.06);
    }

    private static double targetEspMiniSphereRadius(LivingEntity e) {
        double half = e.getWidth() * 0.5;
        return Math.min(0.22, Math.max(0.05, half * 0.17 + 0.045));
    }

    /** Горизонтальный круг: быстрее качание (~38 тиков на полный цикл), ярче — три концентрических кольца. */
    private static void drawTargetEspCircle(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam, float tickDelta, int cr, int cg, int cb) {
        TargetEspFeature.forEachEspTarget(client, tickDelta, p -> {
            float alpha = TargetEspFeature.alpha01(p.getUuid(), tickDelta);
            Vec3d pos = p.getLerpedPos(tickDelta);
            double centerX = pos.x;
            double centerZ = pos.z;
            float visibility = Math.min(1.0f, alpha);
            double period = 38.0;
            double phase = Math.PI * 2 * (((double) client.world.getTime() + (double) tickDelta) % period) / period;
            double y = pos.y + (0.5 + 0.5 * Math.sin(phase)) * (double) p.getHeight();
            double ringR = SpeedysVisualsRenderer.targetEspRingRadius(p);
            int segs = Math.min(64, Math.max(32, (int) (28 + ringR * 26)));
            float a0 = Math.max(0.18f, visibility);
            float a1 = Math.max(0.14f, visibility * 0.88f);
            float a2 = Math.max(0.1f, visibility * 0.72f);
            SpeedysVisualsRenderer.drawCircle(vc, mat, cam, centerX, y, centerZ, ringR, segs, cr, cg, cb, a0);
            SpeedysVisualsRenderer.drawCircle(vc, mat, cam, centerX, y + 0.012, centerZ, ringR + 0.045, segs, cr, cg, cb, a1);
            SpeedysVisualsRenderer.drawCircle(vc, mat, cam, centerX, y + 0.024, centerZ, ringR + 0.09, segs, cr, cg, cb, a2);
        });
    }

    /**
     * Шесть сфер по граням хитбокса; вращение вокруг вертикальной оси: 2.5 с «влево», 2.5 с «вправо», затем затухание.
     */
    private static void drawTargetEspSpheres(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam, float tickDelta, int cr, int cg, int cb) {
        TargetEspFeature.forEachEspTarget(client, tickDelta, p -> {
            float alpha = TargetEspFeature.alpha01(p.getUuid(), tickDelta);
            Vec3d pos = p.getLerpedPos(tickDelta);
            float visibility = Math.min(1.0f, alpha);
            double h = p.getHeight();
            double w = p.getWidth();
            double half = w * 0.5;
            double pad = 0.052;
            double cx = pos.x;
            double cz = pos.z;
            double cy0 = pos.y + h * 0.5;
            double dySide = -0.08 * h;
            double miniR = SpeedysVisualsRenderer.targetEspMiniSphereRadius(p) * 0.88;
            float e = TargetEspFeature.elapsedHitTicks(p.getUuid(), tickDelta);
            final float halfCycle = 50.0f;
            final float omega = (float) (Math.PI * 2.0 / (double) halfCycle);
            float theta = e < halfCycle ? -omega * e : -omega * halfCycle + omega * (e - halfCycle);
            double cosT = Math.cos(theta);
            double sinT = Math.sin(theta);
            float lifeMax = (float) TargetEspFeature.getLifetimeTicks();
            float fadeEnd = e <= lifeMax * 0.72f ? 1.0f : Math.max(0.0f, (lifeMax - e) / (lifeMax * 0.28f));
            float a = Math.max(0.06f, visibility * 0.94f * fadeEnd);
            double[][] local = new double[][]{
                    {0.0, h * 0.5 + pad, 0.0},
                    {0.0, -(h * 0.5 + pad * 0.85), 0.0},
                    {-(half + pad), dySide, 0.0},
                    {half + pad, dySide, 0.0},
                    {0.0, dySide, -(half + pad)},
                    {0.0, dySide, half + pad}
            };
            for (double[] d : local) {
                double lx = d[0];
                double ly = d[1];
                double lz = d[2];
                double rx = lx * cosT - lz * sinT;
                double rz = lx * sinT + lz * cosT;
                SpeedysVisualsRenderer.drawMiniSphere(vc, mat, cam, cx + rx, cy0 + ly, cz + rz, miniR, cr, cg, cb, a);
            }
        });
    }

    private static void drawMiniSphere(VertexConsumer vc, Matrix4f mat, Vec3d cam, double cx, double cy, double cz, double radius, int cr, int cg, int cb, float alpha) {
        float r = (float)cr / 255.0f;
        float g = (float)cg / 255.0f;
        float b = (float)cb / 255.0f;
        int lat = 6;
        int lon = 10;
        double ox = cx - cam.x;
        double oy = cy - cam.y;
        double oz = cz - cam.z;
        for (int i = 0; i < lat; ++i) {
            double v0 = Math.PI * ((double)i / (double)lat - 0.5);
            double v1 = Math.PI * ((double)(i + 1) / (double)lat - 0.5);
            double y0 = Math.sin(v0);
            double y1 = Math.sin(v1);
            double rr0 = Math.cos(v0);
            double rr1 = Math.cos(v1);
            for (int j = 0; j < lon; ++j) {
                double u0 = Math.PI * 2 * (double)j / (double)lon;
                double u1 = Math.PI * 2 * (double)(j + 1) / (double)lon;
                float x00 = (float)(ox + radius * rr0 * Math.cos(u0));
                float y00 = (float)(oy + radius * y0);
                float z00 = (float)(oz + radius * rr0 * Math.sin(u0));
                float x01 = (float)(ox + radius * rr0 * Math.cos(u1));
                float y01 = (float)(oy + radius * y0);
                float z01 = (float)(oz + radius * rr0 * Math.sin(u1));
                float x11 = (float)(ox + radius * rr1 * Math.cos(u1));
                float y11 = (float)(oy + radius * y1);
                float z11 = (float)(oz + radius * rr1 * Math.sin(u1));
                float x10 = (float)(ox + radius * rr1 * Math.cos(u0));
                float y10 = (float)(oy + radius * y1);
                float z10 = (float)(oz + radius * rr1 * Math.sin(u0));
                SpeedysVisualsRenderer.addQuad(vc, mat, x00, y00, z00, x01, y01, z01, x11, y11, z11, x10, y10, z10, r, g, b, alpha);
            }
        }
    }

    private static void drawNimb(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam, float tickDelta, int cr, int cg, int cb) {
        ClientPlayerEntity p = client.player;
        if (p == null || client.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        Vec3d lp = p.getLerpedPos(tickDelta);
        double y = lp.y + (double)p.getHeight() + 0.25;
        SpeedysVisualsRenderer.drawCircle(vc, mat, cam, lp.x, y, lp.z, 0.35, 30, cr, cg, cb, 0.95f);
        SpeedysVisualsRenderer.drawCircle(vc, mat, cam, lp.x, y + 0.01, lp.z, 0.35, 30, cr, cg, cb, 0.95f);
    }

    private static void drawTrapZone(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam, float tickDelta) {
        if (client.player == null) {
            return;
        }
        double size = PVPUtils.CONFIG.trapTimerDragonMode ? 7.0 : 5.0;
        double half = size / 2.0;
        Vec3d center = client.player.getLerpedPos(tickDelta);
        Box area = new Box(center.x - half, center.y - 1.0, center.z - half, center.x + half, center.y + 2.0, center.z + half);
        boolean hit = FtHelperFeature.hasAnyLivingInBox(client, area);
        int r = hit ? PVPUtils.CONFIG.predictionHitR : PVPUtils.CONFIG.predictionIdleR;
        int g = hit ? PVPUtils.CONFIG.predictionHitG : PVPUtils.CONFIG.predictionIdleG;
        int b = hit ? PVPUtils.CONFIG.predictionHitB : PVPUtils.CONFIG.predictionIdleB;
        double y = center.y + 0.02;
        double squareHeight = half * 2.0;
        SpeedysVisualsRenderer.drawSquarePrism(vc, mat, cam, center.x, y, center.z, half, squareHeight, r, g, b, 1.0f);
    }

    private static void drawFtHelper(MinecraftClient client, VertexConsumer vc, Matrix4f mat, Vec3d cam) {
        FtHelperFeature.State state = FtHelperFeature.collect(client);
        if (state.shapeType() == FtHelperFeature.ShapeType.NONE) {
            return;
        }
        int r = state.hitTarget() ? PVPUtils.CONFIG.ftHelperHitR : PVPUtils.CONFIG.ftHelperIdleR;
        int g = state.hitTarget() ? PVPUtils.CONFIG.ftHelperHitG : PVPUtils.CONFIG.ftHelperIdleG;
        int b = state.hitTarget() ? PVPUtils.CONFIG.ftHelperHitB : PVPUtils.CONFIG.ftHelperIdleB;
        double y = state.center().y + 0.02;
        if (state.shapeType() == FtHelperFeature.ShapeType.CIRCLE) {
            SpeedysVisualsRenderer.drawCircle(vc, mat, cam, state.center().x, y, state.center().z, state.radius(), 56, r, g, b, 1.0f);
            SpeedysVisualsRenderer.drawCircle(vc, mat, cam, state.center().x, y + 0.01, state.center().z, state.radius(), 56, r, g, b, 1.0f);
            return;
        }
        double squareHeight = state.halfSquare() * 2.0;
        SpeedysVisualsRenderer.drawSquarePrism(vc, mat, cam, state.center().x, y, state.center().z, state.halfSquare(), squareHeight, r, g, b, 1.0f);
    }

    private static void drawSquarePrism(VertexConsumer vc, Matrix4f mat, Vec3d cam, double cx, double bottomY, double cz, double half, double height, int cr, int cg, int cb, float alpha) {
        double topY = bottomY + height;
        SpeedysVisualsRenderer.drawSquare(vc, mat, cam, cx, bottomY, cz, half, cr, cg, cb, alpha);
        SpeedysVisualsRenderer.drawSquare(vc, mat, cam, cx, topY, cz, half, cr, cg, cb, alpha);
        float r = (float)cr / 255.0f;
        float g = (float)cg / 255.0f;
        float b = (float)cb / 255.0f;
        float x0 = (float)(cx - half - cam.x);
        float x1 = (float)(cx + half - cam.x);
        float z0 = (float)(cz - half - cam.z);
        float z1 = (float)(cz + half - cam.z);
        float fy0 = (float)(bottomY - cam.y);
        float fy1 = (float)(topY - cam.y);
        vc.vertex(mat, x0, fy0, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x0, fy1, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy0, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy1, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy0, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy1, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x0, fy0, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x0, fy1, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
    }

    private static void drawSquare(VertexConsumer vc, Matrix4f mat, Vec3d cam, double cx, double y, double cz, double half, int cr, int cg, int cb, float alpha) {
        float r = (float)cr / 255.0f;
        float g = (float)cg / 255.0f;
        float b = (float)cb / 255.0f;
        float x0 = (float)(cx - half - cam.x);
        float x1 = (float)(cx + half - cam.x);
        float z0 = (float)(cz - half - cam.z);
        float z1 = (float)(cz + half - cam.z);
        float fy = (float)(y - cam.y);
        vc.vertex(mat, x0, fy, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, fy, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x0, fy, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x0, fy, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x0, fy, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
    }

    private static void drawCircle(VertexConsumer vc, Matrix4f mat, Vec3d cam, double cx, double y, double cz, double radius, int segs, int cr, int cg, int cb, float alpha) {
        float r = (float)cr / 255.0f;
        float g = (float)cg / 255.0f;
        float b = (float)cb / 255.0f;
        double ox = cx - cam.x;
        double oy = y - cam.y;
        double oz = cz - cam.z;
        for (int i = 0; i < segs; ++i) {
            double u0 = Math.PI * 2 * (double)i / (double)segs;
            double u1 = Math.PI * 2 * (double)(i + 1) / (double)segs;
            float x0 = (float)(ox + radius * Math.cos(u0));
            float z0 = (float)(oz + radius * Math.sin(u0));
            float x1 = (float)(ox + radius * Math.cos(u1));
            float z1 = (float)(oz + radius * Math.sin(u1));
            vc.vertex(mat, x0, (float)oy, z0).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(mat, x1, (float)oy, z1).color(r, g, b, alpha).normal(0.0f, 1.0f, 0.0f);
        }
    }
}

