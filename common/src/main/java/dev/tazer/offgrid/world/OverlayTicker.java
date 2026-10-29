package dev.tazer.offgrid.world;

import dev.tazer.offgrid.Offgrid;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.OverlayEvent;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.Nullable;
//? if >=26 {
/*import net.minecraft.world.level.gamerules.GameRules;
*///?} else {
import net.minecraft.world.level.GameRules;
//?}

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class OverlayTicker {

    private static final double RANDOM_TICK_RANGE = 128.0;

    private OverlayTicker() {
    }

    public static void tickServer(ServerLevel level) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null) return;
        if (!index.isEmpty()) {
            tickOverlays(level, index, randomTickSpeed(level));
            runScheduledTicks(level, index);
        }
        runEvents(level, index);
        runUncovers(level, index);
        runPromotions(level, index);
        runRevalidations(level, index);
        runUndersides(level, index);
        Interactions.tickDestruction(level);
    }

    public static void tickClient(Level level) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return;
        tickOverlays(level, index, 0);
    }

    public static void schedule(ServerLevel level, BlockPos host, Overlay overlay, long trigger) {
        OffgridChunk data = Cells.data(level, host, false);
        OffgridIndex index = OffgridIndex.of(level);
        if (data == null || index == null || !data.schedule(host.asLong(), overlay.getId(), trigger)) return;
        Cells.markUnsaved(level, host);
        index.getTasks().getTicks().add(new LevelTasks.PendingTick(trigger, host.asLong(), overlay.getId()));
    }

    public static boolean reroute(LevelTicks<?> ticks, ScheduledTick<?> tick) {
        OverlayContext.Frame frame = OverlayContext.current();
        if (frame == null || frame.getOverlay() == null || !(frame.getLevel() instanceof ServerLevel level) || !frame.getHost().equals(tick.pos())) return false;
        if (level.getBlockTicks() != ticks && level.getFluidTicks() != ticks) return false;
        if (tick.type() instanceof Block) {
            schedule(level, frame.getHost(), frame.getOverlay(), tick.triggerTick());
        }
        return true;
    }

    public static void restore(ServerLevel level, OffgridChunk data) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null) return;
        for (OffgridChunk.ScheduledTick tick : data.getTicks()) {
            index.getTasks().getTicks().add(new LevelTasks.PendingTick(tick.trigger(), tick.pos(), tick.id()));
        }
    }

    public static void queueEvent(ServerLevel level, BlockPos host, Overlay overlay, Block block, int type, int param) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            index.getTasks().getEvents().add(new LevelTasks.PendingEvent(host.immutable(), overlay.getId(), block, type, param));
        }
    }

    private static int randomTickSpeed(ServerLevel level) {
        //? if >=26 {
        /*return level.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
        *///?} else {
        return level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        //?}
    }

    private static boolean randomTicksIn(ServerLevel level, long chunkKey) {
        //? if >=26 {
        /*return level.canSpawnEntitiesInChunk(ChunkPos.unpack(chunkKey));
        *///?} else {
        return level.isNaturalSpawningAllowed(new ChunkPos(chunkKey));
        //?}
    }

    private static void tickOverlays(Level level, OffgridIndex index, int randomSpeed) {
        List<Job> jobs = new ArrayList<>();
        for (Long2ObjectMap.Entry<OffgridChunk> chunkEntry : index.getChunks().long2ObjectEntrySet()) {
            long chunkKey = chunkEntry.getLongKey();
            if (!level.shouldTickBlocksAt(chunkKey)) {
                continue;
            }
            boolean randomChunk = randomSpeed > 0 && level instanceof ServerLevel serverLevel && randomTicksIn(serverLevel, chunkKey);
            for (Long2ObjectMap.Entry<Cell> cellEntry : chunkEntry.getValue().getCells().long2ObjectEntrySet()) {
                Cell cell = cellEntry.getValue();
                if (!cell.hasOverlays()) {
                    continue;
                }
                long packed = cellEntry.getLongKey();
                BlockPos host = null;
                boolean playerChecked = false;
                boolean playerNear = false;
                for (Overlay overlay : cell.overlays()) {
                    BlockState state = overlay.getState();
                    boolean random = false;
                    if (randomChunk && state.isRandomlyTicking() && level.getRandom().nextInt(4096) < randomSpeed) {
                        if (!playerChecked) {
                            playerNear = level.hasNearbyAlivePlayer(BlockPos.getX(packed) + 0.5, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(BlockPos.getY(packed))) + 8.0, BlockPos.getZ(packed) + 0.5, RANDOM_TICK_RANGE);
                            playerChecked = true;
                        }
                        random = playerNear;
                    }
                    BlockEntityTicker<BlockEntity> ticker = null;
                    if (state.hasBlockEntity()) {
                        if (host == null) {
                            host = BlockPos.of(packed);
                        }
                        overlay.materialize(level, host);
                        ticker = overlay.getTicker(level);
                    }
                    if (random || ticker != null) {
                        if (host == null) {
                            host = BlockPos.of(packed);
                        }
                        jobs.add(new Job(host, overlay, random, ticker));
                    }
                }
            }
        }
        for (Job job : jobs) {
            run(level, job);
        }
    }

    private static void run(Level level, Job job) {
        Overlay overlay = job.overlay();
        if (!Cells.contains(level, job.host(), overlay)) return;
        OverlayContext.Frame frame = OverlayContext.push(level, job.host(), overlay);
        try {
            BlockEntity blockEntity = overlay.getBlockEntity();
            if (job.ticker() != null && blockEntity != null && !blockEntity.isRemoved()) {
                job.ticker().tick(level, job.host(), overlay.getState(), blockEntity);
            }
            if (job.random() && level instanceof ServerLevel serverLevel && Cells.contains(level, job.host(), overlay)) {
                overlay.getState().randomTick(serverLevel, job.host(), serverLevel.getRandom());
            }
        } catch (RuntimeException exception) {
            overlay.stopTicking();
            Offgrid.LOGGER.error("Overlay {} at {} failed to tick and will no longer tick", overlay.getState(), job.host(), exception);
        } finally {
            OverlayContext.pop(frame);
        }
    }

    private static void runScheduledTicks(ServerLevel level, OffgridIndex index) {
        PriorityQueue<LevelTasks.PendingTick> queue = index.getTasks().getTicks();
        long now = level.getGameTime();
        List<LevelTasks.PendingTick> due = new ArrayList<>();
        while (!queue.isEmpty() && queue.peek().trigger() <= now) {
            due.add(queue.poll());
        }
        for (LevelTasks.PendingTick tick : due) {
            BlockPos host = BlockPos.of(tick.pos());
            OffgridChunk data = index.getChunk(ChunkPos.asLong(host.getX() >> 4, host.getZ() >> 4));
            if (data == null || !data.getTicks().contains(new OffgridChunk.ScheduledTick(tick.pos(), tick.id(), tick.trigger()))) {
                continue;
            }
            data.unschedule(tick.pos(), tick.id());
            Overlay overlay = Cells.find(level, host, tick.id());
            if (overlay == null) {
                continue;
            }
            OverlayContext.run(level, host, overlay, () -> overlay.getState().tick(level, host, level.getRandom()));
        }
    }

    private static void runEvents(ServerLevel level, OffgridIndex index) {
        int budget = 4096;
        while (!index.getTasks().getEvents().isEmpty() && budget-- > 0) {
            LevelTasks.PendingEvent event = index.getTasks().getEvents().poll();
            Overlay overlay = Cells.find(level, event.host(), event.id());
            if (overlay == null || !overlay.getState().is(event.block()) || !level.shouldTickBlocksAt(ChunkPos.asLong(event.host().getX() >> 4, event.host().getZ() >> 4))) {
                continue;
            }
            boolean fired = OverlayContext.call(level, event.host(), overlay, () -> overlay.getState().triggerEvent(level, event.host(), event.type(), event.param()));
            if (fired) {
                Cells.sendTracking(level, event.host(), OffgridNetwork.EVENT, new OverlayEvent(event.host().asLong(), event.id(), event.type(), event.param()));
            }
        }
    }

    private static void runRevalidations(ServerLevel level, OffgridIndex index) {
        LongLinkedOpenHashSet revalidations = index.getTasks().getRevalidations();
        if (revalidations.isEmpty()) return;
        long[] pending = revalidations.toLongArray();
        revalidations.clear();
        for (long packed : pending) {
            BlockPos pos = BlockPos.of(packed);
            if (level.hasChunkAt(pos)) {
                Overlays.revalidateOffset(level, pos);
            }
        }
    }

    private static void runUndersides(ServerLevel level, OffgridIndex index) {
        LongLinkedOpenHashSet undersides = index.getTasks().getUndersides();
        if (undersides.isEmpty()) return;
        long[] pending = undersides.toLongArray();
        undersides.clear();
        for (long packed : pending) {
            BlockPos pos = BlockPos.of(packed);
            if (level.hasChunkAt(pos)) {
                Layers.settleUnderside(level, pos);
            }
        }
    }

    private static void runUncovers(ServerLevel level, OffgridIndex index) {
        LongLinkedOpenHashSet uncovers = index.getTasks().getUncovers();
        if (uncovers.isEmpty()) return;
        long[] pending = uncovers.toLongArray();
        uncovers.clear();
        for (long packed : pending) {
            BlockPos pos = BlockPos.of(packed);
            if (level.hasChunkAt(pos)) {
                Covers.uncover(level, pos);
            }
        }
    }

    private static void runPromotions(ServerLevel level, OffgridIndex index) {
        LongLinkedOpenHashSet promotions = index.getTasks().getPromotions();
        if (promotions.isEmpty()) return;
        long[] pending = promotions.toLongArray();
        promotions.clear();
        for (long packed : pending) {
            BlockPos pos = BlockPos.of(packed);
            if (level.hasChunkAt(pos) && Cells.hasOverlays(level, pos)) {
                Overlays.promote(level, pos);
            }
        }
    }

    private record Job(BlockPos host, Overlay overlay, boolean random, @Nullable BlockEntityTicker<BlockEntity> ticker) {
    }
}
