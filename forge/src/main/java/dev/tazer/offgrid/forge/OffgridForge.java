package dev.tazer.offgrid.forge;

import dev.tazer.offgrid.Offgrid;
import dev.tazer.offgrid.OffgridConfig;
import dev.tazer.offgrid.command.OffgridCommand;
import dev.tazer.offgrid.compat.Integrations;
import dev.tazer.offgrid.forge.client.OffgridForgeClient;
import dev.tazer.offgrid.forge.compat.additionalplacements.AdditionalPlacementsSlabs;
import dev.tazer.offgrid.forge.compat.quark.QuarkVerticalSlabs;
import dev.tazer.offgrid.forge.compat.snowrealmagic.SnowRealMagicCover;
import dev.tazer.offgrid.registry.OffgridBlocks;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.Covers;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Offgrid.MOD_ID)
public final class OffgridForge {

    private static final String GENERATING_KEY = "offgrid:generating";

    public OffgridForge(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ForgeNetwork.register();
        OffgridConfig.get();
        Integrations.init();
        if (Integrations.isLoaded(Integrations.QUARK)) {
            MixedSlabs.addFamily(new QuarkVerticalSlabs());
        }
        if (Integrations.isLoaded(Integrations.ADDITIONAL_PLACEMENTS)) {
            MixedSlabs.addParent(AdditionalPlacementsSlabs::parentOf);
            MixedSlabs.addFamily(new AdditionalPlacementsSlabs());
        }
        if (Integrations.isLoaded(Integrations.SNOW_REAL_MAGIC)) {
            Covers.addCover(new SnowRealMagicCover());
        }
        modBus.addListener(OffgridForge::onRegister);
        modBus.addListener(OffgridForge::onCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, OffgridForge::onAttachChunk);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onChunkSave);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onChunkRead);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onChunkUnload);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onLogout);
        MinecraftForge.EVENT_BUS.addListener(OffgridForge::onCommands);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            OffgridForgeClient.init(modBus);
        }
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            OffgridBlocks.registerBlocks((id, block) -> event.register(Registries.BLOCK, id, () -> block));
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            OffgridBlocks.registerBlockEntities((id, type) -> event.register(Registries.BLOCK_ENTITY_TYPE, id, () -> type));
        }
    }

    private static void onCapabilities(RegisterCapabilitiesEvent event) {
        event.register(OffgridChunk.class);
    }

    private static void onAttachChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(Offgrid.id("chunk"), new ForgeChunkData());
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.level instanceof ServerLevel level) {
            OffgridEvents.onServerLevelTick(level);
        } else {
            OffgridEvents.onClientLevelTick(event.level);
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof Level level && event.getChunk() instanceof LevelChunk chunk) {
            if (level instanceof ServerLevel) {
                ForgeChunkData.promoteGenerating(level.dimension(), chunk);
            }
            OffgridEvents.onChunkLoad(level, chunk);
        }
    }

    private static void onChunkSave(ChunkDataEvent.Save event) {
        if (event.getChunk() instanceof LevelChunk || !(event.getLevel() instanceof ServerLevel level)) return;
        OffgridChunk pending = ForgeChunkData.generating(level.dimension(), event.getChunk().getPos().toLong(), false);
        if (pending != null && !pending.isEmpty()) {
            OffgridChunk.CODEC.encodeStart(NbtOps.INSTANCE, pending).result().ifPresent(encoded -> event.getData().put(GENERATING_KEY, encoded));
        }
    }

    private static void onChunkRead(ChunkDataEvent.Load event) {
        if (!(event.getChunk() instanceof ProtoChunk) || event.getChunk() instanceof ImposterProtoChunk || !(event.getLevel() instanceof ServerLevel level)) return;
        Tag stored = event.getData().get(GENERATING_KEY);
        if (stored != null) {
            OffgridChunk.CODEC.parse(NbtOps.INSTANCE, stored).result().ifPresent(pending -> ForgeChunkData.restoreGenerating(level.dimension(), event.getChunk().getPos().toLong(), pending));
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
            if (level instanceof ServerLevel) {
                ForgeChunkData.forgetGenerating(level.dimension());
            }
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
