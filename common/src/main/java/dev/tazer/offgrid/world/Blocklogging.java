package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class Blocklogging {

    private static final double ABOVE_CELL = 2.0;
    private static final Blocklog INVALID = new Blocklog(Shapes.empty(), Shapes.empty(), Shapes.empty(), List.of());
    private static final Map<Key, Blocklog> CACHE = new ConcurrentHashMap<>();
    private static final List<Predicate<BlockState>> HOSTS = new CopyOnWriteArrayList<>();

    private Blocklogging() {
    }

    public static void addBlockloggable(Predicate<BlockState> host) {
        HOSTS.add(host);
    }

    public static boolean canBlocklog(BlockState state) {
        return state.is(OffgridTags.CAN_BLOCKLOG);
    }

    public static boolean isBlockloggable(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(OffgridTags.BLOCKLOGGABLE)) return true;
        for (Predicate<BlockState> host : HOSTS) {
            if (host.test(state)) return true;
        }
        return false;
    }

    public static boolean isFreeStanding(BlockState state, Vec3 offset) {
        return Overlays.isZero(offset) && canBlocklog(state);
    }

    public static boolean isAxial(BlockState state) {
        return canBlocklog(state) && state.hasProperty(BlockStateProperties.AXIS);
    }

    public static boolean allowsOffset(BlockState state, Vec3 offset) {
        if (Overlays.isZero(offset)) return true;
        if (!isAxial(state)) return false;
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        return Math.abs(axis.choose(offset.x, offset.y, offset.z)) <= Support.EPSILON;
    }

    public static Vec3 lateral(BlockState state, Vec3 offset) {
        if (!canBlocklog(state)) return offset;
        if (!isAxial(state)) return Vec3.ZERO;
        return switch (state.getValue(BlockStateProperties.AXIS)) {
            case X -> new Vec3(0.0, offset.y, offset.z);
            case Y -> new Vec3(offset.x, 0.0, offset.z);
            case Z -> new Vec3(offset.x, offset.y, 0.0);
        };
    }

    @Nullable
    public static Blocklog of(BlockState state, BlockState host) {
        return of(state, host, Vec3.ZERO);
    }

    @Nullable
    public static Blocklog of(BlockState state, BlockState host, Vec3 offset) {
        if (!allowsOffset(state, offset)) return null;
        if (Layers.isLayer(state)) {
            if (!Layers.isHost(host)) return null;
        } else if (!canBlocklog(state) || !isBlockloggable(host)) {
            return null;
        }
        Blocklog cut = CACHE.computeIfAbsent(new Key(state, host, offset), key -> Layers.isLayer(key.state()) ? computeLayer(key.state(), key.host()) : compute(key.state(), key.host(), key.offset()));
        return cut == INVALID ? null : cut;
    }

    @Nullable
    public static Blocklog at(BlockGetter getter, BlockPos host, BlockState state, Vec3 offset) {
        if (!canBlocklog(state) && !Layers.isLayer(state) || !allowsOffset(state, offset)) return null;
        return of(state, hostState(getter, host), offset);
    }

    @Nullable
    public static Blocklog at(BlockGetter getter, BlockPos host, Overlay overlay) {
        return at(getter, host, overlay.getState(), overlay.getOffset());
    }

    public static BlockState hostState(BlockGetter getter, BlockPos host) {
        if (getter instanceof Level level) return Covers.raw(level, host, Cells.realState(level, host));
        return OverlayContext.outside(() -> Covers.raw(getter, host, getter.getBlockState(host)));
    }

    @Nullable
    public static BlockState refit(Level level, BlockPos host, Overlay overlay, BlockState requested) {
        if (at(level, host, overlay) == null || at(level, host, requested, overlay.getOffset()) != null) return requested;
        return fit(requested, hostState(level, host), overlay.getState());
    }

    @Nullable
    public static BlockState fit(BlockState state, BlockState host, @Nullable BlockState preferred) {
        if (of(state, host) != null) return state;
        if (!Connections.isConnector(state)) return null;
        BlockState best = null;
        int bestKept = -1;
        int bestPreferred = -1;
        int bestAdded = Integer.MAX_VALUE;
        for (int connected = 0; connected < 16; connected++) {
            BlockState candidate = state;
            int kept = 0;
            int matching = 0;
            int added = 0;
            int bit = 0;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                boolean connects = (connected & (1 << bit++)) != 0;
                boolean requested = Connections.connectsToward(state, side);
                candidate = connects ? Connections.connect(candidate, side) : Connections.disconnect(candidate, side);
                if (connects == requested) {
                    kept++;
                } else if (connects) {
                    added++;
                }
                if (preferred != null && connects == Connections.connectsToward(preferred, side)) {
                    matching++;
                }
            }
            boolean better = kept > bestKept || kept == bestKept && (added < bestAdded || added == bestAdded && matching > bestPreferred);
            if (!better) {
                continue;
            }
            candidate = Connections.settlePost(candidate);
            if (of(candidate, host) == null) {
                continue;
            }
            best = candidate;
            bestKept = kept;
            bestPreferred = matching;
            bestAdded = added;
        }
        return best;
    }

    public static boolean canArm(BlockState state, BlockState host, Direction side) {
        if (!Connections.isConnector(state)) return false;
        BlockState armed = Connections.connect(state, side);
        for (int connected = 0; connected < 16; connected++) {
            BlockState candidate = armed;
            int bit = 0;
            for (Direction other : Direction.Plane.HORIZONTAL) {
                boolean connects = other == side || (connected & (1 << bit)) != 0;
                bit++;
                candidate = connects ? Connections.connect(candidate, other) : Connections.disconnect(candidate, other);
            }
            if (of(Connections.settlePost(candidate), host) != null) return true;
        }
        return false;
    }

    private static Blocklog compute(BlockState state, BlockState host, Vec3 offset) {
        VoxelShape hostShape = host.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        VoxelShape outline = moved(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()), offset);
        if (hostShape.isEmpty() || outline.isEmpty() || Block.isShapeFullBlock(hostShape) || !inside(outline.bounds())) return INVALID;
        VoxelShape cutOutline = Shapes.join(outline, hostShape, BooleanOp.ONLY_FIRST);
        if (cutOutline.isEmpty() || thins(state, outline, hostShape, offset)) return INVALID;
        VoxelShape collision = moved(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()), offset);
        VoxelShape support = moved(state.getBlockSupportShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), offset);
        List<AABB> open = new ArrayList<>();
        for (AABB box : Shapes.join(Shapes.block(), hostShape, BooleanOp.ONLY_FIRST).toAabbs()) {
            open.add(box.move(-offset.x, -offset.y, -offset.z));
        }
        return new Blocklog(cutOutline, cut(collision, capped(hostShape)), cut(support, hostShape), List.copyOf(open));
    }

    private static Blocklog computeLayer(BlockState state, BlockState host) {
        VoxelShape hostOutline = host.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        VoxelShape outline = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        if (outline.isEmpty() || Block.isShapeFullBlock(hostOutline) || outline.min(Direction.Axis.Y) > Support.EPSILON) return INVALID;
        VoxelShape cutOutline = cut(outline, hostOutline);
        if (cutOutline.isEmpty()) return INVALID;
        VoxelShape collision = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        VoxelShape support = state.getBlockSupportShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        return new Blocklog(cutOutline, collision, cut(support, hostOutline), new Layers.HostOutline(hostOutline.toAabbs()));
    }

    private static VoxelShape capped(VoxelShape host) {
        VoxelShape capped = host;
        for (AABB box : host.toAabbs()) {
            if (box.maxY >= 1.0 - Support.EPSILON) {
                capped = Shapes.or(capped, Shapes.box(box.minX, 1.0, box.minZ, box.maxX, ABOVE_CELL, box.maxZ));
            }
        }
        return capped;
    }

    private static VoxelShape moved(VoxelShape shape, Vec3 offset) {
        return shape.isEmpty() || Overlays.isZero(offset) ? shape : shape.move(offset.x, offset.y, offset.z);
    }

    private static boolean inside(AABB bounds) {
        return bounds.minX >= -Support.EPSILON && bounds.minY >= -Support.EPSILON && bounds.minZ >= -Support.EPSILON && bounds.maxX <= 1.0 + Support.EPSILON && bounds.maxY <= 1.0 + Support.EPSILON && bounds.maxZ <= 1.0 + Support.EPSILON;
    }

    private static VoxelShape cut(VoxelShape shape, VoxelShape host) {
        return shape.isEmpty() ? shape : Shapes.join(shape, host, BooleanOp.ONLY_FIRST);
    }

    private static boolean thins(BlockState state, VoxelShape outline, VoxelShape host, Vec3 offset) {
        if (thins(outline, host, Blocklogging::spans)) return true;
        if (!Connections.isConnector(state)) return false;
        BlockState bare = Connections.bare(state);
        VoxelShape post = Shapes.join(moved(bare.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()), offset), outline, BooleanOp.AND);
        return thins(post, host, (box, axis) -> axis == Direction.Axis.Y ? spans(box, axis) : Connections.continuesThrough(state, axis));
    }

    public static List<AABB> maximalBoxes(VoxelShape shape) {
        List<AABB> boxes = shape.toAabbs();
        double[] xs = coordinates(boxes, Direction.Axis.X);
        double[] ys = coordinates(boxes, Direction.Axis.Y);
        double[] zs = coordinates(boxes, Direction.Axis.Z);
        List<AABB> inside = new ArrayList<>();
        for (int x0 = 0; x0 < xs.length; x0++) {
            for (int x1 = x0 + 1; x1 < xs.length; x1++) {
                for (int y0 = 0; y0 < ys.length; y0++) {
                    for (int y1 = y0 + 1; y1 < ys.length; y1++) {
                        for (int z0 = 0; z0 < zs.length; z0++) {
                            for (int z1 = z0 + 1; z1 < zs.length; z1++) {
                                AABB box = new AABB(xs[x0], ys[y0], zs[z0], xs[x1], ys[y1], zs[z1]);
                                if (!Shapes.joinIsNotEmpty(Shapes.create(box), shape, BooleanOp.ONLY_FIRST)) {
                                    inside.add(box);
                                }
                            }
                        }
                    }
                }
            }
        }
        List<AABB> maximal = new ArrayList<>();
        for (AABB box : inside) {
            boolean contained = false;
            for (AABB other : inside) {
                if (other != box && contains(other, box)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                maximal.add(box);
            }
        }
        return maximal;
    }

    private static double[] coordinates(List<AABB> boxes, Direction.Axis axis) {
        TreeSet<Double> values = new TreeSet<>();
        for (AABB box : boxes) {
            values.add(box.min(axis));
            values.add(box.max(axis));
        }
        double[] sorted = new double[values.size()];
        int index = 0;
        for (double value : values) {
            sorted[index++] = value;
        }
        return sorted;
    }

    private static boolean contains(AABB outer, AABB inner) {
        return outer.minX <= inner.minX && outer.minY <= inner.minY && outer.minZ <= inner.minZ && outer.maxX >= inner.maxX && outer.maxY >= inner.maxY && outer.maxZ >= inner.maxZ && !outer.equals(inner);
    }

    private static boolean thins(VoxelShape shape, VoxelShape host, BiPredicate<AABB, Direction.Axis> lengthwise) {
        for (AABB box : maximalBoxes(shape)) {
            VoxelShape rest = Shapes.join(Shapes.create(box), host, BooleanOp.ONLY_FIRST);
            for (AABB piece : rest.toAabbs()) {
                for (Direction.Axis axis : Direction.Axis.values()) {
                    if (lengthwise.test(box, axis)) {
                        continue;
                    }
                    if (Math.abs(piece.min(axis) - box.min(axis)) > Support.EPSILON || Math.abs(piece.max(axis) - box.max(axis)) > Support.EPSILON) return true;
                }
            }
        }
        return false;
    }

    private static boolean spans(AABB box, Direction.Axis axis) {
        return box.min(axis) <= Support.EPSILON || box.max(axis) >= 1.0 - Support.EPSILON;
    }

    public record Blocklog(VoxelShape outline, VoxelShape collision, VoxelShape support, List<AABB> open) {
    }

    private record Key(BlockState state, BlockState host, Vec3 offset) {
    }
}
