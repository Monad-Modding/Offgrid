package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class OverlayShapes {

    private OverlayShapes() {
    }

    public static VoxelShape of(BlockState state, Vec3 offset) {
        VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        return shape.isEmpty() ? shape : shape.move(offset.x, offset.y, offset.z);
    }

    public static VoxelShape of(Level level, BlockPos host, Overlay overlay) {
        return of(level, host, overlay.getState(), overlay.getOffset(), overlay);
    }

    public static VoxelShape of(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing) {
        Blocklogging.Blocklog cut = Blocklogging.at(level, host, state, offset);
        if (cut != null) return cut.outline();
        if (Scope.get(Scope.RESOLVING_SHAPE)) return of(state, offset);
        Overlay probe = existing != null ? existing : new Overlay(-1, state, offset, null);
        return Scope.with(Scope.RESOLVING_SHAPE, Boolean.TRUE, () -> OverlayContext.call(level, host, probe, () -> {
            try {
                VoxelShape shape = state.getShape(level, host, CollisionContext.empty());
                return shape.isEmpty() ? shape : shape.move(offset.x, offset.y, offset.z);
            } catch (RuntimeException exception) {
                return of(state, offset);
            }
        }));
    }

    public static VoxelShape support(Level level, BlockPos host, Overlay overlay) {
        Blocklogging.Blocklog cut = Blocklogging.at(level, host, overlay);
        if (cut != null) return cut.support();
        BlockState state = overlay.getState();
        Vec3 offset = overlay.getOffset();
        VoxelShape shape = OverlayContext.call(level, host, overlay, () -> state.getBlockSupportShape(level, host));
        return shape.isEmpty() ? shape : shape.move(offset.x, offset.y, offset.z);
    }

    public static VoxelShape collision(BlockGetter getter, BlockPos host, Overlay overlay) {
        Blocklogging.Blocklog cut = Blocklogging.at(getter, host, overlay);
        return cut != null ? cut.collision() : collision(overlay);
    }

    public static VoxelShape collision(Overlay overlay) {
        Vec3 offset = overlay.getOffset();
        VoxelShape shape = overlay.getState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        return shape.isEmpty() ? shape : shape.move(offset.x, offset.y, offset.z);
    }

    public static AABB bounds(Level level, BlockPos host, Overlay overlay) {
        VoxelShape shape = of(level, host, overlay);
        if (!shape.isEmpty()) return shape.bounds();
        Vec3 offset = overlay.getOffset();
        return new AABB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75).move(offset.x, offset.y, offset.z);
    }

    public static Vec3 centre(Level level, BlockPos host, Overlay overlay) {
        AABB bounds = bounds(level, host, overlay);
        return new Vec3(host.getX() + (bounds.minX + bounds.maxX) * 0.5, host.getY() + (bounds.minY + bounds.maxY) * 0.5, host.getZ() + (bounds.minZ + bounds.maxZ) * 0.5);
    }
}
