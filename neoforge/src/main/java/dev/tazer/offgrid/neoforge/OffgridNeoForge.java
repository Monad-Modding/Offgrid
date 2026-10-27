package dev.tazer.offgrid.neoforge;

import dev.tazer.offgrid.Offgrid;
import dev.tazer.offgrid.OffgridConfig;
import dev.tazer.offgrid.command.OffgridCommand;
import dev.tazer.offgrid.compat.Integrations;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.PacketType;
import dev.tazer.offgrid.registry.OffgridBlocks;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
//? if <26 {
import dev.tazer.offgrid.neoforge.compat.quark.QuarkVerticalSlabs;
//?}
//? if <26.2 {
import dev.tazer.offgrid.neoforge.compat.additionalplacements.AdditionalPlacementsSlabs;
//?}

@Mod(Offgrid.MOD_ID)
public final class OffgridNeoForge {

    private static final String PROTOCOL = "1";
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Offgrid.MOD_ID);

    //? if >=26 {
    /*public static final DeferredHolder<AttachmentType<?>, AttachmentType<OffgridChunk>> CHUNK = ATTACHMENTS.register("chunk", () -> AttachmentType.builder(OffgridChunk::new).serialize(OffgridChunk.MAP_CODEC, data -> !data.isEmpty()).build());
    *///?} else {
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OffgridChunk>> CHUNK = ATTACHMENTS.register("chunk", () -> AttachmentType.builder(OffgridChunk::new).serialize(OffgridChunk.CODEC, data -> !data.isEmpty()).build());
    //?}

    public OffgridNeoForge(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        OffgridConfig.get();
        Integrations.init();
        //? if <26 {
        if (Integrations.isLoaded(Integrations.QUARK)) {
            MixedSlabs.addFamily(new QuarkVerticalSlabs());
        }
        //?}
        //? if <26.2 {
        if (Integrations.isLoaded(Integrations.ADDITIONAL_PLACEMENTS)) {
            MixedSlabs.addParent(AdditionalPlacementsSlabs::parentOf);
            MixedSlabs.addFamily(new AdditionalPlacementsSlabs());
        }
        //?}
        modBus.addListener(OffgridNeoForge::onPayloads);
        modBus.addListener(OffgridNeoForge::onRegister);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForge::onLevelTick);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForge::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForge::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForge::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForge::onLogout);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForge::onCommands);
    }

    private static void onPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        for (PacketType<?> type : OffgridNetwork.all()) {
            register(registrar, type);
        }
    }

    private static <T> void register(PayloadRegistrar registrar, PacketType<T> type) {
        if (type.isServerbound()) {
            registrar.playToServer(type.getPayloadType(), type.getStreamCodec(), (payload, context) -> {
                if (context.player() instanceof ServerPlayer player) {
                    type.handleOnServer(payload.value(), player);
                }
            });
        } else {
            registrar.playToClient(type.getPayloadType(), type.getStreamCodec(), (payload, context) -> payload.handleOnClient());
        }
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            OffgridBlocks.registerBlocks((id, block) -> event.register(Registries.BLOCK, id, () -> block));
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            OffgridBlocks.registerBlockEntities((id, type) -> event.register(Registries.BLOCK_ENTITY_TYPE, id, () -> type));
        }
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            OffgridEvents.onServerLevelTick(level);
        } else {
            OffgridEvents.onClientLevelTick(event.getLevel());
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof Level level && event.getChunk() instanceof LevelChunk chunk) {
            OffgridEvents.onChunkLoad(level, chunk);
        }
    }

    private static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && event.getChunk() instanceof LevelChunk chunk) {
            OffgridEvents.onChunkUnload(level, chunk);
        }
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            OffgridEvents.onLevelUnload(level);
        }
    }

    private static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(OffgridCommand.build());
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OffgridEvents.onPlayerLogout(player);
        }
    }
}
