package dev.tazer.offgrid.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tazer.offgrid.Offgrid;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public final class OffgridChunk {

    public static final MapCodec<OffgridChunk> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            lenient(StoredCell.CODEC, "cell").optionalFieldOf("cells", List.of()).forGetter(OffgridChunk::storedCells),
            lenient(ScheduledTick.CODEC, "scheduled tick").optionalFieldOf("ticks", List.of()).forGetter(chunk -> List.copyOf(chunk.ticks)),
            Codec.INT.optionalFieldOf("next_id", 0).forGetter(chunk -> chunk.nextId)
    ).apply(instance, OffgridChunk::new));
    public static final Codec<OffgridChunk> CODEC = MAP_CODEC.codec();

    private final List<ScheduledTick> ticks = new ArrayList<>();
    private volatile Long2ObjectMap<Cell> cells = Long2ObjectMaps.emptyMap();
    private int nextId;

    public OffgridChunk() {
    }

    private OffgridChunk(List<StoredCell> stored, List<ScheduledTick> ticks, int nextId) {
        Long2ObjectOpenHashMap<Cell> loaded = new Long2ObjectOpenHashMap<>(stored.size());
        int highest = nextId;
        for (StoredCell cell : stored) {
            Cell restored = Cell.of(cell.offset().orElse(null), cell.overlays());
            if (restored.isEmpty()) {
                continue;
            }
            loaded.put(cell.pos(), restored);
            for (Overlay overlay : restored.overlays()) {
                highest = Math.max(highest, overlay.getId() + 1);
            }
        }
        this.cells = Long2ObjectMaps.unmodifiable(loaded);
        this.nextId = highest;
        for (ScheduledTick tick : ticks) {
            Cell cell = loaded.get(tick.pos());
            if (cell != null && cell.find(tick.id()) != null) {
                this.ticks.add(tick);
            }
        }
    }

    private static <T> Codec<List<T>> lenient(Codec<T> codec, String kind) {
        return Codec.PASSTHROUGH.listOf().xmap(entries -> decodeEach(codec, kind, entries), values -> encodeEach(codec, kind, values));
    }

    private static <T> List<T> decodeEach(Codec<T> codec, String kind, List<Dynamic<?>> entries) {
        List<T> decoded = new ArrayList<>(entries.size());
        for (Dynamic<?> entry : entries) {
            codec.parse(entry).resultOrPartial(error -> Offgrid.LOGGER.error("Dropping unreadable {}: {}", kind, error)).ifPresent(decoded::add);
        }
        return decoded;
    }

    private static <T> List<Dynamic<?>> encodeEach(Codec<T> codec, String kind, List<T> values) {
        List<Dynamic<?>> encoded = new ArrayList<>(values.size());
        for (T value : values) {
            codec.encodeStart(NbtOps.INSTANCE, value).resultOrPartial(error -> Offgrid.LOGGER.error("Could not save {}: {}", kind, error)).ifPresent(tag -> encoded.add(new Dynamic<>(NbtOps.INSTANCE, tag)));
        }
        return encoded;
    }

    private List<StoredCell> storedCells() {
        List<StoredCell> stored = new ArrayList<>(this.cells.size());
        for (Long2ObjectMap.Entry<Cell> entry : this.cells.long2ObjectEntrySet()) {
            Cell cell = entry.getValue();
            stored.add(new StoredCell(entry.getLongKey(), Optional.ofNullable(cell.offset()), cell.overlays()));
        }
        return stored;
    }

    @Nullable
    public Cell get(long pos) {
        return this.cells.get(pos);
    }

    public Cell getOrEmpty(long pos) {
        Cell cell = this.cells.get(pos);
        return cell == null ? Cell.EMPTY : cell;
    }

    public Long2ObjectMap<Cell> getCells() {
        return this.cells;
    }

    public boolean isEmpty() {
        return this.cells.isEmpty() && this.ticks.isEmpty();
    }

    public void put(long pos, Cell cell) {
        Long2ObjectOpenHashMap<Cell> next = new Long2ObjectOpenHashMap<>(this.cells);
        if (cell.isEmpty()) {
            next.remove(pos);
            this.ticks.removeIf(tick -> tick.pos() == pos);
        } else {
            next.put(pos, cell);
            this.ticks.removeIf(tick -> tick.pos() == pos && cell.find(tick.id()) == null);
        }
        this.cells = Long2ObjectMaps.unmodifiable(next);
    }

    public void replaceAll(Long2ObjectMap<Cell> cells) {
        Long2ObjectOpenHashMap<Cell> next = new Long2ObjectOpenHashMap<>(cells.size());
        for (Long2ObjectMap.Entry<Cell> entry : cells.long2ObjectEntrySet()) {
            if (!entry.getValue().isEmpty()) {
                next.put(entry.getLongKey(), entry.getValue());
            }
        }
        this.cells = Long2ObjectMaps.unmodifiable(next);
        this.ticks.clear();
    }

    public int allocateId() {
        return this.nextId++;
    }

    public List<ScheduledTick> getTicks() {
        return this.ticks;
    }

    public OptionalLong getTrigger(long pos, int id) {
        for (ScheduledTick tick : this.ticks) {
            if (tick.pos() == pos && tick.id() == id) return OptionalLong.of(tick.trigger());
        }
        return OptionalLong.empty();
    }

    public boolean schedule(long pos, int id, long trigger) {
        for (int i = 0; i < this.ticks.size(); i++) {
            ScheduledTick tick = this.ticks.get(i);
            if (tick.pos() == pos && tick.id() == id) {
                if (tick.trigger() <= trigger) return false;
                this.ticks.set(i, new ScheduledTick(pos, id, trigger));
                return true;
            }
        }
        this.ticks.add(new ScheduledTick(pos, id, trigger));
        return true;
    }

    public void unschedule(long pos, int id) {
        this.ticks.removeIf(tick -> tick.pos() == pos && tick.id() == id);
    }

    private record StoredCell(long pos, Optional<Vec3> offset, List<Overlay> overlays) {

        private static final Codec<StoredCell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("pos").forGetter(StoredCell::pos),
                Vec3.CODEC.optionalFieldOf("offset").forGetter(StoredCell::offset),
                lenient(Overlay.CODEC, "overlay").optionalFieldOf("overlays", List.of()).forGetter(StoredCell::overlays)
        ).apply(instance, StoredCell::new));
    }

    public record ScheduledTick(long pos, int id, long trigger) {

        private static final Codec<ScheduledTick> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("pos").forGetter(ScheduledTick::pos),
                Codec.INT.fieldOf("id").forGetter(ScheduledTick::id),
                Codec.LONG.fieldOf("trigger").forGetter(ScheduledTick::trigger)
        ).apply(instance, ScheduledTick::new));
    }
}
