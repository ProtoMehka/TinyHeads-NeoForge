package com.protomehka.tinyheads.effect;

import com.protomehka.tinyheads.TinyHeadCollection;
import com.protomehka.tinyheads.blockentity.ShelfBlockEntity;
import com.protomehka.tinyheads.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Ported from 1.12.2's ShelfEffectHandler. No longer a TickEvent.WorldTickEvent scan over
 * world.loadedTileEntityList (that field doesn't exist in 1.20.1) — modern BlockEntity gets its
 * own ticker via EntityBlock#getTicker, called once per tick per loaded shelf automatically, so
 * this is invoked straight from BlockShelf's ticker instead of a global world-tick handler.
 *
 * Villager breeding lost its explicit "willing to mate" flag in modern Minecraft (verified via
 * javap: Villager#canBreed() now checks food count in their own inventory instead) — bridged here
 * by stocking their inventory with enough bread to clear BREEDING_FOOD_THRESHOLD, rather than
 * trying to replicate an API that no longer exists.
 *
 * Two genuine 1.21.1 API changes beyond plain renames (verified against the real decompiled
 * sources): BonemealableBlock#isValidBonemealTarget dropped its trailing `isClientSide` boolean
 * (now 3 args, not 4), and MobEffects' constants are Holder<MobEffect> instead of a bare MobEffect
 * (effects became a proper registry) - MobEffectInstance's constructor followed suit.
 */
public final class ShelfEffectHandler {

    private static final int POTION_INTERVAL = 60;       // 3 seconds
    private static final int BONE_MEAL_INTERVAL = 1200;  // 1 minute
    private static final int BREEDING_INTERVAL = 3600;   // 3 minutes

    private static final int MIN_BOXED_FOR_EFFECT = 3;
    private static final int BREEDING_FOOD_STOCK = 4; // bread, 4 pts each = well above the 12-pt threshold

    private ShelfEffectHandler() {
    }

    public static void tick(Level level, BlockPos pos, ShelfBlockEntity shelf) {
        if (!ModConfig.ENABLE_SHELF_EFFECTS.get()) return;

        long time = level.getGameTime();
        boolean checkPotion = time % POTION_INTERVAL == 0;
        boolean checkBoneMeal = time % BONE_MEAL_INTERVAL == 0;
        boolean checkBreeding = time % BREEDING_INTERVAL == 0;
        if (!checkPotion && !checkBoneMeal && !checkBreeding) return;

        if (shelf.getBoxedCount() < MIN_BOXED_FOR_EFFECT) return;

        TinyHeadCollection collection = shelf.getCollection();
        if (collection == null) return;

        int radius = ModConfig.SHELF_EFFECT_RADIUS.get();

        if (checkPotion && collection.hasPotionEffect()) {
            applyPotionEffect(level, pos, radius, collection.getPotionEffect());
        }

        if (collection.hasCustomEffect()) {
            TinyHeadCollection.CustomEffect effect = collection.getCustomEffect();
            if (checkBoneMeal && effect == TinyHeadCollection.CustomEffect.BONE_MEAL) {
                applyBoneMeal(level, pos, radius);
            }
            if (checkBreeding && effect == TinyHeadCollection.CustomEffect.BREEDING) {
                applyBreeding(level, pos, radius);
            }
        }
    }

    private static void applyPotionEffect(Level level, BlockPos pos, int radius, Holder<MobEffect> effect) {
        AABB area = new AABB(pos).inflate(radius);
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            // 15s duration, refreshed every 3s so it never flickers below 10s.
            player.addEffect(new MobEffectInstance(effect, 300, 0, true, true));
        }
    }

    private static void applyBreeding(Level level, BlockPos pos, int radius) {
        AABB area = new AABB(pos).inflate(radius);

        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);
        for (Animal animal : animals) {
            if (animal.getAge() == 0 && !animal.isInLove()) {
                animal.setInLove(null);
            }
        }

        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, area);
        for (Villager villager : villagers) {
            if (villager.getAge() == 0 && !villager.canBreed()) {
                villager.getInventory().addItem(new net.minecraft.world.item.ItemStack(Items.BREAD, BREEDING_FOOD_STOCK));
            }
        }
    }

    private static void applyBoneMeal(Level level, BlockPos pos, int radius) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        int yMin = pos.getY() - 3;
        int yMax = pos.getY() + 3;
        int xMin = pos.getX() - radius;
        int xMax = pos.getX() + radius;
        int zMin = pos.getZ() - radius;
        int zMax = pos.getZ() + radius;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                for (int y = yMin; y <= yMax; y++) {
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);
                    if (state.getBlock() instanceof GrassBlock) continue; // avoid tall-grass/flower spam

                    if (state.getBlock() instanceof BonemealableBlock growable) {
                        if (growable.isValidBonemealTarget(level, mutablePos, state)
                                && growable.isBonemealSuccess(level, level.getRandom(), mutablePos, state)) {
                            growable.performBonemeal(serverLevel, level.getRandom(), mutablePos, state);
                        }
                    }
                }
            }
        }
    }
}
