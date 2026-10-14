package com.protomehka.tinyheads.block;

import com.mojang.serialization.MapCodec;
import com.protomehka.tinyheads.blockentity.ShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BlockShelf extends HorizontalDirectionalBlock implements EntityBlock {

    public static final EnumProperty<ShelfPart> PART = EnumProperty.create("part", ShelfPart.class);

    // Plain Properties-only constructor -> simpleCodec is enough (same pattern vanilla's own
    // LoomBlock uses, also a HorizontalDirectionalBlock with no extra constructor state).
    public static final MapCodec<BlockShelf> CODEC = simpleCodec(BlockShelf::new);

    public BlockShelf(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, ShelfPart.BOTTOM_LEFT));
    }

    @Override
    protected MapCodec<BlockShelf> codec() {
        return CODEC;
    }

    /** Offset from master to the given part, based on facing direction. Extension goes to the
     * right from the player's perspective (FACING rotated counter-clockwise). */
    public static BlockPos getPartOffset(Direction facing, ShelfPart part) {
        Direction right = facing.getCounterClockWise();
        return switch (part) {
            case BOTTOM_RIGHT -> new BlockPos(right.getStepX(), 0, right.getStepZ());
            default -> BlockPos.ZERO;
        };
    }

    public static BlockPos getMasterPos(BlockPos pos, Direction facing, ShelfPart part) {
        return pos.subtract(getPartOffset(facing, part));
    }

    @Nullable
    public static ShelfBlockEntity getMasterBlockEntity(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        ShelfPart part = state.getValue(PART);
        BlockPos masterPos = getMasterPos(pos, facing, part);
        BlockEntity be = level.getBlockEntity(masterPos);
        return be instanceof ShelfBlockEntity shelf ? shelf : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == ShelfPart.BOTTOM_LEFT ? new ShelfBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (state.getValue(PART) != ShelfPart.BOTTOM_LEFT || type != com.protomehka.tinyheads.init.ModBlockEntities.SHELF.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> com.protomehka.tinyheads.effect.ShelfEffectHandler.tick(lvl, pos, (ShelfBlockEntity) be);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Block#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult) is gone in
     * 1.21.1 - right-click interaction is now split into useItemOn (called first, with whatever
     * item the player is holding, possibly empty) and useWithoutItem (the fallback when useItemOn
     * returns PASS_TO_DEFAULT_BLOCK_INTERACTION and the player used their main hand). The engine
     * calls useItemOn even with an empty hand, so that override must explicitly bail out to let
     * useWithoutItem handle the "take a figurine out" case - otherwise an empty-hand click would
     * never reach it.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        ShelfBlockEntity shelf = getMasterBlockEntity(level, pos, state);
        if (shelf == null) {
            return ItemInteractionResult.FAIL;
        }

        ItemStack result = shelf.addFigurine(stack.copy());
        if (result != stack) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ShelfBlockEntity shelf = getMasterBlockEntity(level, pos, state);
        if (shelf == null) {
            return InteractionResult.FAIL;
        }

        ItemStack removed = shelf.removeLastFigurine();
        if (!removed.isEmpty()) {
            if (!player.getInventory().add(removed)) {
                level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), removed));
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LivingEntity placer = context.getPlayer();
        Direction facing = placer != null ? placer.getDirection().getOpposite() : Direction.NORTH;
        return defaultBlockState().setValue(FACING, facing).setValue(PART, ShelfPart.BOTTOM_LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;

        Direction facing = state.getValue(FACING);
        BlockPos partPos = pos.offset(getPartOffset(facing, ShelfPart.BOTTOM_RIGHT));

        if (!level.getBlockState(partPos).canBeReplaced()) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(this));
            return;
        }

        level.setBlock(partPos, defaultBlockState().setValue(FACING, facing).setValue(PART, ShelfPart.BOTTOM_RIGHT), 3);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!level.isClientSide()) {
                Direction facing = state.getValue(FACING);
                ShelfPart part = state.getValue(PART);
                BlockPos masterPos = getMasterPos(pos, facing, part);

                BlockEntity be = level.getBlockEntity(masterPos);
                if (be instanceof ShelfBlockEntity shelf) {
                    shelf.dropAllFigurines(level, pos);
                }

                for (ShelfPart p : ShelfPart.values()) {
                    BlockPos partPos = masterPos.offset(getPartOffset(facing, p));
                    if (!partPos.equals(pos)) {
                        BlockState partState = level.getBlockState(partPos);
                        if (partState.getBlock() == this) {
                            level.removeBlock(partPos, isMoving);
                        }
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }
}
