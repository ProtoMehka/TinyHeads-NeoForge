package com.protomehka.tinyheads.config;

import com.protomehka.tinyheads.TinyHeadRarity;
import net.neoforged.neoforge.common.ModConfigSpec;

/** ForgeConfigSpec -> ModConfigSpec is a pure rename; Builder/BooleanValue/IntValue/define/
 * defineInRange/comment/push/pop are all otherwise identical in shape. */
public final class ModConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SHELF_EFFECTS;
    public static final ModConfigSpec.IntValue SHELF_EFFECT_RADIUS;

    public static final ModConfigSpec.BooleanValue ENABLE_LOOT;
    public static final ModConfigSpec.IntValue LOOT_WEIGHT_COMMON_UNBOXED;
    public static final ModConfigSpec.IntValue LOOT_WEIGHT_COMMON_BOXED;
    public static final ModConfigSpec.IntValue LOOT_WEIGHT_UNCOMMON_UNBOXED;
    public static final ModConfigSpec.IntValue LOOT_WEIGHT_UNCOMMON_BOXED;
    public static final ModConfigSpec.IntValue LOOT_WEIGHT_RARE_UNBOXED;
    public static final ModConfigSpec.IntValue LOOT_WEIGHT_RARE_BOXED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Shelf collection effects").push("shelf");
        ENABLE_SHELF_EFFECTS = builder
                .comment("Enable collection effects when figurines are placed on a shelf")
                .define("enableShelfEffects", true);
        SHELF_EFFECT_RADIUS = builder
                .comment("Radius (in blocks) of the shelf collection effect")
                .defineInRange("shelfEffectRadius", 64, 1, 256);
        builder.pop();

        builder.comment("Figurines in vanilla loot tables — applied live via a Global Loot Modifier,",
                        "so changes here take effect without needing to rebuild the mod.")
                .push("loot");
        ENABLE_LOOT = builder
                .comment("Enable figurines appearing in vanilla loot tables")
                .define("enableLoot", true);
        LOOT_WEIGHT_COMMON_UNBOXED = builder.comment("Loot weight for Common unboxed figurines").defineInRange("weightCommonUnboxed", 50, 0, 1000);
        LOOT_WEIGHT_COMMON_BOXED = builder.comment("Loot weight for Common boxed figurines").defineInRange("weightCommonBoxed", 40, 0, 1000);
        LOOT_WEIGHT_UNCOMMON_UNBOXED = builder.comment("Loot weight for Uncommon unboxed figurines").defineInRange("weightUncommonUnboxed", 30, 0, 1000);
        LOOT_WEIGHT_UNCOMMON_BOXED = builder.comment("Loot weight for Uncommon boxed figurines").defineInRange("weightUncommonBoxed", 20, 0, 1000);
        LOOT_WEIGHT_RARE_UNBOXED = builder.comment("Loot weight for Rare unboxed figurines").defineInRange("weightRareUnboxed", 10, 0, 1000);
        LOOT_WEIGHT_RARE_BOXED = builder.comment("Loot weight for Rare boxed figurines").defineInRange("weightRareBoxed", 5, 0, 1000);
        builder.pop();

        SPEC = builder.build();
    }

    public static int getLootWeight(TinyHeadRarity rarity, boolean boxed) {
        return switch (rarity) {
            case COMMON -> boxed ? LOOT_WEIGHT_COMMON_BOXED.get() : LOOT_WEIGHT_COMMON_UNBOXED.get();
            case UNCOMMON -> boxed ? LOOT_WEIGHT_UNCOMMON_BOXED.get() : LOOT_WEIGHT_UNCOMMON_UNBOXED.get();
            case RARE -> boxed ? LOOT_WEIGHT_RARE_BOXED.get() : LOOT_WEIGHT_RARE_UNBOXED.get();
        };
    }

    private ModConfig() {
    }
}
