package dev.tazer.offgrid.compat.light;

import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class OverlayLight {

    private OverlayLight() {
    }

    public static int emission(@Nullable BlockGetter level, int x, int y, int z, int original) {
        if (original >= 15) return original;
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return original;
        Cell cell = index.getCell(x, y, z);
        return cell == null ? original : Math.max(original, cell.light());
    }

    public static int emission(@Nullable BlockGetter level, BlockPos pos, int original) {
        return emission(level, pos.getX(), pos.getY(), pos.getZ(), original);
    }

    public static List<BlockPos> withSources(List<BlockPos> sources, ChunkAccess chunk) {
        if (!(chunk instanceof LevelChunk levelChunk)) return sources;
        OffgridChunk data = Services.PLATFORM.getChunkData(levelChunk);
        if (data == null) return sources;
        for (Long2ObjectMap.Entry<Cell> entry : data.getCells().long2ObjectEntrySet()) {
            if (entry.getValue().light() > 0) {
                sources.add(BlockPos.of(entry.getLongKey()));
            }
        }
        return sources;
    }
}
