package dev.tazer.offgrid.forge;

import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.Overlay;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ForgeChunkData implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<OffgridChunk> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final String KEY = "data";
    private static final Map<ResourceKey<Level>, Long2ObjectMap<OffgridChunk>> GENERATING = new ConcurrentHashMap<>();

    private final LazyOptional<OffgridChunk> optional;

    private OffgridChunk data = new OffgridChunk();

    public ForgeChunkData() {
        this.optional = LazyOptional.of(() -> this.data);
    }

    @Nullable
    public static OffgridChunk generating(ResourceKey<Level> dimension, long chunk, boolean create) {
        Long2ObjectMap<OffgridChunk> chunks = GENERATING.computeIfAbsent(dimension, key -> Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>()));
        synchronized (chunks) {
            return create ? chunks.computeIfAbsent(chunk, key -> new OffgridChunk()) : chunks.get(chunk);
        }
    }

    public static void restoreGenerating(ResourceKey<Level> dimension, long chunk, OffgridChunk data) {
        Long2ObjectMap<OffgridChunk> chunks = GENERATING.computeIfAbsent(dimension, key -> Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>()));
        chunks.put(chunk, data);
    }

    public static void promoteGenerating(ResourceKey<Level> dimension, LevelChunk chunk) {
        Long2ObjectMap<OffgridChunk> chunks = GENERATING.get(dimension);
        OffgridChunk pending = chunks == null ? null : chunks.remove(chunk.getPos().toLong());
        if (pending == null || pending.isEmpty()) return;
        OffgridChunk data = chunk.getCapability(CAPABILITY).resolve().orElse(null);
        if (data == null) return;
        for (Long2ObjectMap.Entry<Cell> entry : pending.getCells().long2ObjectEntrySet()) {
            Cell cell = entry.getValue();
            List<Overlay> overlays = new ArrayList<>(cell.overlays().size());
            for (Overlay overlay : cell.overlays()) {
                overlays.add(new Overlay(data.allocateId(), overlay.getState(), overlay.getOffset(), overlay.getBlockEntityTag()));
            }
            data.put(entry.getLongKey(), Cell.of(cell.offset(), overlays));
        }
        chunk.setUnsaved(true);
    }

    public static void forgetGenerating(ResourceKey<Level> dimension) {
        GENERATING.remove(dimension);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return CAPABILITY.orEmpty(capability, this.optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!this.data.isEmpty()) {
            OffgridChunk.CODEC.encodeStart(NbtOps.INSTANCE, this.data).result().ifPresent(encoded -> tag.put(KEY, encoded));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        Tag stored = tag.get(KEY);
        if (stored != null) {
            OffgridChunk.CODEC.parse(NbtOps.INSTANCE, stored).result().ifPresent(loaded -> this.data = loaded);
        }
    }
}
