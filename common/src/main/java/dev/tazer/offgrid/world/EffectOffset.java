package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class EffectOffset {

    private EffectOffset() {
    }

    public static Vec3 push(Vec3 offset) {
        return Scope.bind(Scope.EFFECT_OFFSET, offset);
    }

    public static void pop(Vec3 previous) {
        Scope.restore(Scope.EFFECT_OFFSET, previous);
    }

    public static Vec3 forBlock(BlockGetter level, BlockPos pos, BlockState state) {
        Vec3 offset = Offsets.of(level, pos, state);
        return offset == null ? Vec3.ZERO : offset;
    }

    public static Vec3 at(Level level, double x, double y, double z) {
        Vec3 explicit = Scope.get(Scope.EFFECT_OFFSET);
        if (explicit != Vec3.ZERO) return explicit;
        OverlayContext.Frame frame = OverlayContext.current();
        if (frame == null || frame.getLevel() != level || frame.getOverlay() == null) return Vec3.ZERO;
        BlockPos host = frame.getHost();
        if (x < host.getX() - 1.0 || x > host.getX() + 2.0 || y < host.getY() - 1.0 || y > host.getY() + 2.0 || z < host.getZ() - 1.0 || z > host.getZ() + 2.0) return Vec3.ZERO;
        return frame.getOverlay().getOffset();
    }

    public static void run(Vec3 offset, Runnable action) {
        if (offset == Vec3.ZERO) {
            action.run();
            return;
        }
        Scope.run(Scope.EFFECT_OFFSET, offset, action);
    }
}
