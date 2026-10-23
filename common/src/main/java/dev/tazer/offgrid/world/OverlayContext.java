package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class OverlayContext {

    private OverlayContext() {
    }

    @Nullable
    public static Frame current() {
        Frame frame = Scope.get(Scope.FRAME);
        return frame == null || frame.getOverlay() == null ? null : frame;
    }

    @Nullable
    public static Frame at(BlockGetter getter, BlockPos pos) {
        Frame frame = Scope.get(Scope.FRAME);
        return frame != null && frame.getLevel() == getter && frame.getHost().equals(pos) ? frame : null;
    }

    public static boolean isActiveAt(BlockGetter getter, BlockPos pos) {
        return at(getter, pos) != null;
    }

    public static Frame push(Level level, BlockPos host, Overlay overlay) {
        return enter(new Frame(level, host.immutable(), overlay, Scope.get(Scope.FRAME)));
    }

    public static void pop(Frame frame) {
        Scope.restore(Scope.FRAME, frame.getPrevious());
    }

    public static void run(Level level, BlockPos host, Overlay overlay, Runnable action) {
        Frame frame = push(level, host, overlay);
        try {
            action.run();
        } finally {
            pop(frame);
        }
    }

    public static <T> T call(Level level, BlockPos host, Overlay overlay, Supplier<T> action) {
        Frame frame = push(level, host, overlay);
        try {
            return action.get();
        } finally {
            pop(frame);
        }
    }

    public static <T> T outside(Supplier<T> action) {
        Frame current = Scope.get(Scope.FRAME);
        if (current == null) return action.get();
        Frame frame = enter(new Frame(null, BlockPos.ZERO, null, current));
        try {
            return action.get();
        } finally {
            pop(frame);
        }
    }

    public static void runOutside(Runnable action) {
        outside(() -> {
            action.run();
            return null;
        });
    }

    private static Frame enter(Frame frame) {
        Scope.bind(Scope.FRAME, frame);
        return frame;
    }

    public static final class Frame {

        @Nullable
        private final Level level;
        private final BlockPos host;
        @Nullable
        private final Overlay overlay;
        @Nullable
        private final Frame previous;

        private boolean destroyEffects;

        private Frame(@Nullable Level level, BlockPos host, @Nullable Overlay overlay, @Nullable Frame previous) {
            this.level = level;
            this.host = host;
            this.overlay = overlay;
            this.previous = previous;
        }

        @Nullable
        public Level getLevel() {
            return this.level;
        }

        public BlockPos getHost() {
            return this.host;
        }

        @Nullable
        public Overlay getOverlay() {
            return this.overlay;
        }

        @Nullable
        public Frame getPrevious() {
            return this.previous;
        }

        public boolean hasDestroyEffects() {
            return this.destroyEffects;
        }

        public void requestDestroyEffects() {
            this.destroyEffects = true;
        }
    }
}
