package dev.tazer.offgrid.fabric;

import dev.tazer.offgrid.OffgridConfig;
import dev.tazer.offgrid.command.OffgridCommand;
import dev.tazer.offgrid.compat.Integrations;
import dev.tazer.offgrid.registry.OffgridBlocks;
import dev.tazer.offgrid.world.Covers;
import dev.tazer.offgrid.world.OffgridEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//? if <26.2 {
import dev.tazer.offgrid.fabric.compat.snowrealmagic.SnowRealMagicCover;
//?}

public final class OffgridFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        OffgridBlocks.registerBlocks((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
        OffgridBlocks.registerBlockEntities((id, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type));
        FabricNetwork.register();
        OffgridConfig.get();
        Integrations.init();
        //? if <26.2 {
        if (Integrations.isLoaded(Integrations.SNOW_REAL_MAGIC)) {
            Covers.addCover(new SnowRealMagicCover());
        }
        //?}
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, selection) -> dispatcher.register(OffgridCommand.build()));
        ServerTickEvents.END_WORLD_TICK.register(OffgridEvents::onServerLevelTick);
        //? if >=26 {
        /*ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> OffgridEvents.onChunkLoad(level, chunk));
        *///?} else {
        ServerChunkEvents.CHUNK_LOAD.register(OffgridEvents::onChunkLoad);
        //?}
        ServerChunkEvents.CHUNK_UNLOAD.register(OffgridEvents::onChunkUnload);
        ServerWorldEvents.UNLOAD.register((server, level) -> OffgridEvents.onLevelUnload(level));
        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> OffgridEvents.onPlayerLogout(listener.player));
    }
}
