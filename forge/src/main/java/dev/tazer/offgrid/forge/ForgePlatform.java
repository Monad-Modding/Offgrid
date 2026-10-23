package dev.tazer.offgrid.forge;

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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public final class ForgePlatform implements Platform {

    @Override
    public String getName() {
        return "forge";
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
        return FMLEnvironment.dist.isClient();
    }

    @Override
    @Nullable
    public OffgridChunk getChunkData(LevelChunk chunk) {
        return chunk.getCapability(ForgeChunkData.CAPABILITY).resolve().orElse(null);
    }

    @Override
    public OffgridChunk getOrCreateChunkData(LevelChunk chunk) {
        return chunk.getCapability(ForgeChunkData.CAPABILITY).orElseThrow(() -> new IllegalStateException("Chunk " + chunk.getPos() + " has no offgrid data"));
    }

    @Override
    public OffgridChunk getOrCreateGeneratingChunkData(ServerLevel level, ChunkAccess chunk) {
        if (chunk instanceof LevelChunk levelChunk) return this.getOrCreateChunkData(levelChunk);
        return ForgeChunkData.generating(level.dimension(), chunk.getPos().toLong(), true);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        return BlockEntityType.Builder.of(factory, block).build(null);
    }

    @Override
    public MixedSlabBlock createMixedSlab(BlockBehaviour.Properties properties) {
        return new ForgeMixedSlabBlock(properties);
    }

    @Override
    public <T> void sendToServer(PacketType<T> type, T packet) {
        ForgeNetwork.CHANNEL.sendToServer(packet);
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        ForgeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
