package com.protomehka.tinyheads;

import com.protomehka.tinyheads.config.ModConfig;
import com.protomehka.tinyheads.init.ModBlockEntities;
import com.protomehka.tinyheads.init.ModBlocks;
import com.protomehka.tinyheads.init.ModItems;
import com.protomehka.tinyheads.loot.ModLootModifiers;
import com.protomehka.tinyheads.tab.ModCreativeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;

/**
 * Ported from the Forge 1.20.1 version. The constructor shape changed: NeoForge's main mod class
 * now takes (IEventBus, ModContainer) directly instead of a single FMLJavaModLoadingContext, and
 * config registration moved from context.registerConfig(...) to modContainer.registerConfig(...).
 */
@Mod(TinyHeads.MODID)
public class TinyHeads {

    public static final String MODID = "tinyheads";

    public TinyHeads(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModCreativeTab.register(modEventBus);
    }
}
