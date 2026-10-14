package com.protomehka.tinyheads;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import javax.annotation.Nullable;

/**
 * Field names are unchanged from the Forge 1.20.1 port (DAMAGE_BOOST, DAMAGE_RESISTANCE, DIG_SPEED,
 * MOVEMENT_SPEED, JUMP, REGENERATION, LUCK, NIGHT_VISION). What DID change (verified against the
 * real decompiled MobEffects.java): every constant is now Holder<MobEffect>, not a plain MobEffect
 * - effects became a proper datapack-registerable registry. MobEffectInstance's constructors moved
 * in step (Holder<MobEffect> instead of MobEffect), so this holds up the whole chain unchanged in
 * shape, just wrapped in Holder<>.
 */
public enum TinyHeadCollection {

    LEAGUE_OF_LEGENDS("lol", "league_of_legends", MobEffects.DAMAGE_BOOST, null),
    MARVEL("marvel", "marvel", MobEffects.DAMAGE_RESISTANCE, null),
    RICK_AND_MORTY("rick_and_morty", "rick_and_morty", MobEffects.REGENERATION, null),
    MINECRAFT("minecraft", "minecraft", MobEffects.DIG_SPEED, null),
    SOUTH_PARK("south_park", "south_park", MobEffects.LUCK, null),
    DC_COMICS("dc_comics", "dc_comics", MobEffects.MOVEMENT_SPEED, null),
    STAR_WARS("star_wars", "star_wars", MobEffects.JUMP, null),
    HORROR("horror", "horror", MobEffects.NIGHT_VISION, null),
    TOUHOU_PROJECT("touhou", "touhou_project", null, CustomEffect.BREEDING),
    DATE_A_LIVE("date_a_live", "date_a_live", null, CustomEffect.BONE_MEAL);

    public enum CustomEffect {
        BREEDING,
        BONE_MEAL
    }

    private final String id;
    private final String langKey;
    @Nullable
    private final Holder<MobEffect> potionEffect;
    @Nullable
    private final CustomEffect customEffect;

    TinyHeadCollection(String id, String langKey, @Nullable Holder<MobEffect> potionEffect, @Nullable CustomEffect customEffect) {
        this.id = id;
        this.langKey = langKey;
        this.potionEffect = potionEffect;
        this.customEffect = customEffect;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return "tinyheads.collection." + langKey;
    }

    @Nullable
    public Holder<MobEffect> getPotionEffect() {
        return potionEffect;
    }

    @Nullable
    public CustomEffect getCustomEffect() {
        return customEffect;
    }

    public boolean hasPotionEffect() {
        return potionEffect != null;
    }

    public boolean hasCustomEffect() {
        return customEffect != null;
    }
}
