package dev.tazer.offgrid.fabric.client;

import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.client.OffgridClient;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.PacketType;
import dev.tazer.offgrid.registry.OffgridBlocks;
import dev.tazer.offgrid.world.OffgridEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? if >=26 {
/*import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
*///?} else {
import dev.tazer.offgrid.client.OverlayBlockEntities;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
//?}

public final class OffgridFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OffgridClient.init();
        for (PacketType<?> type : OffgridNetwork.all()) {
            if (!type.isServerbound()) {
                registerReceiver(type);
            }
        }
        ClientTickEvents.END_WORLD_TICK.register(OffgridEvents::onClientLevelTick);
        ClientChunkEvents.CHUNK_LOAD.register(OffgridEvents::onChunkLoad);
        ClientChunkEvents.CHUNK_UNLOAD.register(OffgridEvents::onChunkUnload);
        //? if >=26 {
        /*BlockColorRegistry.register(MixedSlabColors.tintSources(), OffgridBlocks.getMixedSlab());
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register((model, modifier) -> modifier.state().is(OffgridBlocks.getMixedSlab()) ? new FabricMixedSlabModel(model) : model));
        *///?} else if >=1.21 {
        ColorProviderRegistry.BLOCK.register(MixedSlabColors::getColor, OffgridBlocks.getMixedSlab());
        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register((model, modifier) -> modifier.topLevelId() != null && modifier.topLevelId().id().equals(OffgridBlocks.MIXED_SLAB_ID) ? new FabricMixedSlabModel(model) : model));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.matrixStack() == null || context.consumers() == null) return;
            OverlayBlockEntities.render(context.matrixStack(), context.camera().getPosition(), context.consumers(), context.frustum(), context.tickCounter().getGameTimeDeltaPartialTick(false));
        });
        //?} else {
        /*ColorProviderRegistry.BLOCK.register(MixedSlabColors::getColor, OffgridBlocks.getMixedSlab());
        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register((model, modifier) -> modifier.id().getNamespace().equals(OffgridBlocks.MIXED_SLAB_ID.getNamespace()) && modifier.id().getPath().equals(OffgridBlocks.MIXED_SLAB_ID.getPath()) ? new FabricMixedSlabModel(model) : model));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.matrixStack() == null || context.consumers() == null) return;
            OverlayBlockEntities.render(context.matrixStack(), context.camera().getPosition(), context.consumers(), context.frustum(), context.tickDelta());
        });
        *///?}
    }

    private static <T> void registerReceiver(PacketType<T> type) {
        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(type.getPayloadType(), (payload, context) -> payload.handleOnClient());
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(type.getId(), (client, listener, buf, sender) -> {
            T value = type.read(buf);
            client.execute(() -> type.handleOnClient(value));
        });
        *///?}
    }
}
