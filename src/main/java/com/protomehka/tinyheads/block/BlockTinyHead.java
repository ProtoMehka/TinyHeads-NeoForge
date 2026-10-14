package com.protomehka.tinyheads.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.protomehka.tinyheads.TinyHeadType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.context.BlockPlaceContext;

public class BlockTinyHead extends HorizontalDirectionalBlock {

    private static final VoxelShape BOXED_SHAPE = Shapes.box(0.1875, 0.0, 0.1875, 0.8125, 0.875, 0.8125);
    private static final VoxelShape UNBOXED_SHAPE = Shapes.box(0.25, 0.0, 0.25, 0.75, 0.75, 0.75);

    /**
     * BlockBehaviour.codec() is new in 1.21.1 (didn't exist in 1.20.1) - every concrete Block
     * subclass must now supply one, since blocks became data-driven/datapack-registerable. This
     * constructor takes two extra args beyond Properties, so the simple simpleCodec(Block::new)
     * helper (fine for BlockShelf, see its own codec) doesn't apply - this follows the same
     * RecordCodecBuilder.mapCodec pattern vanilla uses for SkullBlock (which also carries extra
     * enum-like state beyond Properties). TinyHeadType.CODEC is this mod's own addition, built via
     * StringRepresentable.fromEnum - see TinyHeadType.java.
     */
    public static final MapCodec<BlockTinyHead> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.BOOL.fieldOf("boxed").forGetter(BlockTinyHead::isBoxed),
                    TinyHeadType.CODEC.fieldOf("head_type").forGetter(BlockTinyHead::getHeadType),
                    propertiesCodec())
            .apply(instance, (boxed, headType, properties) -> new BlockTinyHead(properties, boxed, headType)));

    private final boolean boxed;
    private final TinyHeadType headType;

    public BlockTinyHead(Properties properties, boolean boxed, TinyHeadType headType) {
        super(properties);
        this.boxed = boxed;
        this.headType = headType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlockTinyHead> codec() {
        return CODEC;
    }

    public boolean isBoxed() {
        return boxed;
    }

    public TinyHeadType getHeadType() {
        return headType;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return boxed ? BOXED_SHAPE : UNBOXED_SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LivingEntity placer = context.getPlayer();
        Direction facing = placer != null ? placer.getDirection().getOpposite() : Direction.NORTH;
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
}
