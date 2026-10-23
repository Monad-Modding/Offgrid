package dev.tazer.offgrid.fabric;

import dev.tazer.offgrid.Offgrid;
import dev.tazer.offgrid.network.PacketType;
import dev.tazer.offgrid.platform.Platform;
import dev.tazer.offgrid.world.OffgridChunk;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
//? if >=26 {
/*import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
*///?}
//? if <1.20.5 {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}

public final class FabricPlatform implements Platform {

    public static final AttachmentType<OffgridChunk> CHUNK = AttachmentRegistry.<OffgridChunk>builder().persistent(OffgridChunk.CODEC).initializer(OffgridChunk::new).buildAndRegister(Offgrid.id("chunk"));

    @Override
    public String getName() {
        return "fabric";
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    @Nullable
    public OffgridChunk getChunkData(LevelChunk chunk) {
        return chunk.getAttached(CHUNK);
    }

    @Override
    public OffgridChunk getOrCreateChunkData(LevelChunk chunk) {
        return chunk.getAttachedOrCreate(CHUNK);
    }

    @Override
    public OffgridChunk getOrCreateGeneratingChunkData(ServerLevel level, ChunkAccess chunk) {
        return chunk.getAttachedOrCreate(CHUNK);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        //? if >=26 {
        /*return FabricBlockEntityTypeBuilder.create(factory::create, block).build();
        *///?} else {
        return BlockEntityType.Builder.of(factory, block).build(null);
        //?}
    }

    @Override
    public <T> void sendToServer(PacketType<T> type, T packet) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(type.wrap(packet));
        //?} else {
        /*FriendlyByteBuf buf = PacketByteBufs.create();
        type.write(buf, packet);
        ClientPlayNetworking.send(type.getId(), buf);
        *///?}
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        //? if >=1.20.5 {
        ServerPlayNetworking.send(player, type.wrap(packet));
        //?} else {
        /*FriendlyByteBuf buf = PacketByteBufs.create();
        type.write(buf, packet);
        ServerPlayNetworking.send(player, type.getId(), buf);
        *///?}
    }
}
