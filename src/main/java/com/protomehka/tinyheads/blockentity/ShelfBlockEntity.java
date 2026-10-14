package com.protomehka.tinyheads.blockentity;

import com.protomehka.tinyheads.TinyHeadCollection;
import com.protomehka.tinyheads.block.BlockTinyHead;
import com.protomehka.tinyheads.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Ported from the Forge 1.20.1 version. BlockEntity's save/load methods gained a
 * HolderLookup.Provider parameter in 1.21.1 (saveAdditional/load -> saveAdditional/loadAdditional,
 * both now take registries; getUpdateTag too) - part of the same data-component rework that changed
 * ItemStack serialization: ItemStack#save(CompoundTag) -> ItemStack#save(HolderLookup.Provider, Tag)
 * and the static ItemStack#of(CompoundTag) -> ItemStack#parse(HolderLookup.Provider, Tag), which
 * now returns Optional<ItemStack> instead of a plain ItemStack. The registries are threaded through
 * from whatever BlockEntity's caller already has - no new state needed here.
 */
public class ShelfBlockEntity extends BlockEntity {

    public static final int MAX_SLOTS = 5;
    // v1.0: only 3 slots usable (common, uncommon, rare); v2.0 unlocks slots 3 and 4 (epic, legendary)
    public static final int AVAILABLE_SLOTS = 3;

    private final ItemStack[] figurines = new ItemStack[MAX_SLOTS];
    private int figurineCount = 0;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHELF.get(), pos, state);
        for (int i = 0; i < MAX_SLOTS; i++) {
            figurines[i] = ItemStack.EMPTY;
        }
    }

    @Nullable
    public TinyHeadCollection getCollection() {
        for (ItemStack stack : figurines) {
            if (!stack.isEmpty()) {
                var type = getTypeFromStack(stack);
                if (type != null) {
                    return type.getCollection();
                }
            }
        }
        return null;
    }

    public ItemStack addFigurine(ItemStack stack) {
        if (figurineCount >= AVAILABLE_SLOTS) return stack;

        var type = getTypeFromStack(stack);
        if (type == null) return stack;

        TinyHeadCollection shelfCollection = getCollection();
        if (shelfCollection != null && type.getCollection() != shelfCollection) {
            return stack;
        }

        for (ItemStack existing : figurines) {
            if (!existing.isEmpty()) {
                var existingType = getTypeFromStack(existing);
                if (existingType == type) {
                    BlockTinyHead existingBlock = getBlockFromStack(existing);
                    BlockTinyHead newBlock = getBlockFromStack(stack);
                    if (existingBlock != null && newBlock != null
                            && existingBlock.isBoxed() == newBlock.isBoxed()) {
                        return stack;
                    }
                }
            }
        }

        for (int i = 0; i < AVAILABLE_SLOTS; i++) {
            if (figurines[i].isEmpty()) {
                ItemStack single = stack.copy();
                single.setCount(1);
                figurines[i] = single;
                figurineCount++;
                markDirtyAndSync();
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    public ItemStack removeLastFigurine() {
        for (int i = AVAILABLE_SLOTS - 1; i >= 0; i--) {
            if (!figurines[i].isEmpty()) {
                ItemStack removed = figurines[i];
                figurines[i] = ItemStack.EMPTY;
                figurineCount--;
                markDirtyAndSync();
                return removed;
            }
        }
        return ItemStack.EMPTY;
    }

    public void dropAllFigurines(Level level, BlockPos dropPos) {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (!figurines[i].isEmpty()) {
                level.addFreshEntity(new ItemEntity(level,
                        dropPos.getX() + 0.5, dropPos.getY() + 0.5, dropPos.getZ() + 0.5,
                        figurines[i]));
                figurines[i] = ItemStack.EMPTY;
            }
        }
        figurineCount = 0;
    }

    public int getBoxedCount() {
        int count = 0;
        for (ItemStack stack : figurines) {
            if (!stack.isEmpty()) {
                BlockTinyHead block = getBlockFromStack(stack);
                if (block != null && block.isBoxed()) {
                    count++;
                }
            }
        }
        return count;
    }

    public ItemStack getFigurine(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return ItemStack.EMPTY;
        return figurines[slot];
    }

    public int getFigurineCount() {
        return figurineCount;
    }

    @Nullable
    public static com.protomehka.tinyheads.TinyHeadType getTypeFromStack(ItemStack stack) {
        BlockTinyHead block = getBlockFromStack(stack);
        return block != null ? block.getHeadType() : null;
    }

    @Nullable
    public static BlockTinyHead getBlockFromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        return blockItem.getBlock() instanceof BlockTinyHead tinyHead ? tinyHead : null;
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (!figurines[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                slotTag.put("Item", figurines[i].save(registries));
                list.add(slotTag);
            }
        }
        tag.put("Figurines", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < MAX_SLOTS; i++) {
            figurines[i] = ItemStack.EMPTY;
        }
        figurineCount = 0;
        ListTag list = tag.getList("Figurines", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slotTag = list.getCompound(i);
            int slot = slotTag.getByte("Slot") & 0xFF;
            if (slot < MAX_SLOTS) {
                figurines[slot] = ItemStack.parse(registries, slotTag.get("Item")).orElse(ItemStack.EMPTY);
                if (!figurines[slot].isEmpty()) {
                    figurineCount++;
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
