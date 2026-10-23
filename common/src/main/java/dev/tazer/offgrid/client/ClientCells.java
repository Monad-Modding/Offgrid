package dev.tazer.offgrid.client;

import dev.tazer.offgrid.network.CellData;
import dev.tazer.offgrid.network.CellSync;
import dev.tazer.offgrid.network.ChunkSync;
import dev.tazer.offgrid.network.MovingCell;
import dev.tazer.offgrid.network.OverlayData;
import dev.tazer.offgrid.network.OverlayEvent;
import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ClientCells {

    private ClientCells() {
    }

    public static void markDirty(Level level, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (level != minecraft.level) return;
        //? if >=26.2 {
        /*minecraft.levelExtractor.setBlocksDirty(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        *///?} else {
        minecraft.levelRenderer.setBlocksDirty(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        //?}
    }

    public static void applyChunk(ChunkSync packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        long key = packet.chunk();
        LevelChunk chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
        if (chunk == null) return;
        OffgridChunk data = Services.PLATFORM.getOrCreateChunkData(chunk);
        Long2ObjectMap<Cell> previous = data.getCells();
        Long2ObjectOpenHashMap<Cell> cells = new Long2ObjectOpenHashMap<>(packet.cells().size());
        for (CellData cell : packet.cells()) {
            cells.put(cell.pos(), rebuild(level, cell, previous.get(cell.pos())));
        }
        for (Long2ObjectMap.Entry<Cell> entry : previous.long2ObjectEntrySet()) {
            if (!cells.containsKey(entry.getLongKey())) {
                discard(entry.getValue(), null);
            }
        }
        data.replaceAll(cells);
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            index.track(key, data);
        }
        for (long pos : cells.keySet()) {
            refresh(level, BlockPos.of(pos), previous.get(pos), cells.get(pos));
        }
    }

    public static void applyCell(CellSync packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockPos pos = BlockPos.of(packet.cell().pos());
        Cell previous = Cells.get(level, pos);
        if (previous != null) {
            for (int id : packet.broken()) {
                Overlay broken = previous.find(id);
                if (broken != null) {
                    OverlayEffects.destroy(level, pos, broken);
                }
            }
        }
        Cell rebuilt = rebuild(level, packet.cell(), previous);
        Cells.updateSilently(level, pos, cell -> rebuilt);
    }

    public static void applyEvent(OverlayEvent packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockPos pos = BlockPos.of(packet.pos());
        Overlay overlay = Cells.find(level, pos, packet.id());
        if (overlay != null) {
            OverlayContext.run(level, pos, overlay, () -> overlay.getState().triggerEvent(level, pos, packet.type(), packet.param()));
        }
    }

    private static Cell rebuild(ClientLevel level, CellData data, @Nullable Cell previous) {
        BlockPos host = BlockPos.of(data.pos());
        List<Overlay> overlays = new ArrayList<>(data.overlays().size());
        for (OverlayData incoming : data.overlays()) {
            Overlay existing = previous == null ? null : previous.find(incoming.id());
            if (existing != null && existing.getState().getBlock() == incoming.state().getBlock() && existing.getOffset().equals(incoming.offset())) {
                if (existing.getState() != incoming.state()) {
                    existing.setState(incoming.state());
                }
                existing.loadBlockEntity(level, incoming.tag());
                overlays.add(existing);
            } else {
                Overlay created = new Overlay(incoming.id(), incoming.state(), incoming.offset(), incoming.tag());
                created.materialize(level, host);
                overlays.add(created);
            }
        }
        Cell rebuilt = Cell.of(data.offset(), overlays);
        if (previous != null) {
            discard(previous, rebuilt);
        }
        return rebuilt;
    }

    private static void discard(Cell previous, @Nullable Cell kept) {
        for (Overlay overlay : previous.overlays()) {
            if (kept == null || !kept.overlays().contains(overlay)) {
                overlay.discardBlockEntity();
            }
        }
    }

    private static void refresh(ClientLevel level, BlockPos pos, @Nullable Cell before, Cell after) {
        markDirty(level, pos);
        if (before == null || before.light() != after.light()) {
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }

    public static void applyMovingCell(MovingCell packet) {
        ClientLevel level = Minecraft.getInstance().level;
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null && packet.cell() != null) {
            index.putMovingCell(packet.pos(), packet.cell(), level.getGameTime());
        }
    }
}
