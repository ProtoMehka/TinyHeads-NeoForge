package com.protomehka.tinyheads.item;

import com.protomehka.tinyheads.TinyHeadCollection;
import com.protomehka.tinyheads.TinyHeadType;
import com.protomehka.tinyheads.block.BlockTinyHead;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Consolidates both the 1.12.2 ItemBlockTinyHead (collection/rarity/boxed tooltip) and
 * ClientProxy#onItemTooltip (the same info, split across two classes there for historical
 * reasons) into a single appendHoverText override.
 *
 * appendHoverText's second parameter changed from Level to Item.TooltipContext in 1.21.1 (verified
 * against the real decompiled Item.java) - this method never used the level for anything besides
 * passing it to super, so the port is a straight signature swap with no logic change.
 */
public class BlockItemTinyHead extends BlockItem {

    public BlockItemTinyHead(BlockTinyHead block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        BlockTinyHead tinyHead = (BlockTinyHead) this.getBlock();
        TinyHeadType type = tinyHead.getHeadType();

        tooltip.add(Component.translatable(type.getCollection().getTranslationKey()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tinyheads.rarity." + type.getRarity().getId())
                .withStyle(type.getRarity().getColor()));

        if (tinyHead.isBoxed()) {
            tooltip.add(Component.translatable("tinyheads.tooltip.boxed").withStyle(ChatFormatting.GOLD));

            if (Screen.hasShiftDown()) {
                TinyHeadCollection collection = type.getCollection();
                Component effectName = null;

                if (collection.hasPotionEffect()) {
                    effectName = Component.translatable(collection.getPotionEffect().value().getDescriptionId());
                } else if (collection.hasCustomEffect()) {
                    effectName = Component.translatable("tinyheads.effect." + collection.getCustomEffect().name().toLowerCase());
                }

                if (effectName != null) {
                    tooltip.add(Component.translatable("tinyheads.tooltip.collection_effect").withStyle(ChatFormatting.GOLD));
                    tooltip.add(Component.literal(" ").append(effectName).withStyle(ChatFormatting.AQUA));
                }
            } else {
                tooltip.add(Component.translatable("tinyheads.tooltip.hold_shift").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
