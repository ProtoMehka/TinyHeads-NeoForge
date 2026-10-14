package com.protomehka.tinyheads.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.protomehka.tinyheads.TinyHeadType;
import com.protomehka.tinyheads.config.ModConfig;
import com.protomehka.tinyheads.init.ModBlocks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * docs/suivi_projet.md — replaces 1.12.2's LootTableLoadEvent injection. Config-driven weights
 * are read live in doApply(), unlike a static JSON loot table override, so ModConfig's weights
 * stay adjustable in-game without rebuilding the mod (ProtoMehka's choice — see conversation).
 * Mirrors the original's own pool semantics: 0-or-1 roll on whether the pool fires at all, then
 * one weighted pick among all 60 boxed/unboxed figurines if it did.
 *
 * CODEC is now a MapCodec (see ModLootModifiers.java for why) - built the same way vanilla/NeoForge
 * build every other LootModifier: RecordCodecBuilder.mapCodec wrapping LootModifier's own
 * codecStart() helper, instead of RecordCodecBuilder.create.
 */
public class TinyHeadsLootModifier extends LootModifier {

    public static final Set<ResourceLocation> TARGET_TABLES = Set.of(
            ResourceLocation.parse("minecraft:chests/simple_dungeon"),
            ResourceLocation.parse("minecraft:chests/abandoned_mineshaft"),
            ResourceLocation.parse("minecraft:chests/desert_pyramid"),
            ResourceLocation.parse("minecraft:chests/jungle_temple"),
            ResourceLocation.parse("minecraft:chests/stronghold_corridor"),
            ResourceLocation.parse("minecraft:chests/stronghold_crossing"),
            ResourceLocation.parse("minecraft:chests/village/village_weaponsmith"),
            ResourceLocation.parse("minecraft:chests/woodland_mansion"),
            ResourceLocation.parse("minecraft:chests/igloo_chest")
    );

    public static final MapCodec<TinyHeadsLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).apply(inst, TinyHeadsLootModifier::new));

    public TinyHeadsLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModConfig.ENABLE_LOOT.get()) {
            return generatedLoot;
        }
        if (!TARGET_TABLES.contains(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        RandomSource random = context.getRandom();
        // Mirrors the original pool's rolls: RandomValueRange(0, 1) — the pool fires 0 or 1 times.
        if (random.nextIntBetweenInclusive(0, 1) == 0) {
            return generatedLoot;
        }

        List<WeightedEntry> entries = new ArrayList<>(TinyHeadType.values().length * 2);
        int totalWeight = 0;
        for (TinyHeadType type : TinyHeadType.values()) {
            int boxedWeight = ModConfig.getLootWeight(type.getRarity(), true);
            int unboxedWeight = ModConfig.getLootWeight(type.getRarity(), false);
            if (boxedWeight > 0) {
                entries.add(new WeightedEntry(new ItemStack(ModBlocks.BOXED.get(type).get()), boxedWeight));
                totalWeight += boxedWeight;
            }
            if (unboxedWeight > 0) {
                entries.add(new WeightedEntry(new ItemStack(ModBlocks.UNBOXED.get(type).get()), unboxedWeight));
                totalWeight += unboxedWeight;
            }
        }
        if (entries.isEmpty() || totalWeight <= 0) {
            return generatedLoot;
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedEntry entry : entries) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                generatedLoot.add(entry.stack().copy());
                break;
            }
        }

        return generatedLoot;
    }

    private record WeightedEntry(ItemStack stack, int weight) {
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
