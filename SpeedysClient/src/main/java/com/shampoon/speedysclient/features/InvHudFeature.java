package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.ui.InvHudScaleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class InvHudFeature {
    private static final int COLS = 9;
    private static final int ROWS = 3;
    private static final int CELL = 18;
    private static final int SLOT_BG = 0x70000000;
    private static final int SLOT_OUTLINE = 0xA0404040;
    private static boolean dragging = false;
    private static int dragGrabX;
    private static int dragGrabY;
    private static boolean wasRightDown;

    private InvHudFeature() {
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || PVPUtils.CONFIG == null || !PVPUtils.CONFIG.invHudEnabled) {
            dragging = false;
            return;
        }
        ClientPlayerEntity player = client.player;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        float scale = PVPUtils.CONFIG.invHudScale;
        int baseX = sw / 2 + 92 + PVPUtils.CONFIG.invHudOffsetX;
        int baseY = sh - 58 + PVPUtils.CONFIG.invHudOffsetY;
        int gridW = COLS * CELL;
        int gridH = ROWS * CELL;

        handleDragAndRightClick(client, baseX, baseY, gridW, gridH, scale, sw, sh);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)baseX, (float)baseY);
        context.getMatrices().scale(scale, scale);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * CELL;
                int y = row * CELL;
                context.fill(x, y, x + 16, y + 16, SLOT_BG);
                context.drawBorder(x, y, 16, 16, SLOT_OUTLINE);
                int invSlot = 9 + row * COLS + col;
                ItemStack stack = player.getInventory().getStack(invSlot);
                if (stack.isEmpty()) {
                    continue;
                }
                context.drawItem(stack, x, y);
                context.drawStackOverlay(client.textRenderer, stack, x, y);
            }
        }
        context.getMatrices().popMatrix();
    }

    private static void handleDragAndRightClick(MinecraftClient client, int x, int y, int w, int h, float scale, int sw, int sh) {
        if (!(client.currentScreen instanceof ChatScreen)) {
            if (dragging) {
                PVPUtils.CONFIG.save();
            }
            dragging = false;
            wasRightDown = false;
            return;
        }
        int mx = (int)(client.mouse.getX() * (double)sw / (double)Math.max(1, client.getWindow().getFramebufferWidth()));
        int my = (int)(client.mouse.getY() * (double)sh / (double)Math.max(1, client.getWindow().getFramebufferHeight()));
        boolean lmb = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDownNow = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        int scaledW = Math.max(1, Math.round((float)w * scale));
        int scaledH = Math.max(1, Math.round((float)h * scale));
        boolean inside = mx >= x && mx <= x + scaledW && my >= y && my <= y + scaledH;
        if (inside && rightDownNow && !wasRightDown) {
            client.setScreen(new InvHudScaleScreen(client.currentScreen));
        }
        wasRightDown = rightDownNow;
        if (lmb) {
            if (!dragging && inside) {
                dragging = true;
                dragGrabX = mx - x;
                dragGrabY = my - y;
            }
            if (dragging) {
                PVPUtils.CONFIG.invHudOffsetX = mx - dragGrabX - (sw / 2 + 92);
                PVPUtils.CONFIG.invHudOffsetY = my - dragGrabY - (sh - 58);
            }
        } else if (dragging) {
            dragging = false;
            PVPUtils.CONFIG.save();
        }
    }
}
