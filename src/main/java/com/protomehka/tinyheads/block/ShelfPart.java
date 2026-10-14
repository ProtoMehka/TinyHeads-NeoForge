package com.protomehka.tinyheads.block;

import net.minecraft.util.StringRepresentable;

public enum ShelfPart implements StringRepresentable {

    BOTTOM_LEFT(0, "bottom_left"),
    BOTTOM_RIGHT(1, "bottom_right");

    private final int index;
    private final String name;

    ShelfPart(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
