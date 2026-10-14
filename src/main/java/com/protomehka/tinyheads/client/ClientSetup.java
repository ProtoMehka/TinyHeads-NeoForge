package com.protomehka.tinyheads.client;

import com.protomehka.tinyheads.TinyHeadType;
import com.protomehka.tinyheads.TinyHeads;
import com.protomehka.tinyheads.init.ModBlockEntities;
import com.protomehka.tinyheads.init.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** References client-only types (RenderType, EntityRenderersEvent) — must never load on a
 * dedicated server, hence the Dist.CLIENT-gated @EventBusSubscriber like the rest of this
 * codebase's client-only classes. @Mod.EventBusSubscriber became a top-level annotation in
 * NeoForge (net.neoforged.fml.common.EventBusSubscriber) - same members, just not nested under
 * @Mod anymore. */
@EventBusSubscriber(modid = TinyHeads.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHELF.get(), RenderType.cutout());
            for (TinyHeadType type : TinyHeadType.values()) {
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.BOXED.get(type).get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.UNBOXED.get(type).get(), RenderType.cutout());
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SHELF.get(), ShelfBlockEntityRenderer::new);
    }
}
