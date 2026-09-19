/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1665
 *  net.minecraft.class_1684
 *  net.minecraft.class_1685
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1887
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_1921
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_2378
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_7924
 *  org.joml.Matrix4f
 */
package com.pearllandingpredictor;

import com.pearllandingpredictor.PearlTrajectoryPredictor;
import com.shampoon.pvputils.PVPUtils;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1665;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1921;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import org.joml.Matrix4f;

final class PearlTrajectoryRenderer {
    private static final int MAX_STEPS = 100;
    private static final double TRACK_RADIUS = 64.0;
    private static final int MAX_CHECKED_PROJECTILES = 32;
    private static boolean enabled = true;
    private static LandingInfo nearestLandingInfo = null;

    PearlTrajectoryRenderer() {
    }

    static boolean isEnabled() {
        return enabled;
    }

    static void setEnabled(boolean value) {
        enabled = value;
    }

    static void toggleEnabled() {
        enabled = !enabled;
    }

    static void render(WorldRenderContext context) {
        if (!enabled) {
            return;
        }
        class_310 client = class_310.method_1551();
        class_746 player = client.field_1724;
        if (client.field_1687 == null || player == null) {
            nearestLandingInfo = null;
            return;
        }
        class_243 cameraPos = context.camera().method_19326();
        class_238 range = new class_238(cameraPos.field_1352 - 64.0, cameraPos.field_1351 - 64.0, cameraPos.field_1350 - 64.0, cameraPos.field_1352 + 64.0, cameraPos.field_1351 + 64.0, cameraPos.field_1350 + 64.0);
        class_4587 matrices = context.matrixStack();
        matrices.method_22903();
        Matrix4f m = matrices.method_23760().method_23761();
        class_4597 consumers = context.consumers();
        if (consumers == null) {
            matrices.method_22909();
            return;
        }
        class_4588 lines = consumers.getBuffer(class_1921.method_23594());
        class_4588 quads = consumers.getBuffer(class_1921.method_49042());
        LandingInfo nearestThrownPearl = null;
        boolean hasOwnActivePearl = false;
        int checked = 0;
        for (class_1297 entity : client.field_1687.method_8333((class_1297)player, range, PearlTrajectoryRenderer::isTrackedProjectileEntity)) {
            class_1684 pearl;
            if (checked >= 32) break;
            PearlTrajectoryPredictor.Prediction prediction = PearlTrajectoryRenderer.predictForEntity(client, entity);
            ++checked;
            if (prediction.landingPos == null || !(entity instanceof class_1684) || (pearl = (class_1684)entity).method_24921() != player) continue;
            hasOwnActivePearl = true;
            double distance = player.method_19538().method_1022(prediction.landingPos);
            if (nearestThrownPearl != null && !(distance < nearestThrownPearl.distance)) continue;
            nearestThrownPearl = new LandingInfo(prediction.landingPos, distance);
        }
        PearlTrajectoryRenderer.drawHeldItemPrediction(client, player, lines, quads, m, cameraPos, hasOwnActivePearl);
        nearestLandingInfo = nearestThrownPearl;
        matrices.method_22909();
    }

    private static boolean isTrackedProjectileEntity(class_1297 entity) {
        return entity instanceof class_1684 || entity instanceof class_1665 || entity instanceof class_1685;
    }

    private static PearlTrajectoryPredictor.Prediction predictForEntity(class_310 client, class_1297 entity) {
        if (entity instanceof class_1684) {
            return PearlTrajectoryPredictor.predict((class_1937)client.field_1687, entity, entity.method_19538(), entity.method_18798(), 100, 0.03, 0.99);
        }
        if (entity instanceof class_1685) {
            return PearlTrajectoryPredictor.predict((class_1937)client.field_1687, entity, entity.method_19538(), entity.method_18798(), 100, 0.05, 0.99);
        }
        return PearlTrajectoryPredictor.predict((class_1937)client.field_1687, entity, entity.method_19538(), entity.method_18798(), 100, 0.05, 0.99);
    }

    private static void drawHeldItemPrediction(class_310 client, class_746 player, class_4588 lines, class_4588 quads, Matrix4f matrix, class_243 cameraPos, boolean hasOwnActivePearl) {
        class_1799 held;
        class_1799 main = player.method_6047();
        class_1799 off = player.method_6079();
        class_1799 class_17992 = PearlTrajectoryRenderer.isSupportedPredictItem(main) ? main : (held = PearlTrajectoryRenderer.isSupportedPredictItem(off) ? off : class_1799.field_8037);
        if (held.method_7960()) {
            return;
        }
        class_1792 item = held.method_7909();
        class_243 dir = player.method_5828(1.0f);
        class_243 startPos = player.method_33571().method_1019(dir.method_1021(0.16));
        if (item == class_1802.field_8634) {
            if (hasOwnActivePearl) {
                return;
            }
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.method_1021(1.5), 0.03);
            return;
        }
        if (item == class_1802.field_8102) {
            if (!player.method_6115() || player.method_6030() != held) {
                return;
            }
            int usedTicks = held.method_7935((class_1309)player) - player.method_6014();
            float pull = class_1753.method_7722((int)usedTicks);
            if (pull < 0.1f) {
                return;
            }
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.method_1021((double)(pull * 3.0f)), 0.05);
            return;
        }
        if (item == class_1802.field_8399) {
            if (!class_1764.method_7781((class_1799)held)) {
                return;
            }
            float speed = 3.15f;
            class_2378 enchantmentRegistry = client.field_1687.method_30349().method_30530(class_7924.field_41265);
            class_1887 multishotValue = enchantmentRegistry.method_31189(class_1893.field_9108).orElse(null);
            class_6880 multishotEntryRaw = multishotValue == null ? null : enchantmentRegistry.method_47983((Object)multishotValue);
            int multishot = 0;
            if (multishotEntryRaw instanceof class_6880) {
                class_6880 raw;
                class_6880 multishotEntry = raw = multishotEntryRaw;
                multishot = class_1890.method_8225((class_6880)multishotEntry, (class_1799)held);
            }
            if (multishot > 0) {
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, PearlTrajectoryRenderer.rotateYaw(dir, -10.0f).method_1021((double)speed), 0.05);
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.method_1021((double)speed), 0.05);
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, PearlTrajectoryRenderer.rotateYaw(dir, 10.0f).method_1021((double)speed), 0.05);
            } else {
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.method_1021((double)speed), 0.05);
            }
            return;
        }
        if (item == class_1802.field_8547) {
            if (!player.method_6115() || player.method_6030() != held) {
                return;
            }
            int usedTicks = held.method_7935((class_1309)player) - player.method_6014();
            if (usedTicks < 10) {
                return;
            }
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.method_1021(2.5), 0.05);
            return;
        }
        if (item == class_1802.field_8436 || item == class_1802.field_8150) {
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.method_1021(0.5), 0.05);
        }
    }

    private static boolean isSupportedPredictItem(class_1799 stack) {
        return stack.method_31574(class_1802.field_8634) || stack.method_31574(class_1802.field_8102) || stack.method_31574(class_1802.field_8399) || stack.method_31574(class_1802.field_8547) || stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150);
    }

    private static void renderOnePrediction(class_310 client, class_746 player, class_4588 lines, class_4588 quads, Matrix4f matrix, class_243 cameraPos, class_243 startPos, class_243 startVelocity, double gravity) {
        PearlTrajectoryPredictor.Prediction prediction = PearlTrajectoryPredictor.predict((class_1937)client.field_1687, (class_1297)player, startPos, startVelocity, 100, gravity, 0.99);
        if (prediction.landingPos == null) {
            return;
        }
        boolean hitsEntity = PearlTrajectoryRenderer.hitsEntityNearLanding(client, prediction.landingPos);
        PearlTrajectoryRenderer.drawTrajectoryArc(lines, quads, matrix, prediction.points, cameraPos, hitsEntity);
        PearlTrajectoryRenderer.drawLandingMarker(quads, matrix, prediction.landingPos, cameraPos, hitsEntity);
    }

    private static class_243 rotateYaw(class_243 vec, float degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = vec.field_1352 * cos - vec.field_1350 * sin;
        double z = vec.field_1352 * sin + vec.field_1350 * cos;
        return new class_243(x, vec.field_1351, z).method_1029();
    }

    private static void drawLandingMarker(class_4588 fill, Matrix4f m, class_243 landingPos, class_243 cameraPos, boolean hitEntity) {
        class_243 p = landingPos.method_1020(cameraPos);
        float cx = (float)p.field_1352;
        float cy = (float)p.field_1351 + 0.06f;
        float cz = (float)p.field_1350;
        float cr;
        float cg;
        float cb;
        if (PVPUtils.CONFIG != null) {
            if (hitEntity) {
                cr = (float)PVPUtils.CONFIG.predictionHitR / 255.0f;
                cg = (float)PVPUtils.CONFIG.predictionHitG / 255.0f;
                cb = (float)PVPUtils.CONFIG.predictionHitB / 255.0f;
            } else {
                cr = (float)PVPUtils.CONFIG.predictionIdleR / 255.0f;
                cg = (float)PVPUtils.CONFIG.predictionIdleG / 255.0f;
                cb = (float)PVPUtils.CONFIG.predictionIdleB / 255.0f;
            }
        } else {
            cr = 0.6f;
            cg = 0.6f;
            cb = 0.6f;
        }
        PearlTrajectoryRenderer.drawSolidSphere(fill, m, cx, cy, cz, 0.42f, cr, cg, cb, 0.52f);
    }

    private static void drawSolidSphere(class_4588 fill, Matrix4f m, float cx, float cy, float cz, float radius, float cr, float cg, float cb, float alpha) {
        int lat = 12;
        int lon = 24;
        for (int i = 0; i < lat; ++i) {
            double v0 = Math.PI * ((double)i / (double)lat - 0.5);
            double v1 = Math.PI * ((double)(i + 1) / (double)lat - 0.5);
            float y0 = (float)(Math.sin(v0) * (double)radius);
            float y1 = (float)(Math.sin(v1) * (double)radius);
            float rr0 = (float)(Math.cos(v0) * (double)radius);
            float rr1 = (float)(Math.cos(v1) * (double)radius);
            for (int j = 0; j < lon; ++j) {
                double u0 = Math.PI * 2 * (double)j / (double)lon;
                double u1 = Math.PI * 2 * (double)(j + 1) / (double)lon;
                float x00 = cx + rr0 * (float)Math.cos(u0);
                float z00 = cz + rr0 * (float)Math.sin(u0);
                float x01 = cx + rr0 * (float)Math.cos(u1);
                float z01 = cz + rr0 * (float)Math.sin(u1);
                float x11 = cx + rr1 * (float)Math.cos(u1);
                float z11 = cz + rr1 * (float)Math.sin(u1);
                float x10 = cx + rr1 * (float)Math.cos(u0);
                float z10 = cz + rr1 * (float)Math.sin(u0);
                float yy0 = cy + y0;
                float yy1 = cy + y1;
                PearlTrajectoryRenderer.addQuad(fill, m, x00, yy0, z00, x01, yy0, z01, x11, yy1, z11, x10, yy1, z10, cr, cg, cb, alpha);
            }
        }
    }

    private static void drawTrajectoryArc(class_4588 vc, class_4588 ignoredFill, Matrix4f m, List<class_243> points, class_243 cameraPos, boolean hitEntity) {
        if (points == null || points.size() < 2) {
            return;
        }
        float r;
        float g;
        float b;
        float a = 0.92f;
        if (PVPUtils.CONFIG != null) {
            if (hitEntity) {
                r = (float)PVPUtils.CONFIG.predictionHitR / 255.0f;
                g = (float)PVPUtils.CONFIG.predictionHitG / 255.0f;
                b = (float)PVPUtils.CONFIG.predictionHitB / 255.0f;
            } else {
                r = (float)PVPUtils.CONFIG.predictionIdleR / 255.0f;
                g = (float)PVPUtils.CONFIG.predictionIdleG / 255.0f;
                b = (float)PVPUtils.CONFIG.predictionIdleB / 255.0f;
            }
        } else {
            r = 0.6f;
            g = 0.6f;
            b = 0.6f;
        }
        for (int i = 0; i < points.size() - 1; ++i) {
            class_243 p0 = points.get(i).method_1020(cameraPos);
            class_243 p1 = points.get(i + 1).method_1020(cameraPos);
            vc.method_22918(m, (float)p0.field_1352, (float)p0.field_1351, (float)p0.field_1350).method_22915(r, g, b, a).method_22914(0.0f, 1.0f, 0.0f);
            vc.method_22918(m, (float)p1.field_1352, (float)p1.field_1351, (float)p1.field_1350).method_22915(r, g, b, a).method_22914(0.0f, 1.0f, 0.0f);
        }
    }

    private static void addQuad(class_4588 vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float b, float a) {
        vc.method_22918(m, x0, y0, z0).method_22915(r, g, b, a).method_22914(0.0f, 1.0f, 0.0f);
        vc.method_22918(m, x1, y1, z1).method_22915(r, g, b, a).method_22914(0.0f, 1.0f, 0.0f);
        vc.method_22918(m, x2, y2, z2).method_22915(r, g, b, a).method_22914(0.0f, 1.0f, 0.0f);
        vc.method_22918(m, x3, y3, z3).method_22915(r, g, b, a).method_22914(0.0f, 1.0f, 0.0f);
    }

    private static boolean hitsEntityNearLanding(class_310 client, class_243 landingPos) {
        if (client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        class_238 near = new class_238(landingPos.field_1352 - 1.5, landingPos.field_1351 - 1.5, landingPos.field_1350 - 1.5, landingPos.field_1352 + 1.5, landingPos.field_1351 + 1.5, landingPos.field_1350 + 1.5);
        for (class_1297 e : client.field_1687.method_8335((class_1297)client.field_1724, near)) {
            if (!e.method_5805() || e.method_31481()) continue;
            return true;
        }
        return false;
    }

    static void renderHud(class_332 drawContext) {
        if (!enabled) {
            return;
        }
        LandingInfo info = nearestLandingInfo;
        if (info == null) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client.field_1772 == null) {
            return;
        }
        String title = "Pearl Landing";
        String distanceText = String.format(Locale.ROOT, "Distance: %.1f blocks", info.distance);
        class_2338 bp = class_2338.method_49638((class_2374)info.pos);
        String coordsText = "XYZ: " + bp.method_10263() + " " + bp.method_10264() + " " + bp.method_10260();
        int x = 8;
        int y = 8;
        int pad = 4;
        int lineH = 10;
        int w = Math.max(client.field_1772.method_1727(title), Math.max(client.field_1772.method_1727(distanceText), client.field_1772.method_1727(coordsText))) + pad * 2;
        int h = pad * 2 + lineH * 3;
        drawContext.method_25294(x, y, x + w, y + h, -1879048192);
        drawContext.method_25294(x, y, x + w, y + 1, -43691);
        drawContext.method_51433(client.field_1772, title, x + pad, y + pad, 0xFFFFFF, true);
        drawContext.method_51433(client.field_1772, distanceText, x + pad, y + pad + lineH, -2039584, false);
        drawContext.method_51433(client.field_1772, coordsText, x + pad, y + pad + lineH * 2, -30584, false);
    }

    private record LandingInfo(class_243 pos, double distance) {
    }
}

