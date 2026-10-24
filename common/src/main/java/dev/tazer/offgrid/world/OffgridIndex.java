package dev.tazer.offgrid.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class OffgridIndex {

    private static final long MOVING_CELL_LIFETIME = 40L;

    private final LevelTasks tasks = new LevelTasks();
    private final Long2ObjectOpenHashMap<MovingCellEntry> movingCells = new Long2ObjectOpenHashMap<>();

    private volatile Long2ObjectMap<OffgridChunk> chunks = Long2ObjectMaps.emptyMap();

    @Nullable
    public static OffgridIndex of(@Nullable BlockGetter getter) {
        return getter instanceof OffgridIndexHolder holder ? holder.offgrid$getIndex() : null;
    }

    public static boolean hasCell(@Nullable BlockGetter getter, int x, int y, int z) {
        OffgridIndex index = of(getter);
        return index != null && !index.isEmpty() && index.getCell(x, y, z) != null;
    }

    public static boolean hasCellsInSection(ChunkAccess chunk, int sectionIndex) {
        if (!(chunk instanceof LevelChunk levelChunk)) return false;
        OffgridIndex index = of(levelChunk.getLevel());
        if (index == null || index.isEmpty()) return false;
        OffgridChunk data = index.getChunk(chunk.getPos().toLong());
        return data != null && data.hasCellsInSection(chunk.getSectionYFromSectionIndex(sectionIndex));
    }

    @Nullable
    public static Cell cellAt(@Nullable BlockGetter getter, BlockPos pos) {
        OffgridIndex index = of(getter);
        return index == null ? null : index.getCell(pos.getX(), pos.getY(), pos.getZ());
    }

    public LevelTasks getTasks() {
        return this.tasks;
    }

    public void putMovingCell(long pos, AssembledCell cell, long time) {
        this.movingCells.values().removeIf(entry -> time - entry.time() > MOVING_CELL_LIFETIME);
        this.movingCells.put(pos, new MovingCellEntry(cell, time));
    }

    @Nullable
    public AssembledCell takeMovingCell(long pos, long time) {
        MovingCellEntry entry = this.movingCells.remove(pos);
        return entry == null || time - entry.time() > MOVING_CELL_LIFETIME ? null : entry.cell();
    }

    public boolean isEmpty() {
        return this.chunks.isEmpty();
    }

    public Long2ObjectMap<OffgridChunk> getChunks() {
        return this.chunks;
    }

    @Nullable
    public OffgridChunk getChunk(long chunkKey) {
        return this.chunks.get(chunkKey);
    }

    @Nullable
    public Cell getCell(BlockPos pos) {
        return this.getCell(pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    public Cell getCell(long pos) {
        return this.getCell(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
    }

    @Nullable
    public Cell getCell(int x, int y, int z) {
        Long2ObjectMap<OffgridChunk> current = this.chunks;
        if (current.isEmpty()) return null;
        OffgridChunk chunk = current.get(ChunkPos.asLong(x >> 4, z >> 4));
        return chunk == null ? null : chunk.get(BlockPos.asLong(x, y, z));
    }

    public synchronized OffgridChunk detachedChunk(long chunkKey) {
        OffgridChunk chunk = this.chunks.get(chunkKey);
        if (chunk != null) return chunk;
        OffgridChunk created = new OffgridChunk();
        this.track(chunkKey, created);
        return created;
    }

    public synchronized void track(long chunkKey, OffgridChunk chunk) {
        if (this.chunks.get(chunkKey) == chunk) return;
        Long2ObjectOpenHashMap<OffgridChunk> next = new Long2ObjectOpenHashMap<>(this.chunks);
        next.put(chunkKey, chunk);
        this.chunks = Long2ObjectMaps.unmodifiable(next);
    }

    public synchronized void untrack(long chunkKey) {
        if (!this.chunks.containsKey(chunkKey)) return;
        Long2ObjectOpenHashMap<OffgridChunk> next = new Long2ObjectOpenHashMap<>(this.chunks);
        next.remove(chunkKey);
        this.chunks = next.isEmpty() ? Long2ObjectMaps.emptyMap() : Long2ObjectMaps.unmodifiable(next);
    }

    public synchronized void clear() {
        this.chunks = Long2ObjectMaps.emptyMap();
        this.tasks.clear();
    }

    private record MovingCellEntry(AssembledCell cell, long time) {
    }
}
