package dev.tazer.offgrid.world;

import dev.tazer.offgrid.network.CellData;
import dev.tazer.offgrid.network.CellSync;
import dev.tazer.offgrid.network.ChunkSync;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.OverlayData;
import dev.tazer.offgrid.network.PacketType;
import dev.tazer.offgrid.platform.Services;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public final class Cells {

    private static final int[] NONE = new int[0];

    private static BiConsumer<Level, BlockPos> clientListener = (level, pos) -> {
    };

    private Cells() {
    }

    public static void setClientListener(BiConsumer<Level, BlockPos> listener) {
        clientListener = listener;
    }

    @Nullable
    public static Cell get(@Nullable BlockGetter getter, BlockPos pos) {
        return OffgridIndex.cellAt(getter, pos);
    }

    public static List<Overlay> overlays(@Nullable BlockGetter getter, BlockPos pos) {
        Cell cell = get(getter, pos);
        return cell == null ? List.of() : cell.overlays();
    }

    @Nullable
    public static Overlay find(@Nullable BlockGetter getter, BlockPos pos, int id) {
        Cell cell = get(getter, pos);
        return cell == null ? null : cell.find(id);
    }

    public static boolean contains(@Nullable BlockGetter getter, BlockPos pos, Overlay overlay) {
        return find(getter, pos, overlay.getId()) == overlay;
    }

    public static boolean hasOverlays(@Nullable BlockGetter getter, BlockPos pos) {
        Cell cell = get(getter, pos);
        return cell != null && cell.hasOverlays();
    }

    @Nullable
    public static LevelChunk chunkAt(Level level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static BlockState realState(Level level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        return chunk == null ? Blocks.AIR.defaultBlockState() : chunk.getBlockState(pos);
    }

    @Nullable
    public static OffgridChunk data(Level level, BlockPos pos, boolean create) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            OffgridChunk tracked = index.getChunk(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
            if (tracked != null) return tracked;
        }
        LevelChunk chunk = chunkAt(level, pos);
        if (chunk == null) return null;
        return create ? Services.PLATFORM.getOrCreateChunkData(chunk) : Services.PLATFORM.getChunkData(chunk);
    }

    public static boolean update(Level level, BlockPos pos, UnaryOperator<Cell> change) {
        return update(level, pos, change, NONE);
    }

    public static boolean updateSilently(Level level, BlockPos pos, UnaryOperator<Cell> change) {
        return apply(level, pos, change, false, NONE);
    }

    public static boolean update(Level level, BlockPos pos, UnaryOperator<Cell> change, int[] broken) {
        return apply(level, pos, change, true, broken);
    }

    private static boolean apply(Level level, BlockPos pos, UnaryOperator<Cell> change, boolean notify, int[] broken) {
        LevelChunk chunk = chunkAt(level, pos);
        if (chunk == null) return false;
        OffgridChunk data = Services.PLATFORM.getOrCreateChunkData(chunk);
        long key = pos.asLong();
        Cell before = data.getOrEmpty(key);
        Cell after = change.apply(before);
        if (after == before) return false;
        data.put(key, after);
        chunk.setUnsaved(true);
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            index.track(chunk.getPos().toLong(), data);
        }
        if (before.light() != after.light()) {
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            clientListener.accept(level, pos);
        } else {
            if (notify) {
                sync(serverLevel, pos, after, broken);
            }
            if (!after.isEmpty()) {
                Covers.queue(serverLevel, pos);
            }
        }
        return true;
    }

    public static void markUnsaved(Level level, BlockPos pos) {
        LevelChunk chunk = chunkAt(level, pos);
        if (chunk != null) {
            chunk.setUnsaved(true);
        }
    }

    public static void sync(ServerLevel level, BlockPos pos) {
        Cell cell = get(level, pos);
        sync(level, pos, cell == null ? Cell.EMPTY : cell, NONE);
    }

    public static void sendTo(ServerPlayer player, ServerLevel level, BlockPos pos) {
        Cell cell = get(level, pos);
        OffgridNetwork.sendToPlayer(player, OffgridNetwork.CELL, new CellSync(snapshot(level, pos.asLong(), cell == null ? Cell.EMPTY : cell), NONE));
    }

    private static void sync(ServerLevel level, BlockPos pos, Cell cell, int[] broken) {
        sendTracking(level, pos, OffgridNetwork.CELL, new CellSync(snapshot(level, pos.asLong(), cell), broken));
    }

    public static CellData snapshot(ServerLevel level, long pos, Cell cell) {
        BlockPos host = BlockPos.of(pos);
        List<OverlayData> overlays = new ArrayList<>(cell.overlays().size());
        for (Overlay overlay : cell.overlays()) {
            BlockEntity blockEntity = overlay.materialize(level, host);
            CompoundTag tag = blockEntity == null ? null : BlockEntities.updateTag(blockEntity, level.registryAccess());
            overlays.add(new OverlayData(overlay.getId(), overlay.getState(), overlay.getOffset(), tag));
        }
        return new CellData(pos, cell.offset(), overlays);
    }

    @Nullable
    public static ChunkSync snapshot(ServerLevel level, LevelChunk chunk) {
        OffgridChunk data = Services.PLATFORM.getChunkData(chunk);
        if (data == null || data.getCells().isEmpty()) return null;
        List<CellData> cells = new ArrayList<>(data.getCells().size());
        for (Long2ObjectMap.Entry<Cell> entry : data.getCells().long2ObjectEntrySet()) {
            cells.add(snapshot(level, entry.getLongKey(), entry.getValue()));
        }
        return new ChunkSync(chunk.getPos().toLong(), cells);
    }

    public static void sendChunk(ServerPlayer player, ServerLevel level, LevelChunk chunk) {
        ChunkSync packet = snapshot(level, chunk);
        if (packet != null) {
            OffgridNetwork.sendToPlayer(player, OffgridNetwork.CHUNK, packet);
        }
    }

    public static <T> void sendTracking(ServerLevel level, BlockPos pos, PacketType<T> type, T packet) {
        for (ServerPlayer player : playersTracking(level, pos)) {
            OffgridNetwork.sendToPlayer(player, type, packet);
        }
    }

    public static List<ServerPlayer> playersTracking(ServerLevel level, BlockPos pos) {
        //? if >=26 {
        /*return level.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(pos), false);
        *///?} else {
        return level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false);
        //?}
    }

    public static void track(Level level, LevelChunk chunk) {
        OffgridChunk data = Services.PLATFORM.getChunkData(chunk);
        OffgridIndex index = OffgridIndex.of(level);
        if (data == null || index == null || data.isEmpty()) return;
        index.track(chunk.getPos().toLong(), data);
    }

    public static void untrack(Level level, LevelChunk chunk) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null) return;
        OffgridChunk data = index.getChunk(chunk.getPos().toLong());
        if (data == null) return;
        index.untrack(chunk.getPos().toLong());
        for (Cell cell : data.getCells().values()) {
            for (Overlay overlay : cell.overlays()) {
                if (overlay.getBlockEntity() != null) {
                    overlay.saveBlockEntity();
                    overlay.discardBlockEntity();
                }
            }
        }
    }
}
