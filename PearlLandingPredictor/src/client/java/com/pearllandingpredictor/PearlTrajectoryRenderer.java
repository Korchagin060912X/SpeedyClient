/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.projectile.PersistentProjectileEntity
 *  net.minecraft.entity.projectile.TridentEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.Registry
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 *  org.joml.Matrix4f
 */
package com.pearllandingpredictor;

import com.pearllandingpredictor.PearlTrajectoryPredictor;
import com.shampoon.pvputils.PVPUtils;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (client.world == null || player == null) {
            nearestLandingInfo = null;
            return;
        }
        Vec3d cameraPos = context.camera().getPos();
        Box range = new Box(cameraPos.x - 64.0, cameraPos.y - 64.0, cameraPos.z - 64.0, cameraPos.x + 64.0, cameraPos.y + 64.0, cameraPos.z + 64.0);
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) {
            matrices.pop();
            return;
        }
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        VertexConsumer quads = consumers.getBuffer(RenderLayer.getDebugQuads());
        LandingInfo nearestThrownPearl = null;
        boolean hasOwnActivePearl = false;
        int checked = 0;
        for (Entity entity : client.world.getOtherEntities((Entity)player, range, PearlTrajectoryRenderer::isTrackedProjectileEntity)) {
            EnderPearlEntity pearl;
            if (checked >= 32) break;
            PearlTrajectoryPredictor.Prediction prediction = PearlTrajectoryRenderer.predictForEntity(client, entity);
            ++checked;
            if (prediction.landingPos == null || !(entity instanceof EnderPearlEntity) || (pearl = (EnderPearlEntity)entity).getOwner() != player) continue;
            hasOwnActivePearl = true;
            double distance = player.getPos().distanceTo(prediction.landingPos);
            if (nearestThrownPearl != null && !(distance < nearestThrownPearl.distance)) continue;
            nearestThrownPearl = new LandingInfo(prediction.landingPos, distance);
        }
        PearlTrajectoryRenderer.drawHeldItemPrediction(client, player, lines, quads, m, cameraPos, hasOwnActivePearl);
        nearestLandingInfo = nearestThrownPearl;
        matrices.pop();
    }

    private static boolean isTrackedProjectileEntity(Entity entity) {
        return entity instanceof EnderPearlEntity || entity instanceof PersistentProjectileEntity || entity instanceof TridentEntity;
    }

    private static PearlTrajectoryPredictor.Prediction predictForEntity(MinecraftClient client, Entity entity) {
        if (entity instanceof EnderPearlEntity) {
            return PearlTrajectoryPredictor.predict((World)client.world, entity, entity.getPos(), entity.getVelocity(), 100, 0.03, 0.99);
        }
        if (entity instanceof TridentEntity) {
            return PearlTrajectoryPredictor.predict((World)client.world, entity, entity.getPos(), entity.getVelocity(), 100, 0.05, 0.99);
        }
        return PearlTrajectoryPredictor.predict((World)client.world, entity, entity.getPos(), entity.getVelocity(), 100, 0.05, 0.99);
    }

    private static void drawHeldItemPrediction(MinecraftClient client, ClientPlayerEntity player, VertexConsumer lines, VertexConsumer quads, Matrix4f matrix, Vec3d cameraPos, boolean hasOwnActivePearl) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        ItemStack held = PearlTrajectoryRenderer.isSupportedPredictItem(main) ? main : (PearlTrajectoryRenderer.isSupportedPredictItem(off) ? off : ItemStack.EMPTY);
        if (held.isEmpty()) {
            return;
        }
        Item item = held.getItem();
        Vec3d dir = player.getRotationVec(1.0f);
        Vec3d startPos = player.getEyePos().add(dir.multiply(0.16));
        if (item == Items.ENDER_PEARL) {
            if (hasOwnActivePearl) {
                return;
            }
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.multiply(1.5), 0.03);
            return;
        }
        if (item == Items.BOW) {
            if (!player.isUsingItem() || player.getActiveItem() != held) {
                return;
            }
            int usedTicks = held.getMaxUseTime((LivingEntity)player) - player.getItemUseTimeLeft();
            float pull = BowItem.getPullProgress((int)usedTicks);
            if (pull < 0.1f) {
                return;
            }
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.multiply((double)(pull * 3.0f)), 0.05);
            return;
        }
        if (item == Items.CROSSBOW) {
            if (!CrossbowItem.isCharged((ItemStack)held)) {
                return;
            }
            float speed = 3.15f;
            Registry<Enchantment> enchantmentRegistry = client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            Enchantment multishotValue = enchantmentRegistry.getOptionalValue(Enchantments.MULTISHOT).orElse(null);
            RegistryEntry<Enchantment> multishotEntryRaw = multishotValue == null ? null : enchantmentRegistry.getEntry(multishotValue);
            int multishot = 0;
            if (multishotEntryRaw != null) {
                multishot = EnchantmentHelper.getLevel(multishotEntryRaw, (ItemStack)held);
            }
            if (multishot > 0) {
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, PearlTrajectoryRenderer.rotateYaw(dir, -10.0f).multiply((double)speed), 0.05);
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.multiply((double)speed), 0.05);
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, PearlTrajectoryRenderer.rotateYaw(dir, 10.0f).multiply((double)speed), 0.05);
            } else {
                PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.multiply((double)speed), 0.05);
            }
            return;
        }
        if (item == Items.TRIDENT) {
            if (!player.isUsingItem() || player.getActiveItem() != held) {
                return;
            }
            int usedTicks = held.getMaxUseTime((LivingEntity)player) - player.getItemUseTimeLeft();
            if (usedTicks < 10) {
                return;
            }
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.multiply(2.5), 0.05);
            return;
        }
        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            PearlTrajectoryRenderer.renderOnePrediction(client, player, lines, quads, matrix, cameraPos, startPos, dir.multiply(0.5), 0.05);
        }
    }

    private static boolean isSupportedPredictItem(ItemStack stack) {
        return stack.isOf(Items.ENDER_PEARL) || stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW) || stack.isOf(Items.TRIDENT) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
    }

    private static void renderOnePrediction(MinecraftClient client, ClientPlayerEntity player, VertexConsumer lines, VertexConsumer quads, Matrix4f matrix, Vec3d cameraPos, Vec3d startPos, Vec3d startVelocity, double gravity) {
        PearlTrajectoryPredictor.Prediction prediction = PearlTrajectoryPredictor.predict((World)client.world, (Entity)player, startPos, startVelocity, 100, gravity, 0.99);
        if (prediction.landingPos == null) {
            return;
        }
        boolean hitsEntity = PearlTrajectoryRenderer.hitsEntityNearLanding(client, prediction.landingPos);
        PearlTrajectoryRenderer.drawTrajectoryArc(lines, quads, matrix, prediction.points, cameraPos, hitsEntity);
        PearlTrajectoryRenderer.drawLandingMarker(quads, matrix, prediction.landingPos, cameraPos, hitsEntity);
    }

    private static Vec3d rotateYaw(Vec3d vec, float degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = vec.x * cos - vec.z * sin;
        double z = vec.x * sin + vec.z * cos;
        return new Vec3d(x, vec.y, z).normalize();
    }

    private static void drawLandingMarker(VertexConsumer fill, Matrix4f m, Vec3d landingPos, Vec3d cameraPos, boolean hitEntity) {
        Vec3d p = landingPos.subtract(cameraPos);
        float cx = (float)p.x;
        float cy = (float)p.y + 0.06f;
        float cz = (float)p.z;
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

    private static void drawSolidSphere(VertexConsumer fill, Matrix4f m, float cx, float cy, float cz, float radius, float cr, float cg, float cb, float alpha) {
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

    private static void drawTrajectoryArc(VertexConsumer vc, VertexConsumer ignoredFill, Matrix4f m, List<Vec3d> points, Vec3d cameraPos, boolean hitEntity) {
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
            Vec3d p0 = points.get(i).subtract(cameraPos);
            Vec3d p1 = points.get(i + 1).subtract(cameraPos);
            vc.vertex(m, (float)p0.x, (float)p0.y, (float)p0.z).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(m, (float)p1.x, (float)p1.y, (float)p1.z).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        }
    }

    private static void addQuad(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float b, float a) {
        vc.vertex(m, x0, y0, z0).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(m, x1, y1, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(m, x2, y2, z2).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(m, x3, y3, z3).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f);
    }

    private static boolean hitsEntityNearLanding(MinecraftClient client, Vec3d landingPos) {
        if (client.player == null || client.world == null) {
            return false;
        }
        Box near = new Box(landingPos.x - 1.5, landingPos.y - 1.5, landingPos.z - 1.5, landingPos.x + 1.5, landingPos.y + 1.5, landingPos.z + 1.5);
        for (Entity e : client.world.getOtherEntities((Entity)client.player, near)) {
            if (!e.isAlive() || e.isRemoved()) continue;
            return true;
        }
        return false;
    }

    static void renderHud(DrawContext drawContext) {
        if (!enabled) {
            return;
        }
        LandingInfo info = nearestLandingInfo;
        if (info == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.textRenderer == null) {
            return;
        }
        String title = "Pearl Landing";
        String distanceText = String.format(Locale.ROOT, "Distance: %.1f blocks", info.distance);
        BlockPos bp = BlockPos.ofFloored((Position)info.pos);
        String coordsText = "XYZ: " + bp.getX() + " " + bp.getY() + " " + bp.getZ();
        int x = 8;
        int y = 8;
        int pad = 4;
        int lineH = 10;
        int w = Math.max(client.textRenderer.getWidth(title), Math.max(client.textRenderer.getWidth(distanceText), client.textRenderer.getWidth(coordsText))) + pad * 2;
        int h = pad * 2 + lineH * 3;
        drawContext.fill(x, y, x + w, y + h, -1879048192);
        drawContext.fill(x, y, x + w, y + 1, -43691);
        drawContext.drawText(client.textRenderer, title, x + pad, y + pad, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, distanceText, x + pad, y + pad + lineH, -2039584, false);
        drawContext.drawText(client.textRenderer, coordsText, x + pad, y + pad + lineH * 2, -30584, false);
    }

    private record LandingInfo(Vec3d pos, double distance) {
    }
}

