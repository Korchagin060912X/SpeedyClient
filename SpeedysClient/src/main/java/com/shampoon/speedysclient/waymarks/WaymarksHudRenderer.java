package com.shampoon.speedysclient.waymarks;

import com.shampoon.speedysclient.config.WaymarksConfig;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Метки в 2D HUD с проекцией из мира — без 3D-quad слоёв (они растягивались и давали Z-fighting с текстом).
 */
public final class WaymarksHudRenderer {
    private WaymarksHudRenderer() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.options.hudHidden) {
            return;
        }
        if (client.currentScreen != null && !(client.currentScreen instanceof ChatScreen)) {
            return;
        }
        String dim = client.world.getRegistryKey().getValue().toString();
        List<WaymarksConfig.WaymarkEntry> list = WaymarksConfig.get().markers;
        if (list.isEmpty()) {
            return;
        }
        float tickDelta = tickCounter.getTickProgress(false);
        Vec3d playerPos = client.player.getLerpedPos(tickDelta);
        TextRenderer tr = client.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();

        for (WaymarksConfig.WaymarkEntry e : list) {
            if (e == null || !dim.equals(e.dimension)) {
                continue;
            }
            double wx = e.x + 0.5;
            double wy = e.y + 1.35;
            double wz = e.z + 0.5;
            int[] p = projectToGui(client, new Vec3d(wx, wy, wz), tickDelta, sw, sh);
            if (p == null) {
                continue;
            }
            int cx = p[0];
            int cy = p[1];

            double dist = playerPos.distanceTo(new Vec3d(wx, wy, wz));
            int blocks = Math.max(0, (int) Math.round(dist));
            String icon = WaymarksConfig.ICONS[Math.min(Math.max(e.iconIndex, 0), WaymarksConfig.ICONS.length - 1)];
            String line1 = e.name.isEmpty() ? "\u041c\u0435\u0442\u043a\u0430" : e.name;
            String line2 = blocks + " \u0431\u043b\u043e\u043a";

            int pad = 4;
            int iconSlot = 16;
            int gap = 4;
            int textW = Math.max(tr.getWidth(line1), tr.getWidth(line2));
            int w = pad + iconSlot + gap + textW + pad;
            int h = 26;
            int tailRows = 5;
            int tailHalfBase = 4;
            int left = cx - w / 2;
            int top = cy - h - tailRows - 2;

            int a = 210;
            int fr = e.r;
            int fg = e.g;
            int fb = e.b;
            int br = Math.min(255, fr / 4 + 18);
            int bgc = Math.min(255, fg / 4 + 18);
            int bb = Math.min(255, fb / 4 + 22);
            int bg = (a << 24) | (br << 16) | (bgc << 8) | bb;
            int bsR = Math.min(255, fr / 3 + 28);
            int bsG = Math.min(255, fg / 3 + 22);
            int bsB = Math.min(255, fb / 3 + 26);
            int bgSolid = (a << 24) | (bsR << 16) | (bsG << 8) | bsB;
            int ibR = Math.min(255, fr / 2 + 35);
            int ibG = Math.min(255, fg / 2 + 22);
            int ibB = Math.min(255, fb / 2 + 22);
            int iconBg = (a << 24) | (ibR << 16) | (ibG << 8) | ibB;

            WaymarksHudRenderer.drawRoundedRect(context, left, top, left + w, top + h, bg);
            WaymarksHudRenderer.drawRoundedRect(context, left + pad, top + 4, left + pad + iconSlot, top + h - 4, iconBg);
            WaymarksHudRenderer.drawSpeechTail(context, cx, top + h - 1, tailRows, tailHalfBase, bgSolid);

            int tx = left + pad + iconSlot + gap;
            int ty1 = top + 5;
            int ty2 = top + 15;
            context.drawText(tr, icon, left + pad + 4, top + 8, 0xFFFFFFFF, false);
            context.drawText(tr, line1, tx, ty1, 0xFFFFFFFF, false);
            context.drawText(tr, line2, tx, ty2, 0xFFCCCCCC, false);
        }
    }

    /**
     * Центр метки в экранных координатах GUI (scaled), или {@code null} если за камерой / вне экрана.
     */
    private static int[] projectToGui(MinecraftClient client, Vec3d world, float tickDelta, int scaledW, int scaledH) {
        GameRenderer gr = client.gameRenderer;
        Camera camera = gr.getCamera();
        Vec3d camPos = camera.getPos();
        boolean firstPerson = client.options.getPerspective() == Perspective.FIRST_PERSON;
        float fovEffect = client.options.getFovEffectScale().getValue().floatValue();
        float fovMult = client.player.getFovMultiplier(firstPerson, fovEffect);
        float fovDeg = (float) client.options.getFov().getValue() * fovMult;
        Matrix4f proj = gr.getBasicProjectionMatrix(fovDeg);

        float dx = (float) (world.x - camPos.x);
        float dy = (float) (world.y - camPos.y);
        float dz = (float) (world.z - camPos.z);
        Vector3f view = new Vector3f(dx, dy, dz);
        Quaternionf inv = new Quaternionf(camera.getRotation()).invert();
        inv.transform(view);

        // В пространстве камеры Minecraft точка перед игроком имеет отрицательный Z.
        if (view.z >= -0.01f) {
            return null;
        }

        Vector4f clip = new Vector4f(view.x, view.y, view.z, 1.0f);
        clip.mul(proj);
        if (clip.w() == 0.0f) {
            return null;
        }
        float iw = 1.0f / clip.w();
        float nx = clip.x * iw;
        float ny = clip.y * iw;
        float nz = clip.z * iw;
        if (nz < -1.02f || nz > 1.02f) {
            return null;
        }

        var window = client.getWindow();
        int fw = window.getFramebufferWidth();
        int fh = window.getFramebufferHeight();
        float fx = (nx * 0.5f + 0.5f) * fw;
        float fy = fh - (ny * 0.5f + 0.5f) * fh;
        double scale = window.getScaleFactor();
        int gx = (int) (fx / scale);
        int gy = (int) (fy / scale);

        if (gx < -80 || gx > scaledW + 80 || gy < -80 || gy > scaledH + 80) {
            return null;
        }
        return new int[]{gx, gy};
    }

    /** Скруглённые углы как в {@code WaymarksScreen} (радиус 3 px). Правый/нижний край — не включительно. */
    private static void drawRoundedRect(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, b, color);
        context.fill(l, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 3, color);
        context.fill(r - 3, t + 1, r - 1, t + 3, color);
        context.fill(l + 1, b - 3, l + 3, b - 1, color);
        context.fill(r - 3, b - 3, r - 1, b - 1, color);
    }

    /**
     * Треугольный «хвост» вниз (как у облачка): сверху широкий у основания плашки, снизу остриё по центру {@code apexX}.
     */
    private static void drawSpeechTail(DrawContext context, int apexX, int yTop, int rows, int halfBase, int color) {
        if (rows < 2) {
            return;
        }
        int denom = rows - 1;
        for (int i = 0; i < rows; i++) {
            int y = yTop + i;
            int half;
            if (i == rows - 1) {
                half = 0;
            } else {
                half = (halfBase * (rows - 1 - i) + denom - 1) / denom;
            }
            context.fill(apexX - half, y, apexX + half + 1, y + 1, color);
        }
    }
}
