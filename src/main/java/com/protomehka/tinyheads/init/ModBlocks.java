package com.protomehka.tinyheads.init;

import com.protomehka.tinyheads.TinyHeadType;
import com.protomehka.tinyheads.TinyHeads;
import com.protomehka.tinyheads.block.BlockShelf;
import com.protomehka.tinyheads.block.BlockTinyHead;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ForgeRegistries.BLOCKS -> BuiltInRegistries.BLOCK (vanilla registries aren't wrapped by
 * ForgeRegistries on the NeoForge side anymore), RegistryObject<Block> -> DeferredHolder<Block,
 * Block> - kept un-narrowed (not DeferredBlock) to match the original's own un-narrowed
 * RegistryObject<Block> style, minimizing the diff.
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, TinyHeads.MODID);

    public static final Map<TinyHeadType, DeferredHolder<Block, Block>> BOXED = new LinkedHashMap<>();
    public static final Map<TinyHeadType, DeferredHolder<Block, Block>> UNBOXED = new LinkedHashMap<>();

    public static final DeferredHolder<Block, Block> SHELF = BLOCKS.register("shelf",
            () -> new BlockShelf(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5f, 5.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    static {
        for (TinyHeadType type : TinyHeadType.values()) {
            BOXED.put(type, BLOCKS.register(type.getBoxedRegistryName(),
                    () -> new BlockTinyHead(figureProperties(), true, type)));
            UNBOXED.put(type, BLOCKS.register(type.getUnboxedRegistryName(),
                    () -> new BlockTinyHead(figureProperties(), false, type)));
        }
    }

    private static BlockBehaviour.Properties figureProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                .strength(0.5f, 1.0f)
                .sound(SoundType.STONE)
                .noOcclusion();
    }

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
