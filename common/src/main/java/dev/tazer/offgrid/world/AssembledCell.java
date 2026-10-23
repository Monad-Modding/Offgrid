package dev.tazer.offgrid.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public record AssembledCell(BlockPos origin, @Nullable Vec3 offset, List<AssembledOverlay> overlays) {

    private static final String POS_KEY = "pos";
    private static final String CELL_KEY = "cell";
    public static final Codec<AssembledCell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("origin").forGetter(AssembledCell::origin),
            Vec3.CODEC.optionalFieldOf("offset").forGetter(cell -> Optional.ofNullable(cell.offset())),
            AssembledOverlay.CODEC.listOf().optionalFieldOf("overlays", List.of()).forGetter(AssembledCell::overlays)
    ).apply(instance, (origin, offset, overlays) -> new AssembledCell(origin, offset.orElse(null), overlays)));

    public static List<AssembledCell> assemble(ServerLevel level, Iterable<BlockPos> positions) {
        List<AssembledCell> assembled = new ArrayList<>();
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return assembled;
        for (BlockPos pos : positions) {
            AssembledCell cell = assemble(level, pos);
            if (cell != null) {
                assembled.add(cell);
            }
        }
        return assembled;
    }

    @Nullable
    public static AssembledCell assemble(ServerLevel level, BlockPos pos) {
        return assemble(level, pos, overlay -> true, true);
    }

    public static Map<BlockPos, AssembledCell> assembleWithHeld(ServerLevel level, Collection<BlockPos> positions) {
        return assembleHeld(level, positions, held(level, positions));
    }

    public static Map<BlockPos, AssembledCell> assembleHeld(ServerLevel level, Collection<BlockPos> positions, Held held) {
        Map<BlockPos, AssembledCell> assembled = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, List<Overlay>> entry : held.overlays().entrySet()) {
            List<Overlay> taken = entry.getValue();
            AssembledCell cell = assemble(level, entry.getKey(), taken::contains, false);
            if (cell != null) {
                assembled.put(entry.getKey(), cell);
            }
        }
        for (BlockPos pos : positions) {
            AssembledCell cell = assemble(level, pos);
            if (cell != null) {
                assembled.put(pos.immutable(), cell);
            }
        }
        return assembled;
    }

    public static Held held(ServerLevel level, Collection<BlockPos> positions) {
        Held held = new Held(new LinkedHashMap<>(), new LinkedHashSet<>());
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return held;
        Set<BlockPos> carriers = new HashSet<>();
        for (BlockPos pos : positions) {
            carriers.add(pos.immutable());
        }
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>(carriers);
        while (!frontier.isEmpty()) {
            BlockPos carrier = frontier.poll();
            gather(level, carrier, carriers, held, frontier);
            for (Direction direction : Direction.values()) {
                gather(level, carrier.relative(direction), carriers, held, frontier);
            }
        }
        return held;
    }

    public static Held everything(ServerLevel level, Collection<BlockPos> positions) {
        Held held = held(level, positions);
        if (positions.isEmpty()) return held;
        Set<BlockPos> moved = new HashSet<>(positions);
        moved.addAll(held.blocks());
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : moved) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        for (BlockPos pos : BlockPos.betweenClosed(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1)) {
            if (moved.contains(pos)) {
                continue;
            }
            List<Overlay> overlays = Cells.overlays(level, pos);
            if (!overlays.isEmpty()) {
                held.overlays().put(pos.immutable(), new ArrayList<>(overlays));
            }
        }
        return held;
    }

    private static void gather(ServerLevel level, BlockPos pos, Set<BlockPos> carriers, Held held, ArrayDeque<BlockPos> frontier) {
        if (carriers.contains(pos)) return;
        Cell cell = Cells.get(level, pos);
        if (cell == null || cell.isEmpty()) return;
        BlockPos key = pos.immutable();
        if (cell.offset() != null) {
            BlockState state = Cells.realState(level, key);
            if (!state.isAir() && rests(level, key, state, cell.offset(), null, carriers, held)) {
                carriers.add(key);
                held.blocks().add(key);
                held.overlays().remove(key);
                frontier.add(key);
                return;
            }
        }
        List<Overlay> taken = held.overlays().get(key);
        boolean grew = false;
        for (Overlay overlay : cell.overlays()) {
            if (taken != null && taken.contains(overlay) || !rests(level, key, overlay.getState(), overlay.getOffset(), overlay, carriers, held)) {
                continue;
            }
            if (taken == null) {
                taken = new ArrayList<>();
                held.overlays().put(key, taken);
            }
            taken.add(overlay);
            grew = true;
        }
        if (grew) {
            frontier.add(key);
        }
    }

    private static boolean rests(ServerLevel level, BlockPos pos, BlockState state, Vec3 offset, @Nullable Overlay existing, Set<BlockPos> carriers, Held held) {
        if (Overlays.isZero(offset) && Blocklogging.canBlocklog(state)) return false;
        for (Support.Contact contact : Overlays.contacts(level, pos, state, offset, existing)) {
            BlockPos cell = contact.cell();
            if (carriers.contains(cell)) return true;
            List<Overlay> taken = held.overlays().get(cell);
            if (taken != null && bears(level, cell, taken, existing, contact.location())) return true;
        }
        return false;
    }

    private static boolean bears(ServerLevel level, BlockPos cell, List<Overlay> taken, @Nullable Overlay existing, Vec3 location) {
        Vec3 local = location.subtract(cell.getX(), cell.getY(), cell.getZ());
        for (Overlay overlay : taken) {
            if (overlay == existing) {
                continue;
            }
            VoxelShape shape = OverlayContext.outside(() -> OverlayShapes.support(level, cell, overlay));
            for (AABB box : shape.toAabbs()) {
                if (box.inflate(Support.EPSILON).contains(local)) return true;
            }
        }
        return false;
    }

    public static void clear(ServerLevel level, Collection<BlockPos> positions) {
        for (BlockPos pos : positions) {
            Cell cell = Cells.get(level, pos);
            if (cell == null || cell.isEmpty()) {
                continue;
            }
            for (Overlay overlay : cell.overlays()) {
                overlay.discardBlockEntity();
            }
            Cells.update(level, pos, existing -> Cell.EMPTY);
        }
    }

    @Nullable
    private static AssembledCell assemble(ServerLevel level, BlockPos pos, Predicate<Overlay> carried, boolean whole) {
        Cell cell = Cells.get(level, pos);
        if (cell == null || cell.isEmpty()) return null;
        OffgridChunk data = Cells.data(level, pos, false);
        List<AssembledOverlay> overlays = new ArrayList<>(cell.overlays().size());
        List<Overlay> left = new ArrayList<>();
        for (Overlay overlay : cell.overlays()) {
            if (!carried.test(overlay)) {
                left.add(overlay);
                continue;
            }
            OptionalLong trigger = data == null ? OptionalLong.empty() : data.getTrigger(pos.asLong(), overlay.getId());
            BlockState state = overlay.getState();
            BlockEntity blockEntity = overlay.materialize(level, pos);
            overlays.add(new AssembledOverlay(state, overlay.getOffset(), overlay.saveBlockEntity(), trigger));
            OverlayContext.run(level, pos, overlay, () -> BlockStates.moved(state, level, pos, blockEntity));
            overlay.discardBlockEntity();
        }
        if (overlays.isEmpty() && !whole) return null;
        Cells.update(level, pos, existing -> whole ? Cell.EMPTY : Cell.of(existing.offset(), left));
        return new AssembledCell(pos.immutable(), whole ? cell.offset() : null, overlays);
    }

    public void write(OffgridChunk data, BlockPos target, Mirror mirror, Rotation rotation) {
        List<Overlay> overlays = new ArrayList<>(this.overlays.size());
        for (AssembledOverlay assembled : this.overlays) {
            overlays.add(new Overlay(data.allocateId(), assembled.state().mirror(mirror).rotate(rotation), rotate(mirror(assembled.offset(), mirror), rotation), assembled.tag()));
        }
        Vec3 offset = this.offset == null ? null : rotate(mirror(this.offset, mirror), rotation);
        data.put(target.asLong(), Cell.of(offset, overlays));
    }

    @Nullable
    public static AssembledCell copy(Level level, BlockPos pos, BlockPos origin) {
        Cell cell = Cells.get(level, pos);
        if (cell == null || cell.isEmpty()) return null;
        List<AssembledOverlay> overlays = new ArrayList<>(cell.overlays().size());
        for (Overlay overlay : cell.overlays()) {
            overlay.materialize(level, pos);
            overlays.add(new AssembledOverlay(overlay.getState(), overlay.getOffset(), overlay.saveBlockEntity(), OptionalLong.empty()));
        }
        return new AssembledCell(origin.immutable(), cell.offset(), overlays);
    }

    public List<Overlay> disassemble(ServerLevel level, BlockPos target, Rotation rotation) {
        return this.land(level, target, state -> state.rotate(rotation), offset -> rotate(offset, rotation), false);
    }

    public void place(ServerLevel level, BlockPos target, Mirror mirror, Rotation rotation) {
        this.land(level, target, state -> state.mirror(mirror).rotate(rotation), offset -> rotate(mirror(offset, mirror), rotation), true);
    }

    public List<Overlay> land(ServerLevel level, BlockPos target, UnaryOperator<BlockState> states, UnaryOperator<Vec3> offsets, boolean replace) {
        OffgridChunk data = Cells.data(level, target, true);
        if (data == null) return List.of();
        if (replace) {
            for (Overlay existing : Cells.overlays(level, target)) {
                existing.discardBlockEntity();
            }
        }
        List<Overlay> landed = new ArrayList<>(this.overlays.size());
        for (AssembledOverlay assembled : this.overlays) {
            Overlay overlay = new Overlay(data.allocateId(), states.apply(assembled.state()), offsets.apply(assembled.offset()), assembled.tag());
            overlay.materialize(level, target);
            landed.add(overlay);
        }
        Vec3 offset = this.offset == null ? null : offsets.apply(this.offset);
        Cells.update(level, target, cell -> {
            List<Overlay> merged = new ArrayList<>((replace ? 0 : cell.overlays().size()) + landed.size());
            if (!replace) {
                merged.addAll(cell.overlays());
            }
            merged.addAll(landed);
            return Cell.of(offset == null && !replace ? cell.offset() : offset, merged);
        });
        for (int i = 0; i < landed.size(); i++) {
            OptionalLong trigger = this.overlays.get(i).trigger();
            if (trigger.isPresent()) {
                OverlayTicker.schedule(level, target, landed.get(i), trigger.getAsLong());
            }
        }
        return landed;
    }

    public static Map<BlockPos, AssembledCell> assembleLocal(ServerLevel level, BlockPos origin, Collection<BlockPos> positions, Held held) {
        Map<BlockPos, AssembledCell> carried = new HashMap<>();
        for (Map.Entry<BlockPos, AssembledCell> entry : assembleHeld(level, positions, held).entrySet()) {
            carried.put(entry.getKey().subtract(origin), entry.getValue());
        }
        return carried;
    }

    public static void landAll(ServerLevel level, Map<BlockPos, AssembledCell> cells, Collection<BlockPos> moved, UnaryOperator<BlockPos> positions, UnaryOperator<BlockState> states, UnaryOperator<Vec3> offsets) {
        Set<BlockPos> targets = new LinkedHashSet<>();
        for (BlockPos pos : moved) {
            targets.add(positions.apply(pos));
        }
        for (Map.Entry<BlockPos, AssembledCell> entry : cells.entrySet()) {
            BlockPos target = positions.apply(entry.getKey());
            entry.getValue().land(level, target, states, offsets, false);
            targets.add(target);
        }
        reevaluate(level, targets);
    }

    public static void reevaluate(ServerLevel level, Collection<BlockPos> positions) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return;
        Set<BlockPos> around = new LinkedHashSet<>();
        for (BlockPos pos : positions) {
            around.add(pos.immutable());
            for (Direction direction : Direction.values()) {
                around.add(pos.relative(direction));
            }
        }
        for (BlockPos pos : around) {
            Overlays.reevaluate(level, pos);
        }
        for (BlockPos pos : around) {
            Cell cell = Cells.get(level, pos);
            if (cell != null && !cell.isEmpty()) {
                Cells.sync(level, pos);
            }
        }
    }

    //? if <26 {
    public static ListTag writeAll(Map<BlockPos, AssembledCell> cells) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, AssembledCell> entry : cells.entrySet()) {
            CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue()).result().ifPresent(encoded -> {
                CompoundTag cell = new CompoundTag();
                cell.putLong(POS_KEY, entry.getKey().asLong());
                cell.put(CELL_KEY, encoded);
                list.add(cell);
            });
        }
        return list;
    }

    public static void readAll(@Nullable Tag tag, Map<BlockPos, AssembledCell> into) {
        into.clear();
        if (!(tag instanceof ListTag list)) return;
        for (Tag entry : list) {
            if (entry instanceof CompoundTag cell) {
                BlockPos local = BlockPos.of(cell.getLong(POS_KEY));
                CODEC.parse(NbtOps.INSTANCE, cell.get(CELL_KEY)).result().ifPresent(parsed -> into.put(local, parsed));
            }
        }
    }
    //?}

    public static void write(FriendlyByteBuf buf, AssembledCell cell) {
        CompoundTag tag = new CompoundTag();
        CODEC.encodeStart(NbtOps.INSTANCE, cell).result().ifPresent(encoded -> tag.put(CELL_KEY, encoded));
        buf.writeNbt(tag);
    }

    @Nullable
    public static AssembledCell read(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return tag == null || !tag.contains(CELL_KEY) ? null : CODEC.parse(NbtOps.INSTANCE, tag.get(CELL_KEY)).result().orElse(null);
    }

    public static Vec3 mirror(Vec3 offset, Mirror mirror) {
        return switch (mirror) {
            case NONE -> offset;
            case LEFT_RIGHT -> new Vec3(offset.x, offset.y, -offset.z);
            case FRONT_BACK -> new Vec3(-offset.x, offset.y, offset.z);
        };
    }

    public static Vec3 rotate(Vec3 offset, Rotation rotation) {
        return switch (rotation) {
            case NONE -> offset;
            case CLOCKWISE_90 -> new Vec3(-offset.z, offset.y, offset.x);
            case CLOCKWISE_180 -> new Vec3(-offset.x, offset.y, -offset.z);
            case COUNTERCLOCKWISE_90 -> new Vec3(offset.z, offset.y, -offset.x);
        };
    }

    public record Held(Map<BlockPos, List<Overlay>> overlays, Set<BlockPos> blocks) {
    }

    public record AssembledOverlay(BlockState state, Vec3 offset, @Nullable CompoundTag tag, OptionalLong trigger) {

        public static final Codec<AssembledOverlay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("state").forGetter(AssembledOverlay::state),
                Vec3.CODEC.fieldOf("offset").forGetter(AssembledOverlay::offset),
                CompoundTag.CODEC.optionalFieldOf("block_entity").forGetter(overlay -> Optional.ofNullable(overlay.tag())),
                Codec.LONG.optionalFieldOf("trigger").forGetter(overlay -> overlay.trigger().isPresent() ? Optional.of(overlay.trigger().getAsLong()) : Optional.empty())
        ).apply(instance, (state, offset, tag, trigger) -> new AssembledOverlay(state, offset, tag.orElse(null), trigger.map(OptionalLong::of).orElse(OptionalLong.empty()))));
    }
}
