//? if <26 {
package dev.tazer.offgrid.neoforge.compat.polytone;

import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Overlay;
import net.mehvahdjukaar.polytone.api.VoxelVolumeApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public final class PolytoneOverlayLights {

    private PolytoneOverlayLights() {
    }

    public static void markDirty(BlockPos pos) {
        VoxelVolumeApi.markDirty(pos);
    }

    public static boolean hasCells(BlockGetter level, SectionPos section) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return false;
        OffgridChunk chunk = index.getChunk(ChunkPos.asLong(section.x(), section.z()));
        return chunk != null && chunk.hasCellsInSection(section.y());
    }

    public static BlockState brightest(BlockGetter level, int x, int y, int z, BlockState host) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return host;
        Cell cell = index.getCell(x, y, z);
        if (cell == null || !cell.hasOverlays()) return host;
        BlockState best = host;
        for (Overlay overlay : cell.overlays()) {
            BlockState state = overlay.getState();
            if (state.getLightEmission() > best.getLightEmission()) best = state;
        }
        return best;
    }
}
//?}
