package com.protomehka.tinyheads;

import net.minecraft.ChatFormatting;

public enum TinyHeadRarity {

    COMMON("common", ChatFormatting.WHITE),
    UNCOMMON("uncommon", ChatFormatting.GREEN),
    RARE("rare", ChatFormatting.BLUE);

    private final String id;
    private final ChatFormatting color;

    TinyHeadRarity(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public ChatFormatting getColor() {
        return color;
    }
}
