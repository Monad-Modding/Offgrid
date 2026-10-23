package dev.tazer.offgrid.platform;

import dev.tazer.offgrid.network.PacketType;
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
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface Platform {

    String getName();

    boolean isModLoaded(String id);

    boolean isClient();

    Path getConfigDirectory();

    @Nullable
    OffgridChunk getChunkData(LevelChunk chunk);

    OffgridChunk getOrCreateChunkData(LevelChunk chunk);

    @Nullable
    OffgridChunk getOrCreateGeneratingChunkData(ServerLevel level, ChunkAccess chunk);

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntityType.BlockEntitySupplier<T> factory, Block block);

    default MixedSlabBlock createMixedSlab(BlockBehaviour.Properties properties) {
        return new MixedSlabBlock(properties);
    }

    <T> void sendToServer(PacketType<T> type, T packet);

    <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet);
}
