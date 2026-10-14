package com.protomehka.tinyheads.tab;

import com.protomehka.tinyheads.TinyHeadType;
import com.protomehka.tinyheads.TinyHeads;
import com.protomehka.tinyheads.init.ModBlocks;
import com.protomehka.tinyheads.init.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinyHeads.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TINYHEADS_TAB = TABS.register("tinyheads",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tinyheads"))
                    .icon(() -> new net.minecraft.world.item.ItemStack(ModItems.BOXED.get(TinyHeadType.ASHE).get()))
                    .displayItems((parameters, output) -> {
                        for (TinyHeadType type : TinyHeadType.values()) {
                            output.accept(ModBlocks.BOXED.get(type).get());
                            output.accept(ModBlocks.UNBOXED.get(type).get());
                        }
                        output.accept(ModBlocks.SHELF.get());
                    })
                    .build());

    private ModCreativeTab() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
