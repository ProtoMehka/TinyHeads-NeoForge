package com.protomehka.tinyheads.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protomehka.tinyheads.block.BlockShelf;
import com.protomehka.tinyheads.block.ShelfPart;
import com.protomehka.tinyheads.blockentity.ShelfBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from 1.12.2's TileEntityShelfRenderer. Instead of re-deriving row/depth world vectors
 * and their pivot corrections per facing (error-prone, as several rounds of bugs demonstrated),
 * the whole render frame is rotated ONCE by `angle` around the master block's own center — the
 * same rotation already confirmed correct for the figurine's own orientation. Every subsequent
 * position is then expressed with FIXED, orientation-independent numbers, tuned once against the
 * NORTH-facing case; the outer rotation guarantees every other facing matches it automatically.
 *
 * BlockEntityRenderer's render(...) signature and BlockRenderDispatcher#renderSingleBlock's 5-arg
 * overload are both unchanged in 1.21.1 (verified against the real decompiled sources) - this
 * class ports with only mechanical import changes, no logic changes.
 */
public class ShelfBlockEntityRenderer implements BlockEntityRenderer<ShelfBlockEntity> {

    public ShelfBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ShelfBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getFigurineCount() == 0) return;

        BlockState state = be.getLevel().getBlockState(be.getBlockPos());
        if (!(state.getBlock() instanceof BlockShelf)) return;
        if (state.getValue(BlockShelf.PART) != ShelfPart.BOTTOM_LEFT) return;

        Direction facing = state.getValue(BlockShelf.FACING);
        float angle = switch (facing) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> 270f;
            default -> 0f;
        };

        // Slot 0: slave-side center, slot 1: the seam between the two blocks, slot 2: master-side
        // center (matches the original 1.12.2 "factor" semantics), expressed in the NORTH-facing
        // local frame: rowDir=WEST=(-1,0), depthDir=NORTH=(0,-1). The working frame's origin is
        // already master's own center (see the translate below), so factor=0 needs NO extra base
        // offset to land there — rowBase/depthBase are purely optional EXTRA nudges on top, both
        // 0 by default.
        float[] factors = {1.0f, 0.5f, 0.0f};
        final double rowBase = 0.0;
        final double depthBase = 0.0;

        poseStack.pushPose();
        // Move to the master block's own center, then rotate the working frame there to match this
        // facing. Everything placed after this point uses fixed local coordinates (as if facing
        // were always NORTH, relative to master's center) and comes out correctly rotated in world
        // space by construction.
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        for (int i = 0; i < ShelfBlockEntity.AVAILABLE_SLOTS; i++) {
            ItemStack stack = be.getFigurine(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) continue;

            BlockState figurineState = blockItem.getBlock().defaultBlockState();
            float factor = factors[i];

            // rowDir(NORTH)=WEST=(-1,0), depthDir(NORTH)=NORTH=(0,-1).
            double localX = -(rowBase + factor);
            double localZ = -depthBase;
            double localY = 1.0; // display stand: figurines sit on top of the bottom blocks

            renderFigurine(figurineState, poseStack, buffer, packedLight, packedOverlay, localX, localY, localZ);
        }

        poseStack.popPose();
    }

    private void renderFigurine(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            double x, double y, double z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        // The box is rendered from its own local origin (corner), but its actual geometry
        // (tinyhead_base_boxed.json elements: X:[5.5,10.5], Y:[0,7], Z:[7,10] of 16) isn't centered
        // there — its true centroid is (0.5, 0.21875, 0.53125). To make the box's real visual
        // center land exactly on (x,y,z), translate by -centroid. Y is the exception: we want the
        // box's BOTTOM sitting at this height (resting on the shelf), not its vertical middle, so Y
        // keeps a small "just above the surface" nudge instead of the full centroid subtraction.
        poseStack.translate(-0.5, 0.01, -0.53125);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
