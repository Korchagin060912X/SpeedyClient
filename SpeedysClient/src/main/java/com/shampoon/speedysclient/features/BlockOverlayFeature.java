package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.joml.Matrix4f;

/**
 * Подсветка всего блока под прицелом: все грани полупрозрачным основным цветом и опционально каркас блока цветом «бока».
 */
public final class BlockOverlayFeature {

    private static final int[] GRADIENT_RGB = new int[3];
    private static final float FACE_EPS = 0.0025f;
    /** На каждую грань — несколько граней видно одновременно, альфа чуть ниже. */
    private static final float FILL_ALPHA = 0.34f;

    private BlockOverlayFeature() {
    }

    public static void render(
            MinecraftClient client,
            VertexConsumer lines,
            VertexConsumer quads,
            Matrix4f mat,
            Vec3d cam) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.blockOverlayEnabled) {
            return;
        }
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult bhr = (BlockHitResult) client.crosshairTarget;
        BlockRenderView world = client.world;
        if (world == null) {
            return;
        }
        BlockPos pos = bhr.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        VoxelShape shape = state.getOutlineShape(world, pos);
        if (shape.isEmpty()) {
            return;
        }
        Box bb = shape.getBoundingBox();
        float ox = (float) -cam.x;
        float oy = (float) -cam.y;
        float oz = (float) -cam.z;

        float x0 = (float) (pos.getX() + bb.minX) + ox;
        float y0 = (float) (pos.getY() + bb.minY) + oy;
        float z0 = (float) (pos.getZ() + bb.minZ) + oz;
        float x1 = (float) (pos.getX() + bb.maxX) + ox;
        float y1 = (float) (pos.getY() + bb.maxY) + oy;
        float z1 = (float) (pos.getZ() + bb.maxZ) + oz;

        VisualRgbGradient.sampleRgb(
                System.currentTimeMillis(),
                PVPUtils.CONFIG.blockOverlayR,
                PVPUtils.CONFIG.blockOverlayG,
                PVPUtils.CONFIG.blockOverlayB,
                PVPUtils.CONFIG.blockOverlayR2,
                PVPUtils.CONFIG.blockOverlayG2,
                PVPUtils.CONFIG.blockOverlayB2,
                PVPUtils.CONFIG.blockOverlayR3,
                PVPUtils.CONFIG.blockOverlayG3,
                PVPUtils.CONFIG.blockOverlayB3,
                PVPUtils.CONFIG.blockOverlayColorStops,
                PVPUtils.CONFIG.blockOverlayGradientAnim,
                GRADIENT_RGB);
        int mr = GRADIENT_RGB[0];
        int mg = GRADIENT_RGB[1];
        int mb = GRADIENT_RGB[2];
        float mrF = mr / 255.0f;
        float mgF = mg / 255.0f;
        float mbF = mb / 255.0f;

        for (Direction face : Direction.values()) {
            BlockOverlayFeature.drawFilledFace(quads, mat, face, x0, y0, z0, x1, y1, z1, mrF, mgF, mbF, FILL_ALPHA);
        }

        if (PVPUtils.CONFIG.blockOverlaySidesEnabled) {
            int sr = PVPUtils.CONFIG.blockOverlaySidesR;
            int sg = PVPUtils.CONFIG.blockOverlaySidesG;
            int sb = PVPUtils.CONFIG.blockOverlaySidesB;
            float srF = sr / 255.0f;
            float sgF = sg / 255.0f;
            float sbF = sb / 255.0f;
            BlockOverlayFeature.drawWireCube(lines, mat, x0, y0, z0, x1, y1, z1, srF, sgF, sbF, 0.92f);
        }
    }

    private static void drawFilledFace(
            VertexConsumer quads,
            Matrix4f mat,
            Direction face,
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
        float e = FACE_EPS;
        switch (face) {
            case UP -> {
                float y = y1 + e;
                BlockOverlayFeature.addQuad(
                        quads, mat, x0, y, z0, x1, y, z0, x1, y, z1, x0, y, z1, r, g, b, a, 0.0f, 1.0f, 0.0f);
            }
            case DOWN -> {
                float y = y0 - e;
                BlockOverlayFeature.addQuad(
                        quads, mat, x0, y, z0, x0, y, z1, x1, y, z1, x1, y, z0, r, g, b, a, 0.0f, -1.0f, 0.0f);
            }
            case NORTH -> {
                float z = z0 - e;
                BlockOverlayFeature.addQuad(
                        quads, mat, x0, y0, z, x1, y0, z, x1, y1, z, x0, y1, z, r, g, b, a, 0.0f, 0.0f, -1.0f);
            }
            case SOUTH -> {
                float z = z1 + e;
                BlockOverlayFeature.addQuad(
                        quads, mat, x0, y0, z, x0, y1, z, x1, y1, z, x1, y0, z, r, g, b, a, 0.0f, 0.0f, 1.0f);
            }
            case WEST -> {
                float x = x0 - e;
                BlockOverlayFeature.addQuad(
                        quads, mat, x, y0, z0, x, y0, z1, x, y1, z1, x, y1, z0, r, g, b, a, -1.0f, 0.0f, 0.0f);
            }
            case EAST -> {
                float x = x1 + e;
                BlockOverlayFeature.addQuad(
                        quads, mat, x, y0, z0, x, y1, z0, x, y1, z1, x, y0, z1, r, g, b, a, 1.0f, 0.0f, 0.0f);
            }
        }
    }

    /** Все 12 рёбер ограничивающего параллелепипеда (в координатах относительно камеры). */
    public static void drawWireCube(
            VertexConsumer lines,
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
        BlockOverlayFeature.line(lines, mat, x0, y0, z0, x1, y0, z0, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x1, y0, z0, x1, y0, z1, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x1, y0, z1, x0, y0, z1, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x0, y0, z1, x0, y0, z0, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x0, y1, z0, x1, y1, z0, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x1, y1, z0, x1, y1, z1, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x1, y1, z1, x0, y1, z1, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x0, y1, z1, x0, y1, z0, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x0, y0, z0, x0, y1, z0, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x1, y0, z0, x1, y1, z0, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x1, y0, z1, x1, y1, z1, r, g, b, a);
        BlockOverlayFeature.line(lines, mat, x0, y0, z1, x0, y1, z1, r, g, b, a);
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
            float a,
            float nx,
            float ny,
            float nz) {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(nx, ny, nz);
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        vc.vertex(mat, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
        vc.vertex(mat, x3, y3, z3).color(r, g, b, a).normal(nx, ny, nz);
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
