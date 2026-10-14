package com.protomehka.tinyheads.init;

import com.protomehka.tinyheads.TinyHeadType;
import com.protomehka.tinyheads.TinyHeads;
import com.protomehka.tinyheads.item.BlockItemTinyHead;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, TinyHeads.MODID);

    public static final Map<TinyHeadType, DeferredHolder<Item, Item>> BOXED = new LinkedHashMap<>();
    public static final Map<TinyHeadType, DeferredHolder<Item, Item>> UNBOXED = new LinkedHashMap<>();

    public static final DeferredHolder<Item, Item> SHELF = ITEMS.register("shelf",
            () -> new BlockItem(ModBlocks.SHELF.get(), new Item.Properties()));

    static {
        for (TinyHeadType type : TinyHeadType.values()) {
            BOXED.put(type, ITEMS.register(type.getBoxedRegistryName(),
                    () -> new BlockItemTinyHead((com.protomehka.tinyheads.block.BlockTinyHead) ModBlocks.BOXED.get(type).get(), new Item.Properties())));
            UNBOXED.put(type, ITEMS.register(type.getUnboxedRegistryName(),
                    () -> new BlockItemTinyHead((com.protomehka.tinyheads.block.BlockTinyHead) ModBlocks.UNBOXED.get(type).get(), new Item.Properties())));
        }
    }

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
