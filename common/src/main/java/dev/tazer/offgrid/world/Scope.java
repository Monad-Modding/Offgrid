package dev.tazer.offgrid.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class Scope {

    public static final Slot<OverlayContext.Frame> FRAME = new Slot<>(null);
    public static final Slot<Transaction> TRANSACTION = new Slot<>(null);
    public static final Slot<Vec3> EFFECT_OFFSET = new Slot<>(Vec3.ZERO);
    public static final Slot<Placement.Plan> PLAN = new Slot<>(null);
    public static final Slot<UseOnContext> PLACING = new Slot<>(null);
    public static final Slot<Connections.Viewer> VIEWER = new Slot<>(null);
    public static final Slot<Boolean> READING_SOURCE = new Slot<>(Boolean.FALSE);
    public static final Slot<Boolean> SUPPRESSED_OFFSETS = new Slot<>(Boolean.FALSE);
    public static final Slot<Boolean> RESOLVING_SHAPE = new Slot<>(Boolean.FALSE);
    public static final Slot<Boolean> HIGHLIGHTING = new Slot<>(Boolean.FALSE);
    public static final Slot<AtomicReference<BlockState>> PLACED_HALF = new Slot<>(null);
    public static final Slot<List<Layers.Replaced>> REPLACED_LAYERS = new Slot<>(null);
    private static final ThreadLocal<Scope> CURRENT = ThreadLocal.withInitial(Scope::new);

    private final Object[] values = new Object[Slot.ALL.size()];
    private final LongOpenHashSet signalQueries = new LongOpenHashSet();
    private final LongOpenHashSet directSignalQueries = new LongOpenHashSet();
    private final ArrayDeque<BlockPos> settleQueue = new ArrayDeque<>();
    private final LongOpenHashSet settleQueued = new LongOpenHashSet();

    private boolean settling;

    private Scope() {
        for (Slot<?> slot : Slot.ALL) {
            this.values[slot.index] = slot.fallback;
        }
    }

    public static Scope current() {
        return CURRENT.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Slot<T> slot) {
        if (slot.bound.get() == 0) return slot.fallback;
        return (T) CURRENT.get().values[slot.index];
    }

    @SuppressWarnings("unchecked")
    public static <T> T bind(Slot<T> slot, T value) {
        Scope scope = CURRENT.get();
        T previous = (T) scope.values[slot.index];
        scope.values[slot.index] = value;
        slot.bound.incrementAndGet();
        return previous;
    }

    public static <T> void restore(Slot<T> slot, T previous) {
        CURRENT.get().values[slot.index] = previous;
        slot.bound.decrementAndGet();
    }

    public static <T, R> R with(Slot<T> slot, T value, Supplier<R> action) {
        T previous = bind(slot, value);
        try {
            return action.get();
        } finally {
            restore(slot, previous);
        }
    }

    public static <T> void run(Slot<T> slot, T value, Runnable action) {
        T previous = bind(slot, value);
        try {
            action.run();
        } finally {
            restore(slot, previous);
        }
    }

    public LongOpenHashSet getSignalQueries(boolean direct) {
        return direct ? this.directSignalQueries : this.signalQueries;
    }

    public ArrayDeque<BlockPos> getSettleQueue() {
        return this.settleQueue;
    }

    public LongOpenHashSet getSettleQueued() {
        return this.settleQueued;
    }

    public boolean isSettling() {
        return this.settling;
    }

    public void setSettling(boolean settling) {
        this.settling = settling;
    }

    public static final class Slot<T> {

        private static final List<Slot<?>> ALL = new ArrayList<>();

        private final int index;
        private final T fallback;
        private final AtomicInteger bound = new AtomicInteger();

        private Slot(T fallback) {
            this.index = ALL.size();
            this.fallback = fallback;
            ALL.add(this);
        }
    }
}
