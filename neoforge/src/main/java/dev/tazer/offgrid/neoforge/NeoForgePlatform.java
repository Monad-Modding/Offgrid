package dev.tazer.offgrid.neoforge;

import dev.tazer.offgrid.network.PacketType;
import dev.tazer.offgrid.platform.Platform;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.world.OffgridChunk;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
//? if >=26 {
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Set;
*///?}

public final class NeoForgePlatform implements Platform {

    @Override
    public String getName() {
        return "neoforge";
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    @Override
    public boolean isClient() {
        //? if >=26 {
        /*return FMLEnvironment.getDist().isClient();
        *///?} else {
        return FMLEnvironment.dist.isClient();
        //?}
    }

    @Override
    @Nullable
    public OffgridChunk getChunkData(LevelChunk chunk) {
        return chunk.getExistingDataOrNull(OffgridNeoForge.CHUNK.get());
    }

    @Override
    public OffgridChunk getOrCreateChunkData(LevelChunk chunk) {
        return chunk.getData(OffgridNeoForge.CHUNK.get());
    }

    @Override
    public OffgridChunk getOrCreateGeneratingChunkData(ServerLevel level, ChunkAccess chunk) {
        return chunk.getData(OffgridNeoForge.CHUNK.get());
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        //? if >=26 {
        /*return new BlockEntityType<>(factory, Set.of(block));
        *///?} else {
        return BlockEntityType.Builder.of(factory, block).build(null);
        //?}
    }

    @Override
    public MixedSlabBlock createMixedSlab(BlockBehaviour.Properties properties) {
        return new NeoForgeMixedSlabBlock(properties);
    }

    @Override
    public <T> void sendToServer(PacketType<T> type, T packet) {
        //? if >=26 {
        /*ClientPacketDistributor.sendToServer(type.wrap(packet));
        *///?} else {
        PacketDistributor.sendToServer(type.wrap(packet));
        //?}
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        PacketDistributor.sendToPlayer(player, type.wrap(packet));
    }
}
