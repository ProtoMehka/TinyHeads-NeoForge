package com.protomehka.tinyheads.loot;

import com.mojang.serialization.MapCodec;
import com.protomehka.tinyheads.TinyHeads;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Genuine (non-rename) API change, not just a package move: IGlobalLootModifier#codec() now
 * returns MapCodec<? extends IGlobalLootModifier>, not Codec<? extends IGlobalLootModifier> as in
 * Forge 1.20.1 (verified against the real decompiled IGlobalLootModifier.java/LootModifier.java in
 * the NeoForge 21.1.259 sources jar). The registry itself also moved from Forge's own
 * ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS to NeoForgeRegistries.Keys of the same
 * name, holding MapCodec entries instead of Codec entries. See TinyHeadsLootModifier for the
 * matching MapCodec change on the implementation side.
 */
public final class ModLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TinyHeads.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<TinyHeadsLootModifier>> FIGURINES =
            LOOT_MODIFIER_SERIALIZERS.register("figurines", () -> TinyHeadsLootModifier.CODEC);

    private ModLootModifiers() {
    }

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}
