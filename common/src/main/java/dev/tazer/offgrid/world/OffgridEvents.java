package dev.tazer.offgrid.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public final class OffgridEvents {

    private OffgridEvents() {
    }

    public static void onServerLevelTick(ServerLevel level) {
        OverlayTicker.tickServer(level);
    }

    public static void onClientLevelTick(Level level) {
        OverlayTicker.tickClient(level);
    }

    public static void onChunkLoad(Level level, LevelChunk chunk) {
        Cells.track(level, chunk);
        if (!(level instanceof ServerLevel serverLevel)) return;
        OffgridIndex index = OffgridIndex.of(level);
        OffgridChunk data = index == null ? null : index.getChunk(chunk.getPos().toLong());
        if (data == null) return;
        OverlayTicker.restore(serverLevel, data);
        for (Long2ObjectMap.Entry<Cell> entry : data.getCells().long2ObjectEntrySet()) {
            BlockPos host = BlockPos.of(entry.getLongKey());
            Cell cell = entry.getValue();
            for (Overlay overlay : cell.overlays()) {
                overlay.materialize(level, host);
            }
            if (cell.light() > 0) {
                level.getChunkSource().getLightEngine().checkBlock(host);
            }
        }
    }

    public static void onChunkUnload(Level level, LevelChunk chunk) {
        Cells.untrack(level, chunk);
    }

    public static void onLevelUnload(Level level) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            index.clear();
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        Interactions.forget(player);
    }

    public static void onChunkSent(ServerPlayer player, ServerLevel level, LevelChunk chunk) {
        Cells.sendChunk(player, level, chunk);
    }
}
