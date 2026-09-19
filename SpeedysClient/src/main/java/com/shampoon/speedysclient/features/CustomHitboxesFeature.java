package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Отрисовка хитбоксов сущностей: основной AABB, линия взгляда, малый бокс головы; опциональная заливка по AABB.
 */
public final class CustomHitboxesFeature {
    private static final double RANGE = 96.0;
    private static final float FILL_ALPHA = 0.34f;
    private static final float LINE_ALPHA = 0.92f;
    private static final float FACE_EPS = 0.0025f;
    private static final double LOOK_LENGTH = 1.5;
    private static final double HEAD_BELOW = 0.32;
    private static final double HEAD_ABOVE = 0.12;

    private CustomHitboxesFeature() {
    }

    public static void render(
            MinecraftClient client,
            VertexConsumer lines,
            VertexConsumer quads,
            Matrix4f mat,
            Camera camera,
            float tickDelta) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.customHitboxesEnabled) {
            return;
        }
        if (client.world == null || client.player == null) {
            return;
        }
        Vec3d cam = camera.getPos();
        Box range = client.player.getBoundingBox().expand(RANGE);
        int br = PVPUtils.CONFIG.customHitboxBoxR;
        int bg = PVPUtils.CONFIG.customHitboxBoxG;
        int bb = PVPUtils.CONFIG.customHitboxBoxB;
        int er = PVPUtils.CONFIG.customHitboxEyeR;
        int eg = PVPUtils.CONFIG.customHitboxEyeG;
        int eb = PVPUtils.CONFIG.customHitboxEyeB;
        int hr = PVPUtils.CONFIG.customHitboxHeadR;
        int hg = PVPUtils.CONFIG.customHitboxHeadG;
        int hb = PVPUtils.CONFIG.customHitboxHeadB;
        float fr = PVPUtils.CONFIG.customHitboxFillR / 255.0f;
        float fg = PVPUtils.CONFIG.customHitboxFillG / 255.0f;
        float fb = PVPUtils.CONFIG.customHitboxFillB / 255.0f;
        float bLineR = br / 255.0f;
        float bLineG = bg / 255.0f;
        float bLineB = bb / 255.0f;
        float eLineR = er / 255.0f;
        float eLineG = eg / 255.0f;
        float eLineB = eb / 255.0f;
        float hLineR = hr / 255.0f;
        float hLineG = hg / 255.0f;
        float hLineB = hb / 255.0f;

        for (Entity entity : client.world.getEntitiesByClass(Entity.class, range, Entity::isAlive)) {
            if (entity == client.player && client.options.getPerspective() == Perspective.FIRST_PERSON) {
                continue;
            }
            Vec3d pos = entity.getPos();
            Vec3d lerp = entity.getLerpedPos(tickDelta);
            double ox = lerp.x - pos.x;
            double oy = lerp.y - pos.y;
            double oz = lerp.z - pos.z;
            Box worldBox = entity.getBoundingBox().offset(ox, oy, oz);
            boolean fillDoubleSided = worldBox.contains(cam);
            if (PVPUtils.CONFIG.customHitboxFillEnabled) {
                CustomHitboxesFeature.drawFilledAabb(quads, mat, cam, worldBox, fr, fg, fb, FILL_ALPHA, fillDoubleSided);
            }
            CustomHitboxesFeature.drawWireAabb(lines, mat, cam, worldBox, bLineR, bLineG, bLineB, LINE_ALPHA);
            if (entity instanceof LivingEntity le) {
                double relEye = le.getEyeY() - le.getY();
                double eyeY = lerp.y + relEye;
                Vec3d eye = new Vec3d(lerp.x, eyeY, lerp.z);
                Vec3d look = CustomHitboxesFeature.lookDirectionForEntity(client, le, camera, tickDelta);
                if (look.lengthSquared() < 1.0e-8) {
                    look = new Vec3d(0.0, -1.0, 0.0);
                }
                look = look.normalize().multiply(LOOK_LENGTH);
                Vec3d lookEnd = eye.add(look);
                CustomHitboxesFeature.drawLineSegment(lines, mat, cam, eye, lookEnd, eLineR, eLineG, eLineB, LINE_ALPHA);
                double w = le.getWidth();
                double headMinY = eyeY - HEAD_BELOW;
                double headMaxY = eyeY + HEAD_ABOVE;
                Box headBox = new Box(lerp.x - w * 0.5, headMinY, lerp.z - w * 0.5, lerp.x + w * 0.5, headMaxY, lerp.z + w * 0.5);
                CustomHitboxesFeature.drawWireAabb(lines, mat, cam, headBox, hLineR, hLineG, hLineB, LINE_ALPHA);
            }
        }
    }

    /** Локальный игрок не от 1 лица: направление камеры (прицел). Иначе — тело сущности (как в игре). Всегда нормализуем × 1.5. */
    private static Vec3d lookDirectionForEntity(
            MinecraftClient client,
            LivingEntity le,
            Camera camera,
            float tickDelta) {
        if (le == client.player) {
            return CustomHitboxesFeature.rotationVecFromCamera(camera);
        }
        return le.getRotationVec(tickDelta);
    }

    /** Та же формула, что у Entity#getRotationVector(pitch, yaw): камера в градусах → прицел. */
    private static Vec3d rotationVecFromCamera(Camera camera) {
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float cosYaw = MathHelper.cos(g);
        float sinYaw = MathHelper.sin(g);
        float cosPitch = MathHelper.cos(f);
        float sinPitch = MathHelper.sin(f);
        return new Vec3d((double) (sinYaw * cosPitch), (double) (-sinPitch), (double) (cosYaw * cosPitch));
    }

    private static void drawFilledAabb(
            VertexConsumer quads,
            Matrix4f mat,
            Vec3d cam,
            Box box,
            float r,
            float g,
            float b,
            float a,
            boolean doubleSided) {
        float x0 = (float) (box.minX - cam.x);
        float x1 = (float) (box.maxX - cam.x);
        float y0 = (float) (box.minY - cam.y);
        float y1 = (float) (box.maxY - cam.y);
        float z0 = (float) (box.minZ - cam.z);
        float z1 = (float) (box.maxZ - cam.z);
        float e = FACE_EPS;
        for (Direction face : Direction.values()) {
            switch (face) {
                case UP -> {
                    float y = y1 + e;
                    CustomHitboxesFeature.quad(quads, mat, x0, y, z0, x1, y, z0, x1, y, z1, x0, y, z1, r, g, b, a, 0.0f, 1.0f, 0.0f);
                    if (doubleSided) {
                        CustomHitboxesFeature.quad(quads, mat, x0, y, z1, x1, y, z1, x1, y, z0, x0, y, z0, r, g, b, a, 0.0f, -1.0f, 0.0f);
                    }
                }
                case DOWN -> {
                    float y = y0 - e;
                    CustomHitboxesFeature.quad(quads, mat, x0, y, z0, x0, y, z1, x1, y, z1, x1, y, z0, r, g, b, a, 0.0f, -1.0f, 0.0f);
                    if (doubleSided) {
                        CustomHitboxesFeature.quad(quads, mat, x0, y, z0, x1, y, z0, x1, y, z1, x0, y, z1, r, g, b, a, 0.0f, 1.0f, 0.0f);
                    }
                }
                case NORTH -> {
                    float z = z0 - e;
                    CustomHitboxesFeature.quad(quads, mat, x0, y0, z, x1, y0, z, x1, y1, z, x0, y1, z, r, g, b, a, 0.0f, 0.0f, -1.0f);
                    if (doubleSided) {
                        CustomHitboxesFeature.quad(quads, mat, x0, y1, z, x1, y1, z, x1, y0, z, x0, y0, z, r, g, b, a, 0.0f, 0.0f, 1.0f);
                    }
                }
                case SOUTH -> {
                    float z = z1 + e;
                    CustomHitboxesFeature.quad(quads, mat, x0, y0, z, x0, y1, z, x1, y1, z, x1, y0, z, r, g, b, a, 0.0f, 0.0f, 1.0f);
                    if (doubleSided) {
                        CustomHitboxesFeature.quad(quads, mat, x0, y0, z, x1, y0, z, x1, y1, z, x0, y1, z, r, g, b, a, 0.0f, 0.0f, -1.0f);
                    }
                }
                case WEST -> {
                    float x = x0 - e;
                    CustomHitboxesFeature.quad(quads, mat, x, y0, z0, x, y0, z1, x, y1, z1, x, y1, z0, r, g, b, a, -1.0f, 0.0f, 0.0f);
                    if (doubleSided) {
                        CustomHitboxesFeature.quad(quads, mat, x, y0, z0, x, y1, z0, x, y1, z1, x, y0, z1, r, g, b, a, 1.0f, 0.0f, 0.0f);
                    }
                }
                case EAST -> {
                    float x = x1 + e;
                    CustomHitboxesFeature.quad(quads, mat, x, y0, z0, x, y1, z0, x, y1, z1, x, y0, z1, r, g, b, a, 1.0f, 0.0f, 0.0f);
                    if (doubleSided) {
                        CustomHitboxesFeature.quad(quads, mat, x, y0, z1, x, y1, z1, x, y1, z0, x, y0, z0, r, g, b, a, -1.0f, 0.0f, 0.0f);
                    }
                }
            }
        }
    }

    private static void quad(
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
            float a,
            float nx,
            float ny,
            float nz) {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(nx, ny, nz);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
        vc.vertex(mat, x3, y3, z3).color(r, g, b, a).normal(nx, ny, nz);
    }

    private static void drawWireAabb(
            VertexConsumer lines,
            Matrix4f mat,
            Vec3d cam,
            Box box,
            float r,
            float g,
            float b,
            float a) {
        float x0 = (float) (box.minX - cam.x);
        float x1 = (float) (box.maxX - cam.x);
        float y0 = (float) (box.minY - cam.y);
        float y1 = (float) (box.maxY - cam.y);
        float z0 = (float) (box.minZ - cam.z);
        float z1 = (float) (box.maxZ - cam.z);
        CustomHitboxesFeature.line(lines, mat, x0, y0, z0, x1, y0, z0, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x1, y0, z0, x1, y0, z1, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x1, y0, z1, x0, y0, z1, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x0, y0, z1, x0, y0, z0, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x0, y1, z0, x1, y1, z0, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x1, y1, z0, x1, y1, z1, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x1, y1, z1, x0, y1, z1, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x0, y1, z1, x0, y1, z0, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x0, y0, z0, x0, y1, z0, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x1, y0, z0, x1, y1, z0, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x1, y0, z1, x1, y1, z1, r, g, b, a);
        CustomHitboxesFeature.line(lines, mat, x0, y0, z1, x0, y1, z1, r, g, b, a);
    }

    private static void drawLineSegment(
            VertexConsumer lines,
            Matrix4f mat,
            Vec3d cam,
            Vec3d a,
            Vec3d b,
            float r,
            float g,
            float bl,
            float alpha) {
        CustomHitboxesFeature.line(
                lines,
                mat,
                (float) (a.x - cam.x),
                (float) (a.y - cam.y),
                (float) (a.z - cam.z),
                (float) (b.x - cam.x),
                (float) (b.y - cam.y),
                (float) (b.z - cam.z),
                r,
                g,
                bl,
                alpha);
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
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
    }
}
