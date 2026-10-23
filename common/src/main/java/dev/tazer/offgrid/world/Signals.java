package dev.tazer.offgrid.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class Signals {

    private Signals() {
    }

    public static int adjust(BlockState self, BlockGetter getter, BlockPos pos, Direction direction, boolean direct, int original) {
        if (!(getter instanceof Level level)) return original;
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty() || OverlayContext.isActiveAt(level, pos)) return original;
        LongOpenHashSet queries = Scope.current().getSignalQueries(direct);
        long key = pos.asLong();
        if (!queries.add(key)) return original;
        try {
            int result = original;
            if (!Offsets.isReadingSource() && Offsets.movedIntoSupport(level, pos, self)) {
                result = 0;
            }
            Cell cell = index.getCell(pos);
            if (cell != null) {
                for (Overlay overlay : cell.overlays()) {
                    BlockState state = overlay.getState();
                    if (!state.isSignalSource()) {
                        continue;
                    }
                    int signal = OverlayContext.call(level, pos, overlay, () -> direct ? state.getDirectSignal(level, pos, direction) : state.getSignal(level, pos, direction));
                    result = Math.max(result, signal);
                }
            }
            if (!Offsets.isReadingSource()) {
                result = Offsets.relocatedSignal(level, pos, direction, direct, result);
            }
            return result;
        } finally {
            queries.remove(key);
        }
    }

    public static boolean hasSource(Level level, BlockPos pos) {
        for (Overlay overlay : Cells.overlays(level, pos)) {
            if (overlay.getState().isSignalSource()) return true;
        }
        return false;
    }

    public static int analogOutput(Level level, BlockPos pos) {
        int best = 0;
        for (Overlay overlay : Cells.overlays(level, pos)) {
            BlockState state = overlay.getState();
            if (!state.hasAnalogOutputSignal()) {
                continue;
            }
            //? if >=1.21.5 {
            /*int output = OverlayContext.call(level, pos, overlay, () -> state.getAnalogOutputSignal(level, pos, Direction.NORTH));
            *///?} else {
            int output = OverlayContext.call(level, pos, overlay, () -> state.getAnalogOutputSignal(level, pos));
            //?}
            best = Math.max(best, output);
        }
        return best;
    }
}
