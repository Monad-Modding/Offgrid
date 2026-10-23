package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Offsets {

    private Offsets() {
    }

    @Nullable
    public static Vec3 at(@Nullable BlockGetter getter, BlockPos pos) {
        Cell cell = OffgridIndex.cellAt(getter, pos);
        return cell == null ? null : cell.offset();
    }

    @Nullable
    public static Vec3 of(@Nullable BlockGetter getter, BlockPos pos, BlockState state) {
        OffgridIndex index = OffgridIndex.of(getter);
        if (index == null) return null;
        Cell cell = index.getCell(pos.getX(), pos.getY(), pos.getZ());
        if (cell == null || cell.offset() == null || OverlayContext.isActiveAt(getter, pos)) return null;
        return getter.getBlockState(pos).is(state.getBlock()) ? cell.offset() : null;
    }

    public static void set(ServerLevel level, BlockPos pos, @Nullable Vec3 offset) {
        Vec3 stored = offset == null || Overlays.isZero(offset) ? null : offset;
        Cells.update(level, pos, cell -> cell.withOffset(stored));
    }

    public static boolean movedIntoSupport(BlockGetter level, BlockPos pos, BlockState state) {
        Vec3 offset = of(level, pos, state);
        if (offset == null) return false;
        Direction support = Support.direction(state);
        double along = support.getAxis().choose(offset.x, offset.y, offset.z);
        return support.getAxisDirection().getStep() < 0 ? along < -Support.EPSILON : along > Support.EPSILON;
    }

    public static boolean isSuppressed() {
        return Scope.get(Scope.SUPPRESSED_OFFSETS);
    }

    public static void suppress(Runnable action) {
        Scope.run(Scope.SUPPRESSED_OFFSETS, Boolean.TRUE, action);
    }

    public static boolean isReadingSource() {
        return Scope.get(Scope.READING_SOURCE);
    }

    public static int relocatedSignal(Level level, BlockPos pos, Direction direction, boolean direct, int original) {
        int best = original;
        for (Direction side : Direction.values()) {
            BlockPos neighbour = pos.relative(side);
            if (at(level, neighbour) == null || !level.hasChunkAt(neighbour)) {
                continue;
            }
            BlockState state = level.getBlockState(neighbour);
            if (!state.isSignalSource() || Support.direction(state) != side.getOpposite() || !movedIntoSupport(level, neighbour, state)) {
                continue;
            }
            int signal = Scope.with(Scope.READING_SOURCE, Boolean.TRUE, () -> direct ? state.getDirectSignal(level, neighbour, direction) : state.getSignal(level, neighbour, direction));
            best = Math.max(best, signal);
        }
        return best;
    }

    public static boolean hostsRelocatedSource(Level level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = pos.relative(side);
            if (at(level, neighbour) == null || !level.hasChunkAt(neighbour)) {
                continue;
            }
            BlockState state = level.getBlockState(neighbour);
            if (state.isSignalSource() && Support.direction(state) == side.getOpposite() && movedIntoSupport(level, neighbour, state)) return true;
        }
        return false;
    }

    @Nullable
    public static BlockPos relocationTarget(Level level, BlockPos pos) {
        if (at(level, pos) == null) return null;
        BlockState state = level.getBlockState(pos);
        if (!state.isSignalSource() || !movedIntoSupport(level, pos, state)) return null;
        return pos.relative(Support.direction(state));
    }
}
