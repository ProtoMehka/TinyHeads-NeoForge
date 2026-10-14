package com.protomehka.tinyheads;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import static com.protomehka.tinyheads.TinyHeadCollection.*;
import static com.protomehka.tinyheads.TinyHeadRarity.*;

/**
 * Implements StringRepresentable (serialized name = id) so it can back a plain string Codec, as
 * required by BlockTinyHead's new codec() (see block/BlockTinyHead.java) - BlockBehaviour now
 * declares codec() as abstract in 1.21.1, which didn't exist in 1.20.1. StringRepresentable.fromEnum
 * is the same idiom vanilla uses for its own small internal enums (e.g. SkullBlock.Type) - this enum
 * is purely internal to the mod and never needs to be datapack-overridable, so a plain by-id lookup
 * is all that's needed.
 */
public enum TinyHeadType implements StringRepresentable {

    // League of Legends
    TEEMO("teemo", "Teemo", LEAGUE_OF_LEGENDS, COMMON),
    ASHE("ashe", "Ashe", LEAGUE_OF_LEGENDS, UNCOMMON),
    JINX("jinx", "Jinx", LEAGUE_OF_LEGENDS, RARE),

    // Marvel
    HULK("hulk", "Hulk", MARVEL, COMMON),
    SPIDER_MAN("spider_man", "Spider Man", MARVEL, UNCOMMON),
    IRON_MAN("iron_man", "Iron Man", MARVEL, RARE),

    // Rick and Morty
    MORTY("morty", "Morty", RICK_AND_MORTY, COMMON),
    RICK("rick", "Rick", RICK_AND_MORTY, UNCOMMON),
    MR_MEESEEKS("mr_meeseeks", "Mr Meeseeks", RICK_AND_MORTY, RARE),

    // Minecraft
    STEVE("steve", "Steve", MINECRAFT, COMMON),
    CREEPER("creeper", "Creeper", MINECRAFT, UNCOMMON),
    ENDERMAN("enderman", "Enderman", MINECRAFT, RARE),

    // South Park
    CARTMAN("cartman", "Cartman", SOUTH_PARK, COMMON),
    RANDY("randy", "Randy", SOUTH_PARK, UNCOMMON),
    MYSTERION("mysterion", "Mysterion", SOUTH_PARK, RARE),

    // DC Comics
    BATMAN("batman", "Batman", DC_COMICS, COMMON),
    FLASH("flash", "Flash", DC_COMICS, UNCOMMON),
    SUPERMAN("superman", "Superman", DC_COMICS, RARE),

    // Star Wars
    CHEWBACCA("chewbacca", "Chewbacca", STAR_WARS, COMMON),
    MANDALORIAN("mandalorian", "Mandalorian", STAR_WARS, UNCOMMON),
    DARK_VADOR("dark_vador", "Dark Vador", STAR_WARS, RARE),

    // Horror
    SLENDERMAN("slenderman", "Slenderman", HORROR, COMMON),
    MICHAEL_MYERS("michael_myers", "Michael Myers", HORROR, UNCOMMON),
    CARTOON_CAT("cartoon_cat", "Cartoon Cat", HORROR, RARE),

    // Touhou Project
    SAKUYA_IZAYOI("sakuya_izayoi", "Sakuya Izayoi", TOUHOU_PROJECT, COMMON),
    HONG_MEILING("hong_meiling", "Hong Meiling", TOUHOU_PROJECT, UNCOMMON),
    REIMU_HAKUREI("reimu_hakurei", "Reimu Hakurei", TOUHOU_PROJECT, RARE),

    // Date a Live
    YATOGAMI_TOHKA("yatogami_tohka", "Yatogami Tohka", DATE_A_LIVE, COMMON),
    YOSHINO_HIMEKAWA("yoshino_himekawa", "Yoshino Himekawa", DATE_A_LIVE, UNCOMMON),
    TOKISAKI_KURUMI("tokisaki_kurumi", "Tokisaki Kurumi", DATE_A_LIVE, RARE);

    public static final Codec<TinyHeadType> CODEC = StringRepresentable.fromEnum(TinyHeadType::values);

    private final String id;
    private final String displayName;
    private final TinyHeadCollection collection;
    private final TinyHeadRarity rarity;

    TinyHeadType(String id, String displayName, TinyHeadCollection collection, TinyHeadRarity rarity) {
        this.id = id;
        this.displayName = displayName;
        this.collection = collection;
        this.rarity = rarity;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TinyHeadCollection getCollection() {
        return collection;
    }

    public TinyHeadRarity getRarity() {
        return rarity;
    }

    public String getBoxedRegistryName() {
        return "tinyhead_" + id + "_boxed";
    }

    public String getUnboxedRegistryName() {
        return "tinyhead_" + id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
