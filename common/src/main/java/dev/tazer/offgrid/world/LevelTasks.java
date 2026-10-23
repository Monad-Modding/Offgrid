package dev.tazer.offgrid.world;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class LevelTasks {

    private final LongLinkedOpenHashSet promotions = new LongLinkedOpenHashSet();
    private final LongLinkedOpenHashSet revalidations = new LongLinkedOpenHashSet();
    private final PriorityQueue<PendingTick> ticks = new PriorityQueue<>(Comparator.comparingLong(PendingTick::trigger));
    private final ArrayDeque<PendingEvent> events = new ArrayDeque<>();

    public LongLinkedOpenHashSet getPromotions() {
        return this.promotions;
    }

    public LongLinkedOpenHashSet getRevalidations() {
        return this.revalidations;
    }

    public PriorityQueue<PendingTick> getTicks() {
        return this.ticks;
    }

    public ArrayDeque<PendingEvent> getEvents() {
        return this.events;
    }

    public void clear() {
        this.promotions.clear();
        this.revalidations.clear();
        this.ticks.clear();
        this.events.clear();
    }

    public record PendingTick(long trigger, long pos, int id) {
    }

    public record PendingEvent(BlockPos host, int id, Block block, int type, int param) {
    }
}
