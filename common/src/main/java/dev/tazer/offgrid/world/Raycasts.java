package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class Raycasts {

    private Raycasts() {
    }

    @Nullable
    public static BlockHitResult clipBlock(BlockGetter getter, Vec3 from, Vec3 to, BlockPos pos, VoxelShape shape, BlockState state) {
        BlockHitResult result = shape.clip(from, to, pos);
        if (result == null) return null;
        BlockHitResult interaction = state.getInteractionShape(getter, pos).clip(from, to, pos);
        if (interaction != null && interaction.getLocation().subtract(from).lengthSqr() < result.getLocation().subtract(from).lengthSqr()) return result.withDirection(interaction.getDirection());
        return result;
    }

    @Nullable
    public static BlockHitResult clip(Level level, Vec3 from, Vec3 to, BlockPos pos, @Nullable BlockHitResult original) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return original;
        BlockHitResult best = original;
        double nearest = original == null ? Double.MAX_VALUE : original.getLocation().distanceToSqr(from);
        best = test(level, index, pos, from, to, best, nearest);
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            if (index.getCell(neighbour) == null) {
                continue;
            }
            double distance = best == null ? Double.MAX_VALUE : best.getLocation().distanceToSqr(from);
            best = test(level, index, neighbour, from, to, best, distance);
        }
        return best;
    }

    @Nullable
    private static BlockHitResult test(Level level, OffgridIndex index, BlockPos pos, Vec3 from, Vec3 to, @Nullable BlockHitResult best, double nearest) {
        Cell cell = index.getCell(pos);
        if (cell == null) return best;
        BlockHitResult result = best;
        double closest = nearest;
        if (cell.offset() != null && level.hasChunkAt(pos)) {
            BlockState state = Cells.realState(level, pos);
            VoxelShape shape = state.getShape(level, pos, CollisionContext.empty());
            BlockHitResult hit = shape.isEmpty() ? null : shape.clip(from, to, pos);
            if (hit != null) {
                double distance = hit.getLocation().distanceToSqr(from);
                if (distance < closest) {
                    closest = distance;
                    result = hit;
                }
            }
        }
        for (Overlay overlay : cell.overlays()) {
            VoxelShape shape = OverlayShapes.of(level, pos, overlay);
            if (shape.isEmpty()) {
                continue;
            }
            BlockHitResult hit = shape.clip(from, to, pos);
            if (hit == null) {
                continue;
            }
            double distance = hit.getLocation().distanceToSqr(from);
            if (distance < closest) {
                closest = distance;
                result = new OverlayHitResult(hit.getLocation(), hit.getDirection(), pos, overlay.getId());
            }
        }
        return result;
    }
}
