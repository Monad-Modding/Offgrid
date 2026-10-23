//? if <26 {
package dev.tazer.offgrid.compat.create;

import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.Overlays;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record PrintedOverlay(BlockPos pos, BlockState state, Vec3 offset, @Nullable CompoundTag tag) {

    public static List<PrintedOverlay> collect(BlockGetter schematic) {
        OffgridIndex index = OffgridIndex.of(schematic);
        if (index == null || index.isEmpty()) return List.of();
        List<PrintedOverlay> overlays = new ArrayList<>();
        for (Long2ObjectMap.Entry<OffgridChunk> chunk : index.getChunks().long2ObjectEntrySet()) {
            for (Long2ObjectMap.Entry<Cell> cell : chunk.getValue().getCells().long2ObjectEntrySet()) {
                BlockPos pos = BlockPos.of(cell.getLongKey());
                for (Overlay overlay : cell.getValue().overlays()) {
                    overlays.add(new PrintedOverlay(pos, overlay.getState(), overlay.getOffset(), overlay.getBlockEntityTag()));
                }
            }
        }
        overlays.sort(Comparator.comparingInt((PrintedOverlay overlay) -> overlay.pos().getY()).thenComparingInt(overlay -> overlay.pos().getZ()).thenComparingInt(overlay -> overlay.pos().getX()));
        return List.copyOf(overlays);
    }

    public boolean isPrintable(Level level) {
        if (!level.isLoaded(this.pos)) return false;
        for (Overlay existing : Cells.overlays(level, this.pos)) {
            if (existing.isSamePlacement(this.state, this.offset)) return false;
        }
        return Overlays.survives(level, this.pos, this.state, this.offset, null, null) && !Overlays.obstructed(level, this.pos, this.state, this.offset, null);
    }
}
//?}
