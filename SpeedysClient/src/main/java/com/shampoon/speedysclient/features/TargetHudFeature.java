/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.PlayerSkinDrawer
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.util.SkinTextures
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  org.lwjgl.glfw.GLFW
 */
package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public final class TargetHudFeature {
    private static int offsetX = 0;
    private static int offsetY = 28;
    private static boolean dragging = false;
    private static int dragX;
    private static int dragY;

    private TargetHudFeature() {
    }

    public static void render(DrawContext context) {
        EntityHitResult eHit;
        Entity e;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || PVPUtils.CONFIG == null || !PVPUtils.CONFIG.targetHudEnabled) {
            return;
        }
        Entity targetEntity = null;
        HitResult hitResult = client.crosshairTarget;
        if (hitResult instanceof EntityHitResult && (e = (eHit = (EntityHitResult) hitResult).getEntity()) instanceof PlayerEntity && e.isAlive()) {
            targetEntity = e;
        }
        if (!(targetEntity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        AbstractClientPlayerEntity target = (AbstractClientPlayerEntity) targetEntity;
        String name = target.getGameProfile().getName();
        Text tabTag = TargetHudFeature.resolveTabTag(client, target);
        boolean showOffhandText = PVPUtils.CONFIG.targetHudShowOffhand;
        ItemStack offStack = target.getOffHandStack();
        Text offhandLine = null;
        if (showOffhandText) {
            offhandLine = offStack.isEmpty()
                    ? Text.literal("\u041f\u0443\u0441\u0442\u043e")
                    : offStack.getName();
        }
        int nameW = client.textRenderer.getWidth(name);
        int tagScaledW = tabTag != null ? (int) ((float) client.textRenderer.getWidth(tabTag) * 0.62f) + 4 : 0;
        int w = Math.max(140, 48 + nameW + Math.max(0, tagScaledW));
        if (offhandLine != null) {
            w = Math.max(w, 48 + client.textRenderer.getWidth(offhandLine));
        }
        int h = showOffhandText ? 54 : 42;
        int x = context.getScaledWindowWidth() / 2 + offsetX;
        int y = context.getScaledWindowHeight() / 2 + offsetY;
        TargetHudFeature.drawRoundedRect(context, x, y, x + w, y + h, -804253158);
        TargetHudFeature.drawRoundedOutline(context, x, y, x + w, y + h, -14534042);
        PlayerSkinDrawer.draw((DrawContext) context, (SkinTextures) target.getSkinTextures(), x + 5, y + 5, 30);
        int textLeft = x + 40;
        context.drawText(client.textRenderer, name, textLeft, y + 7, -1, false);
        if (tabTag != null) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(textLeft + (float) nameW + 3f, y + 11f);
            context.getMatrices().scale(0.62f, 0.62f);
            context.drawText(client.textRenderer, tabTag, 0, 0, -5592406, false);
            context.getMatrices().popMatrix();
        }
        String hp = target.isInvisible() ? "HP: ?" : "HP: " + Math.round(target.getHealth());
        context.drawText(client.textRenderer, hp, textLeft, y + 21, -4599553, false);
        if (offhandLine != null) {
            int maxOffW = w - 48;
            TargetHudFeature.drawTextTruncated(context, client, offhandLine, textLeft, y + 34, maxOffW, -5592406);
        }
        TargetHudFeature.handleDrag(client, x, y, w, h);
    }

    private static void drawTextTruncated(DrawContext context, MinecraftClient client, Text text, int tx, int ty, int maxW, int color) {
        int tw = client.textRenderer.getWidth(text);
        if (tw <= maxW) {
            context.drawText(client.textRenderer, text, tx, ty, color, false);
            return;
        }
        String s = text.getString();
        while (s.length() > 1 && client.textRenderer.getWidth(s + "\u2026") > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        context.drawText(client.textRenderer, s + "\u2026", tx, ty, color, false);
    }

    /** Префикс/суффикс команды scoreboard или кастомное имя из таба, если отличается от ника. */
    private static Text resolveTabTag(MinecraftClient client, AbstractClientPlayerEntity target) {
        Team team = target.getScoreboardTeam();
        if (team != null) {
            MutableText t = Text.empty().append(team.getPrefix()).append(team.getSuffix());
            if (!t.getString().trim().isEmpty()) {
                return t;
            }
        }
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(target.getUuid());
            if (entry != null) {
                Text dn = entry.getDisplayName();
                if (dn != null) {
                    String nick = target.getGameProfile().getName();
                    if (!dn.getString().strip().equals(nick)) {
                        return dn;
                    }
                }
            }
        }
        return null;
    }

    private static void handleDrag(MinecraftClient client, int x, int y, int w, int h) {
        boolean lmb;
        if (!(client.currentScreen instanceof ChatScreen)) {
            dragging = false;
            return;
        }
        int mx = (int) (client.mouse.getX() * (double) client.getWindow().getScaledWidth()
                / (double) Math.max(1, client.getWindow().getFramebufferWidth()));
        int my = (int) (client.mouse.getY() * (double) client.getWindow().getScaledHeight()
                / (double) Math.max(1, client.getWindow().getFramebufferHeight()));
        boolean bl = lmb = GLFW.glfwGetMouseButton((long) client.getWindow().getHandle(), (int) 0) == 1;
        if (lmb) {
            if (!dragging && mx >= x && mx <= x + w && my >= y && my <= y + h) {
                dragging = true;
                dragX = mx - x;
                dragY = my - y;
            }
            if (dragging) {
                offsetX = mx - dragX - client.getWindow().getScaledWidth() / 2;
                offsetY = my - dragY - client.getWindow().getScaledHeight() / 2;
            }
        } else {
            dragging = false;
        }
    }

    private static void drawRoundedRect(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, b, color);
        context.fill(l, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 3, color);
        context.fill(r - 3, t + 1, r - 1, t + 3, color);
        context.fill(l + 1, b - 3, l + 3, b - 1, color);
        context.fill(r - 3, b - 3, r - 1, b - 1, color);
    }

    private static void drawRoundedOutline(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, t + 1, color);
        context.fill(l + 3, b - 1, r - 3, b, color);
        context.fill(l, t + 3, l + 1, b - 3, color);
        context.fill(r - 1, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 2, color);
        context.fill(r - 3, t + 1, r - 1, t + 2, color);
        context.fill(l + 1, b - 2, l + 3, b - 1, color);
        context.fill(r - 3, b - 2, r - 1, b - 1, color);
    }
}
