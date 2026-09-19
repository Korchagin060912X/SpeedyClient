package com.shampoon.speedysclient.features;



import com.shampoon.pvputils.PVPUtils;

import java.util.Set;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.DrawContext;

import net.minecraft.client.gui.screen.ChatScreen;

import net.minecraft.client.render.RenderTickCounter;

import net.minecraft.component.DataComponentTypes;

import net.minecraft.component.type.PotionContentsComponent;

import net.minecraft.entity.effect.StatusEffect;

import net.minecraft.entity.effect.StatusEffectInstance;

import net.minecraft.entity.effect.StatusEffects;

import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.item.ItemStack;

import net.minecraft.item.Items;

import net.minecraft.registry.entry.RegistryEntry;



/**

 * Подсветка только взрывных (splash) зелий по балансу «хороших» и «плохих» эффектов.

 */

public final class PotionHighlighterFeature {



    public enum Tier {

        BAD,

        MIXED,

        GOOD

    }



    private static final Set<RegistryEntry<StatusEffect>> GOOD_EFFECTS = Set.of(

            StatusEffects.STRENGTH,

            StatusEffects.SPEED,

            StatusEffects.FIRE_RESISTANCE,

            StatusEffects.REGENERATION,

            StatusEffects.INSTANT_HEALTH,

            StatusEffects.RESISTANCE,

            StatusEffects.ABSORPTION,

            StatusEffects.NIGHT_VISION,

            StatusEffects.INVISIBILITY,

            StatusEffects.JUMP_BOOST,

            StatusEffects.WATER_BREATHING,

            StatusEffects.HEALTH_BOOST,

            StatusEffects.DOLPHINS_GRACE,

            StatusEffects.HASTE,

            StatusEffects.LUCK,

            StatusEffects.SATURATION);



    private static final Set<RegistryEntry<StatusEffect>> BAD_EFFECTS = Set.of(

            StatusEffects.INSTANT_DAMAGE,

            StatusEffects.POISON,

            StatusEffects.SLOWNESS,

            StatusEffects.WEAKNESS,

            StatusEffects.WITHER,

            StatusEffects.NAUSEA,

            StatusEffects.BLINDNESS,

            StatusEffects.GLOWING,

            StatusEffects.LEVITATION,

            StatusEffects.MINING_FATIGUE,

            StatusEffects.DARKNESS,

            StatusEffects.HUNGER,

            StatusEffects.UNLUCK);



    private PotionHighlighterFeature() {

    }



    /**

     * Тир подсветки только для {@link Items#SPLASH_POTION}; обычные и тягучие зелья не учитываются.

     */

    public static Tier tierForSplashPotion(ItemStack stack) {

        if (stack.isEmpty() || !stack.isOf(Items.SPLASH_POTION)) {

            return null;

        }

        return classifyEffects(stack);

    }



    /**

     * Полупрозрачная заливка 16×16 без обводки и без мигания.

     */

    public static void drawSlotFillStatic(DrawContext context, int itemX, int itemY, Tier tier) {

        if (PVPUtils.CONFIG == null || tier == null) {

            return;

        }

        int r;

        int g;

        int b;

        switch (tier) {

            case BAD:

                r = PVPUtils.CONFIG.potionHlBadR & 255;

                g = PVPUtils.CONFIG.potionHlBadG & 255;

                b = PVPUtils.CONFIG.potionHlBadB & 255;

                break;

            case MIXED:

                r = PVPUtils.CONFIG.potionHlMixR & 255;

                g = PVPUtils.CONFIG.potionHlMixG & 255;

                b = PVPUtils.CONFIG.potionHlMixB & 255;

                break;

            case GOOD:

                r = PVPUtils.CONFIG.potionHlGoodR & 255;

                g = PVPUtils.CONFIG.potionHlGoodG & 255;

                b = PVPUtils.CONFIG.potionHlGoodB & 255;

                break;

            default:

                return;

        }

        int aFill = 68;

        int w = 16;

        int h = 16;

        int fillArgb = (aFill << 24) | (r << 16) | (g << 8) | b;

        context.fill(itemX, itemY, itemX + w, itemY + h, fillArgb);

    }



    public static void renderHudOverlays(DrawContext context, RenderTickCounter tickCounter) {

        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.potionHighlighterEnabled) {

            return;

        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {

            return;

        }

        if (client.currentScreen != null && !(client.currentScreen instanceof ChatScreen)) {

            return;

        }

        Tier t = tierForSplashPotion(client.player.getOffHandStack());

        if (t == null) {

            return;

        }

        int sw = context.getScaledWindowWidth();

        int sh = context.getScaledWindowHeight();

        int x = sw / 2 - 91 - 26;

        int y = sh - 22;

        drawSlotFillStatic(context, x, y, t);

    }



    private static Tier classifyEffects(ItemStack stack) {

        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);

        if (contents == null || !contents.hasEffects()) {

            return null;

        }

        int good = 0;

        int bad = 0;

        for (StatusEffectInstance inst : contents.getEffects()) {

            RegistryEntry<StatusEffect> type = inst.getEffectType();

            if (GOOD_EFFECTS.contains(type)) {

                good++;

            } else if (BAD_EFFECTS.contains(type)) {

                bad++;

            }

        }

        if (good == 0 && bad == 0) {

            return null;

        }

        if (bad > good) {

            return Tier.BAD;

        }

        if (good > bad) {

            return Tier.GOOD;

        }

        return Tier.MIXED;

    }

}

