package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Surfaces {


    private Surfaces() {
    }

    public static VoxelShape shapeAt(Level level, BlockPos pos) {
        return shapeAt(level, pos, null, false);
    }

    public static Surface surfaceAt(Level level, BlockPos pos, @Nullable Overlay ignore, boolean ignoreReal) {
        BlockState state = Cells.realState(level, pos);
        VoxelShape outline = Shapes.empty();
        VoxelShape support = Shapes.empty();
        List<Plate> plates = new ArrayList<>();
        if (!ignoreReal) {
            outline = state.getShape(level, pos, CollisionContext.empty());
            if (!state.canBeReplaced()) {
                support = state.getBlockSupportShape(level, pos);
            }
            if (isPlate(state) && !outline.isEmpty()) {
                plates.add(new Plate(outline, Support.direction(state).getOpposite()));
            }
        }
        for (Overlay overlay : Cells.overlays(level, pos)) {
            if (overlay == ignore) {
                continue;
            }
            VoxelShape overlayShape = OverlayShapes.of(level, pos, overlay);
            if (!overlayShape.isEmpty()) {
                outline = Shapes.or(outline, overlayShape);
                if (isPlate(overlay.getState())) {
                    plates.add(new Plate(overlayShape, Support.direction(overlay.getState()).getOpposite()));
                }
            }
            VoxelShape overlaySupport = OverlayShapes.support(level, pos, overlay);
            if (!overlaySupport.isEmpty()) {
                support = Shapes.or(support, overlaySupport);
            }
        }
        return new Surface(outline, support, plates);
    }

    private static boolean isPlate(BlockState state) {
        return state.is(BlockTags.BUTTONS) || state.is(BlockTags.PRESSURE_PLATES);
    }

    public static VoxelShape shapeAt(Level level, BlockPos pos, @Nullable Overlay ignore, boolean ignoreReal) {
        BlockState state = Cells.realState(level, pos);
        VoxelShape shape = ignoreReal ? Shapes.empty() : state.getShape(level, pos, CollisionContext.empty());
        for (Overlay overlay : Cells.overlays(level, pos)) {
            if (overlay == ignore) {
                continue;
            }
            VoxelShape overlayShape = OverlayShapes.of(level, pos, overlay);
            if (!overlayShape.isEmpty()) {
                shape = Shapes.or(shape, overlayShape);
            }
        }
        return shape;
    }

    @Nullable
    public static Vec3 offsetFor(Level level, BlockPos clicked, Direction face, Vec3 location, BlockState placed) {
        Vec3 hit = location.subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        Surface surface = surfaceAt(level, clicked, null, false);
        VoxelShape shape = surface.outline();
        AABB box = boxAt(shape, face, hit);
        if (box == null) return null;
        box = clipObstructed(shape, box, face);
        Direction.Axis axis = face.getAxis();
        double depth = depth(box, face);
        if (Block.isShapeFullBlock(shape)) return along(axis, depth);
        double plane = plane(clicked, box, face);
        BlockPos placedPos = clicked.relative(face);
        AABB footprint = footprint(placed);
        List<Vec3> options = candidates(box, axis, depth);
        Vec3 core = core(shape, face, hit, depth);
        if (core != null && backed(level, clicked, surface, placedPos, footprint, core, axis, plane, face) && fits(level, placedPos, placed, core)) return core;
        addCore(options, shape, face, hit, depth);
        for (Vec3 candidate : options) {
            if (supportedAt(box, axis, footprint, candidate) && backed(level, clicked, surface, placedPos, footprint, candidate, axis, plane, face) && fits(level, placedPos, placed, candidate)) return candidate;
        }
        for (Vec3 candidate : options) {
            if (centredOn(box, axis, footprint, candidate) && backed(level, clicked, surface, placedPos, footprint, candidate, axis, plane, face) && fits(level, placedPos, placed, candidate)) return candidate;
        }
        return null;
    }

    public static Vec3 mountOffsetFor(Level level, BlockPos clicked, Direction face, Vec3 location, Vec3 centre, boolean vanillaFits) {
        Vec3 hit = location.subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        Surface surface = surfaceAt(level, clicked, null, false);
        VoxelShape shape = surface.outline();
        AABB box = boxAt(shape, face, hit);
        if (box == null) return Vec3.ZERO;
        box = clipObstructed(shape, box, face);
        Direction.Axis axis = face.getAxis();
        double depth = depth(box, face);
        if (depth == 0.0 && vanillaFits) return Vec3.ZERO;
        double plane = plane(clicked, box, face);
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        Vec3 grid = compose(axis, depth, first, 0.0, second, 0.0);
        Vec3 centred = compose(axis, depth, first, snap(first.choose(clicked.getX(), clicked.getY(), clicked.getZ()) + (box.min(first) + box.max(first)) * 0.5 - first.choose(centre.x, centre.y, centre.z)), second, snap(second.choose(clicked.getX(), clicked.getY(), clicked.getZ()) + (box.min(second) + box.max(second)) * 0.5 - second.choose(centre.x, centre.y, centre.z)));
        for (Vec3 candidate : List.of(grid, centred)) {
            Vec3 point = onPlane(centre.x + candidate.x, centre.y + candidate.y, centre.z + candidate.z, axis, plane);
            if (supports(level, clicked, surface, point, face)) return candidate;
        }
        return Vec3.ZERO;
    }

    public static List<Vec3> hangingOffsetsFor(Level level, BlockPos clicked, Direction face, Vec3 location, AABB hanging, boolean whole) {
        Vec3 hit = location.subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        VoxelShape outline = surfaceAt(level, clicked, null, false).outline();
        AABB box = boxAt(outline, face, hit);
        if (box == null) return List.of();
        box = clipObstructed(outline, box, face);
        List<Vec3> offsets = new ArrayList<>();
        addHangingOffsets(level, new HashMap<>(), offsets, clicked, face, location, box, hanging, whole);
        AABB front = frontBox(outline, face, hit, box);
        if (front != null) {
            addHangingOffsets(level, new HashMap<>(), offsets, clicked, face, location, front, hanging, whole);
        }
        return offsets;
    }

    private static void addHangingOffsets(Level level, Map<BlockPos, VoxelShape> regions, List<Vec3> offsets, BlockPos clicked, Direction face, Vec3 location, AABB box, AABB hanging, boolean whole) {
        Direction.Axis axis = face.getAxis();
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        int step = face.getAxisDirection().getStep();
        double plane = plane(clicked, box, face);
        double along = snap(plane - (step > 0 ? hanging.min(axis) : hanging.max(axis)));
        List<Double> firstShifts;
        List<Double> secondShifts;
        if (whole) {
            firstShifts = new ArrayList<>();
            secondShifts = new ArrayList<>();
            firstShifts.add(0.0);
            secondShifts.add(0.0);
            double firstSize = hanging.max(first) - hanging.min(first);
            double secondSize = hanging.max(second) - hanging.min(second);
            int cellAxis = (int) Math.floor(plane - step * Support.EPSILON);
            for (int firstCell = (int) Math.floor(hanging.min(first) - firstSize); firstCell <= (int) Math.floor(hanging.max(first) + firstSize); firstCell++) {
                for (int secondCell = (int) Math.floor(hanging.min(second) - secondSize); secondCell <= (int) Math.floor(hanging.max(second) + secondSize); secondCell++) {
                    BlockPos cell = cellAt(axis, cellAxis, first, firstCell, second, secondCell);
                    if (!level.hasChunkAt(cell)) {
                        continue;
                    }
                    for (AABB region : regionAt(level, regions, cell, face, plane).toAabbs()) {
                        addShift(firstShifts, firstCell + region.minX - hanging.min(first), firstSize);
                        addShift(firstShifts, firstCell + region.maxX - hanging.max(first), firstSize);
                        addShift(secondShifts, secondCell + region.minZ - hanging.min(second), secondSize);
                        addShift(secondShifts, secondCell + region.maxZ - hanging.max(second), secondSize);
                    }
                }
            }
        } else {
            double firstBase = first.choose(clicked.getX(), clicked.getY(), clicked.getZ());
            double secondBase = second.choose(clicked.getX(), clicked.getY(), clicked.getZ());
            firstShifts = frameShifts(hanging, first, firstBase + box.min(first), firstBase + box.max(first));
            secondShifts = frameShifts(hanging, second, secondBase + box.min(second), secondBase + box.max(second));
        }
        List<int[]> pairs = new ArrayList<>(firstShifts.size() * secondShifts.size());
        for (int firstIndex = 0; firstIndex < firstShifts.size(); firstIndex++) {
            for (int secondIndex = 0; secondIndex < secondShifts.size(); secondIndex++) {
                pairs.add(new int[]{firstIndex, secondIndex});
            }
        }
        if (whole) {
            pairs.sort(Comparator.comparingDouble((int[] pair) -> Math.abs(firstShifts.get(pair[0])) + Math.abs(secondShifts.get(pair[1]))).thenComparingDouble(pair -> firstShifts.get(pair[0])).thenComparingDouble(pair -> secondShifts.get(pair[1])));
        } else {
            pairs.sort(Comparator.comparingInt((int[] pair) -> pair[0] + pair[1]).thenComparingDouble(pair -> Math.abs(firstShifts.get(pair[0])) + Math.abs(secondShifts.get(pair[1]))));
        }
        for (int[] pair : pairs) {
            Vec3 option = compose(axis, along, first, firstShifts.get(pair[0]), second, secondShifts.get(pair[1]));
            if (offsets.contains(option)) {
                continue;
            }
            if (whole) {
                AABB moved = hanging.move(option);
                if (!holds(moved, location, first, second) || !covers(level, regions, face, plane, moved)) {
                    continue;
                }
            }
            offsets.add(option);
        }
    }

    private static List<Double> frameShifts(AABB hanging, Direction.Axis lateral, double min, double max) {
        double low = hanging.min(lateral);
        double high = hanging.max(lateral);
        List<Double> shifts = new ArrayList<>(4);
        if (low >= min - Support.EPSILON && high <= max + Support.EPSILON) {
            shifts.add(0.0);
        }
        addFrameShift(shifts, snap((min + max - low - high) * 0.5));
        double toMin = snap(min - low);
        double toMax = snap(max - high);
        addFrameShift(shifts, Math.abs(toMin) <= Math.abs(toMax) ? toMin : toMax);
        addFrameShift(shifts, Math.abs(toMin) <= Math.abs(toMax) ? toMax : toMin);
        addFrameShift(shifts, 0.0);
        return shifts;
    }

    private static void addFrameShift(List<Double> shifts, double shift) {
        if (!shifts.contains(shift)) {
            shifts.add(shift);
        }
    }

    private static boolean holds(AABB box, Vec3 location, Direction.Axis first, Direction.Axis second) {
        double firstValue = first.choose(location.x, location.y, location.z);
        double secondValue = second.choose(location.x, location.y, location.z);
        return firstValue >= box.min(first) - Support.EPSILON && firstValue <= box.max(first) + Support.EPSILON && secondValue >= box.min(second) - Support.EPSILON && secondValue <= box.max(second) + Support.EPSILON;
    }

    @Nullable
    private static AABB frontBox(VoxelShape outline, Direction face, Vec3 hit, AABB clicked) {
        Direction.Axis axis = face.getAxis();
        int step = face.getAxisDirection().getStep();
        double current = step > 0 ? clicked.max(axis) : clicked.min(axis);
        AABB nearest = null;
        double front = current;
        double distance = Double.MAX_VALUE;
        for (AABB box : outline.toAabbs()) {
            double value = step > 0 ? box.max(axis) : box.min(axis);
            double gap = lateralDistance(box, axis, hit);
            boolean further = step > 0 ? value > front + Support.EPSILON : value < front - Support.EPSILON;
            boolean tied = Math.abs(value - front) <= Support.EPSILON && Math.abs(front - current) > Support.EPSILON;
            if (further || tied && gap < distance) {
                front = value;
                nearest = box;
                distance = gap;
            }
        }
        if (nearest == null) return null;
        Vec3 centre = onPlane(nearest.getCenter().x, nearest.getCenter().y, nearest.getCenter().z, axis, front);
        AABB merged = boxAt(outline, face, centre);
        return merged == null ? null : clipObstructed(outline, merged, face);
    }

    private static double lateralDistance(AABB box, Direction.Axis axis, Vec3 hit) {
        double distance = 0.0;
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other == axis) {
                continue;
            }
            double value = other.choose(hit.x, hit.y, hit.z);
            distance += Math.max(0.0, Math.max(box.min(other) - value, value - box.max(other)));
        }
        return distance;
    }

    public static boolean touches(Level level, Direction face, AABB box) {
        int step = face.getAxisDirection().getStep();
        double plane = step > 0 ? box.min(face.getAxis()) : box.max(face.getAxis());
        return touches(level, new HashMap<>(), face, plane, box);
    }

    private static boolean touches(Level level, Map<BlockPos, VoxelShape> regions, Direction face, double plane, AABB box) {
        Direction.Axis axis = face.getAxis();
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        int cellAxis = (int) Math.floor(plane - face.getAxisDirection().getStep() * Support.EPSILON);
        for (int firstCell = (int) Math.floor(box.min(first) + Support.EPSILON); firstCell <= (int) Math.floor(box.max(first) - Support.EPSILON); firstCell++) {
            for (int secondCell = (int) Math.floor(box.min(second) + Support.EPSILON); secondCell <= (int) Math.floor(box.max(second) - Support.EPSILON); secondCell++) {
                BlockPos cell = cellAt(axis, cellAxis, first, firstCell, second, secondCell);
                if (!level.hasChunkAt(cell)) {
                    continue;
                }
                double minFirst = Math.max(box.min(first), firstCell) - firstCell + Support.EPSILON;
                double maxFirst = Math.min(box.max(first), firstCell + 1) - firstCell - Support.EPSILON;
                double minSecond = Math.max(box.min(second), secondCell) - secondCell + Support.EPSILON;
                double maxSecond = Math.min(box.max(second), secondCell + 1) - secondCell - Support.EPSILON;
                if (maxFirst <= minFirst || maxSecond <= minSecond) {
                    continue;
                }
                VoxelShape area = Shapes.box(minFirst, 0.0, minSecond, maxFirst, 1.0, maxSecond);
                if (Shapes.joinIsNotEmpty(area, regionAt(level, regions, cell, face, plane), BooleanOp.AND)) return true;
            }
        }
        return false;
    }

    public static boolean covers(Level level, Direction face, AABB box) {
        int step = face.getAxisDirection().getStep();
        double plane = step > 0 ? box.min(face.getAxis()) : box.max(face.getAxis());
        return covers(level, new HashMap<>(), face, plane, box);
    }

    private static boolean covers(Level level, Map<BlockPos, VoxelShape> regions, Direction face, double plane, AABB box) {
        Direction.Axis axis = face.getAxis();
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        int cellAxis = (int) Math.floor(plane - face.getAxisDirection().getStep() * Support.EPSILON);
        for (int firstCell = (int) Math.floor(box.min(first) + Support.EPSILON); firstCell <= (int) Math.floor(box.max(first) - Support.EPSILON); firstCell++) {
            for (int secondCell = (int) Math.floor(box.min(second) + Support.EPSILON); secondCell <= (int) Math.floor(box.max(second) - Support.EPSILON); secondCell++) {
                BlockPos cell = cellAt(axis, cellAxis, first, firstCell, second, secondCell);
                if (!level.hasChunkAt(cell)) return false;
                double minFirst = Math.max(box.min(first), firstCell) - firstCell + Support.EPSILON;
                double maxFirst = Math.min(box.max(first), firstCell + 1) - firstCell - Support.EPSILON;
                double minSecond = Math.max(box.min(second), secondCell) - secondCell + Support.EPSILON;
                double maxSecond = Math.min(box.max(second), secondCell + 1) - secondCell - Support.EPSILON;
                if (maxFirst <= minFirst || maxSecond <= minSecond) {
                    continue;
                }
                VoxelShape needed = Shapes.box(minFirst, 0.0, minSecond, maxFirst, 1.0, maxSecond);
                if (Shapes.joinIsNotEmpty(needed, regionAt(level, regions, cell, face, plane), BooleanOp.ONLY_FIRST)) return false;
            }
        }
        return true;
    }

    private static VoxelShape regionAt(Level level, Map<BlockPos, VoxelShape> regions, BlockPos cell, Direction face, double plane) {
        VoxelShape cached = regions.get(cell);
        if (cached != null) return cached;
        Direction.Axis axis = face.getAxis();
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        int step = face.getAxisDirection().getStep();
        double at = plane - axis.choose(cell.getX(), cell.getY(), cell.getZ());
        Surface surface = surfaceAt(level, cell, null, false);
        VoxelShape faced = Shapes.empty();
        for (AABB box : surface.outline().toAabbs()) {
            if (Math.abs((step > 0 ? box.max(axis) : box.min(axis)) - at) <= Support.EPSILON) {
                faced = Shapes.or(faced, flat(box, first, second));
            }
        }
        VoxelShape region = faced;
        if (!faced.isEmpty()) {
            double inside = at - step * Support.EPSILON * 2.0;
            VoxelShape held = Shapes.empty();
            for (AABB box : surface.support().toAabbs()) {
                if (box.min(axis) <= inside && inside <= box.max(axis)) {
                    held = Shapes.or(held, flat(box, first, second));
                }
            }
            for (Plate plate : surface.plates()) {
                if (plate.face() != face) {
                    continue;
                }
                for (AABB box : plate.shape().toAabbs()) {
                    if (box.min(axis) <= inside && inside <= box.max(axis)) {
                        held = Shapes.or(held, flat(box, first, second));
                    }
                }
            }
            region = Shapes.join(faced, held, BooleanOp.AND);
        }
        regions.put(cell, region);
        return region;
    }

    private static void addShift(List<Double> shifts, double shift, double size) {
        double snapped = snap(shift);
        if (Math.abs(snapped) <= size + Support.EPSILON && !shifts.contains(snapped)) {
            shifts.add(snapped);
        }
    }

    private static BlockPos cellAt(Direction.Axis axis, int axisValue, Direction.Axis first, int firstValue, Direction.Axis second, int secondValue) {
        int[] values = new int[3];
        values[axis.ordinal()] = axisValue;
        values[first.ordinal()] = firstValue;
        values[second.ordinal()] = secondValue;
        return new BlockPos(values[0], values[1], values[2]);
    }

    public static List<Vec3> candidatesFor(Level level, BlockPos clicked, Direction face, Vec3 location, BlockState placed, Surface surface) {
        Vec3 hit = location.subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        VoxelShape shape = surface.outline();
        AABB box = boxAt(shape, face, hit);
        if (box == null) return List.of();
        box = clipObstructed(shape, box, face);
        Direction.Axis axis = face.getAxis();
        double depth = depth(box, face);
        if (Block.isShapeFullBlock(shape)) return List.of(along(axis, depth));
        double plane = plane(clicked, box, face);
        BlockPos placedPos = clicked.relative(face);
        AABB footprint = footprint(placed);
        List<Vec3> accepted = new ArrayList<>(3);
        List<Vec3> options = candidates(box, axis, depth);
        Vec3 core = core(shape, face, hit, depth);
        addCore(options, shape, face, hit, depth);
        for (Vec3 candidate : options) {
            if ((supportedAt(box, axis, footprint, candidate) || centredOn(box, axis, footprint, candidate) || candidate.equals(core)) && backed(level, clicked, surface, placedPos, footprint, candidate, axis, plane, face)) {
                accepted.add(candidate);
            }
        }
        return accepted;
    }

    @Nullable
    public static Vec3 restingFor(Level level, BlockPos clicked, Direction face, Vec3 location, BlockState placed, Surface surface, Vec3 offset) {
        Vec3 hit = location.subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        VoxelShape shape = surface.outline();
        if (Block.isShapeFullBlock(shape)) return null;
        AABB box = boxAt(shape, face, hit);
        if (box == null) return null;
        Direction.Axis axis = face.getAxis();
        Vec3 candidate = switch (axis) {
            case X -> new Vec3(depth(box, face), offset.y, offset.z);
            case Y -> new Vec3(offset.x, depth(box, face), offset.z);
            case Z -> new Vec3(offset.x, offset.y, depth(box, face));
        };
        AABB footprint = footprint(placed);
        if (!restsOn(box, axis, footprint, candidate)) return null;
        return backed(level, clicked, surface, clicked.relative(face), footprint, candidate, axis, plane(clicked, box, face), face) ? candidate : null;
    }

    private static boolean restsOn(AABB box, Direction.Axis axis, AABB footprint, Vec3 candidate) {
        for (Direction.Axis lateral : Direction.Axis.values()) {
            if (lateral == axis) {
                continue;
            }
            double shift = lateral.choose(candidate.x, candidate.y, candidate.z);
            boolean within = footprint.min(lateral) + shift >= box.min(lateral) - Support.EPSILON && footprint.max(lateral) + shift <= box.max(lateral) + Support.EPSILON;
            boolean centred = Math.abs((footprint.min(lateral) + footprint.max(lateral)) / 2.0 + shift - (box.min(lateral) + box.max(lateral)) / 2.0) <= Support.EPSILON;
            if (!within && !centred) return false;
        }
        return true;
    }

    private static boolean backed(Level level, BlockPos clicked, Surface surface, BlockPos placedPos, AABB footprint, Vec3 candidate, Direction.Axis axis, double plane, Direction face) {
        Vec3 centre = footprint.getCenter();
        Vec3 point = onPlane(placedPos.getX() + centre.x + candidate.x, placedPos.getY() + centre.y + candidate.y, placedPos.getZ() + centre.z + candidate.z, axis, plane);
        return supports(level, clicked, surface, point, face);
    }

    @Nullable
    public static Vec3 centreOn(Level level, BlockPos clicked, Direction face, Vec3 location, BlockState placed) {
        Vec3 hit = location.subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        VoxelShape shape = shapeAt(level, clicked);
        AABB box = boxAt(shape, face, hit);
        if (box == null) return null;
        box = clipObstructed(shape, box, face);
        Direction.Axis axis = face.getAxis();
        if (Block.isShapeFullBlock(shape)) return Vec3.ZERO;
        AABB footprint = footprint(placed);
        List<Vec3> options = candidates(box, axis, 0.0);
        for (Vec3 candidate : options) {
            if (supportedAt(box, axis, footprint, candidate)) return candidate;
        }
        for (Vec3 candidate : options) {
            if (centredOn(box, axis, footprint, candidate)) return candidate;
        }
        return null;
    }

    public static List<Vec3> centresOn(Level level, BlockPos clicked, Direction face, Vec3 location, BlockState placed) {
        List<Vec3> centres = new ArrayList<>(2);
        Vec3 centred = centreOn(level, clicked, face, location, placed);
        centres.add(centred == null ? Vec3.ZERO : centred);
        addCore(centres, shapeAt(level, clicked), face, location.subtract(clicked.getX(), clicked.getY(), clicked.getZ()), 0.0);
        return centres;
    }

    public static boolean fits(Level level, BlockPos pos, BlockState state, Vec3 offset) {
        return !Overlays.collides(level, pos, OverlayShapes.of(state, offset), null, false);
    }

    private static List<Vec3> candidates(AABB box, Direction.Axis axis, double depth) {
        Direction.Axis first = null;
        Direction.Axis second = null;
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other == axis) {
                continue;
            }
            if (first == null) {
                first = other;
            } else {
                second = other;
            }
        }
        List<Vec3> options = new ArrayList<>(2);
        options.add(compose(axis, depth, first, 0.0, second, 0.0));
        options.add(compose(axis, depth, first, snap(midpoint(box, first)), second, snap(midpoint(box, second))));
        return options;
    }

    private static void addCore(List<Vec3> options, VoxelShape shape, Direction face, Vec3 hit, double depth) {
        Vec3 core = core(shape, face, hit, depth);
        if (core != null && !options.contains(core)) {
            options.add(core);
        }
    }

    @Nullable
    private static Vec3 core(VoxelShape shape, Direction face, Vec3 hit, double depth) {
        AABB merged = boxAt(shape, face, hit);
        if (merged == null) return null;
        Direction.Axis axis = face.getAxis();
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        int step = face.getAxisDirection().getStep();
        double surface = step > 0 ? merged.max(axis) : merged.min(axis);
        VoxelShape region = Shapes.empty();
        VoxelShape cover = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            double near = step > 0 ? box.max(axis) : box.min(axis);
            double far = step > 0 ? box.min(axis) : box.max(axis);
            if (Math.abs(near - surface) <= Support.EPSILON && box.min(first) >= merged.min(first) - Support.EPSILON && box.max(first) <= merged.max(first) + Support.EPSILON && box.min(second) >= merged.min(second) - Support.EPSILON && box.max(second) <= merged.max(second) + Support.EPSILON) {
                region = Shapes.or(region, flat(box, first, second));
            } else if (Math.abs(far - surface) <= Support.EPSILON) {
                cover = Shapes.or(cover, flat(box, first, second));
            }
        }
        region = Shapes.join(region, cover, BooleanOp.ONLY_FIRST);
        if (region.isEmpty()) return null;
        List<AABB> rectangles = Blocklogging.maximalBoxes(region);
        if (rectangles.size() < 2) return null;
        double minFirst = 0.0;
        double maxFirst = 1.0;
        double minSecond = 0.0;
        double maxSecond = 1.0;
        for (AABB rectangle : rectangles) {
            minFirst = Math.max(minFirst, rectangle.minX);
            maxFirst = Math.min(maxFirst, rectangle.maxX);
            minSecond = Math.max(minSecond, rectangle.minZ);
            maxSecond = Math.min(maxSecond, rectangle.maxZ);
        }
        if (maxFirst - minFirst <= Support.EPSILON || maxSecond - minSecond <= Support.EPSILON) return null;
        return compose(axis, depth, first, snap((minFirst + maxFirst) * 0.5 - 0.5), second, snap((minSecond + maxSecond) * 0.5 - 0.5));
    }

    private static VoxelShape flat(AABB box, Direction.Axis first, Direction.Axis second) {
        return Shapes.box(box.min(first), 0.0, box.min(second), box.max(first), 1.0, box.max(second));
    }

    private static Vec3 compose(Direction.Axis axis, double depth, Direction.Axis first, double firstValue, Direction.Axis second, double secondValue) {
        double[] values = new double[3];
        values[axis.ordinal()] = depth;
        values[first.ordinal()] = firstValue;
        values[second.ordinal()] = secondValue;
        return new Vec3(values[0], values[1], values[2]);
    }

    private static Vec3 along(Direction.Axis axis, double value) {
        return compose(axis, value, axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X, 0.0, axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z, 0.0);
    }

    private static boolean centredOn(AABB box, Direction.Axis axis, AABB footprint, Vec3 candidate) {
        for (Direction.Axis lateral : Direction.Axis.values()) {
            if (lateral == axis) {
                continue;
            }
            double shift = lateral.choose(candidate.x, candidate.y, candidate.z);
            double placed = (footprint.min(lateral) + footprint.max(lateral)) / 2.0 + shift;
            double surface = (box.min(lateral) + box.max(lateral)) / 2.0;
            if (Math.abs(placed - surface) > Support.EPSILON) return false;
        }
        return true;
    }

    private static boolean supportedAt(AABB box, Direction.Axis axis, AABB footprint, Vec3 candidate) {
        for (Direction.Axis lateral : Direction.Axis.values()) {
            if (lateral == axis) {
                continue;
            }
            double shift = lateral.choose(candidate.x, candidate.y, candidate.z);
            if (footprint.min(lateral) + shift < box.min(lateral) - Support.EPSILON || footprint.max(lateral) + shift > box.max(lateral) + Support.EPSILON) return false;
        }
        return true;
    }

    private static double midpoint(AABB box, Direction.Axis lateral) {
        return (box.min(lateral) + box.max(lateral)) * 0.5 - 0.5;
    }

    private static double depth(AABB box, Direction face) {
        Direction.Axis axis = face.getAxis();
        int step = face.getAxisDirection().getStep();
        return snap((step > 0 ? box.max(axis) : box.min(axis)) - (step > 0 ? 1.0 : 0.0));
    }

    public static boolean supports(Level level, Vec3 point, Direction face) {
        return supports(level, null, null, point, face);
    }

    private static boolean supports(Level level, @Nullable BlockPos known, @Nullable Surface knownSurface, Vec3 point, Direction face) {
        Direction.Axis axis = face.getAxis();
        Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        int[] base = new int[3];
        base[axis.ordinal()] = (int) Math.floor(axis.choose(point.x, point.y, point.z) - face.getAxisDirection().getStep() * Support.EPSILON);
        for (int firstCell : spanned(first.choose(point.x, point.y, point.z))) {
            for (int secondCell : spanned(second.choose(point.x, point.y, point.z))) {
                base[first.ordinal()] = firstCell;
                base[second.ordinal()] = secondCell;
                BlockPos cell = new BlockPos(base[0], base[1], base[2]);
                if (!level.hasChunkAt(cell)) {
                    continue;
                }
                Surface surface = knownSurface != null && cell.equals(known) ? knownSurface : surfaceAt(level, cell, null, false);
                if (backs(surface, point.subtract(cell.getX(), cell.getY(), cell.getZ()), face)) return true;
            }
        }
        return false;
    }

    private static int[] spanned(double value) {
        int floor = (int) Math.floor(value);
        double fraction = value - floor;
        if (fraction <= Support.EPSILON) return new int[]{floor, floor - 1};
        if (fraction >= 1.0 - Support.EPSILON) return new int[]{floor, floor + 1};
        return new int[]{floor};
    }

    private static boolean backs(Surface surface, Vec3 local, Direction face) {
        Direction.Axis axis = face.getAxis();
        int step = face.getAxisDirection().getStep();
        double at = axis.choose(local.x, local.y, local.z);
        boolean faced = false;
        for (AABB box : surface.outline().toAabbs()) {
            double value = step > 0 ? box.max(axis) : box.min(axis);
            if (Math.abs(value - at) <= Support.EPSILON && within(box, axis, local)) {
                faced = true;
                break;
            }
        }
        if (!faced) return false;
        double inside = at - step * Support.EPSILON * 2.0;
        if (contains(surface.support(), axis, inside, local)) return true;
        for (Plate plate : surface.plates()) {
            if (plate.face() == face && contains(plate.shape(), axis, inside, local)) return true;
        }
        return false;
    }

    private static boolean contains(VoxelShape shape, Direction.Axis axis, double inside, Vec3 local) {
        for (AABB box : shape.toAabbs()) {
            if (inside > box.min(axis) && inside < box.max(axis) && within(box, axis, local)) return true;
        }
        return false;
    }

    private static double plane(BlockPos clicked, AABB box, Direction face) {
        Direction.Axis axis = face.getAxis();
        return axis.choose(clicked.getX(), clicked.getY(), clicked.getZ()) + (face.getAxisDirection().getStep() > 0 ? box.max(axis) : box.min(axis));
    }

    private static Vec3 onPlane(double x, double y, double z, Direction.Axis axis, double plane) {
        return switch (axis) {
            case X -> new Vec3(plane, y, z);
            case Y -> new Vec3(x, plane, z);
            case Z -> new Vec3(x, y, plane);
        };
    }

    private static AABB footprint(BlockState state) {
        VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        return shape.isEmpty() ? new AABB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75) : shape.bounds();
    }

    private static AABB clipObstructed(VoxelShape shape, AABB box, Direction face) {
        Direction.Axis axis = face.getAxis();
        int step = face.getAxisDirection().getStep();
        double surface = step > 0 ? box.max(axis) : box.min(axis);
        AABB clipped = box;
        for (AABB other : shape.toAabbs()) {
            boolean beyond = step > 0 ? other.min(axis) >= surface - Support.EPSILON : other.max(axis) <= surface + Support.EPSILON;
            double ownSurface = step > 0 ? other.max(axis) : other.min(axis);
            if (!beyond || Math.abs(ownSurface - surface) <= Support.EPSILON) {
                continue;
            }
            for (Direction.Axis lateral : Direction.Axis.values()) {
                if (lateral == axis) {
                    continue;
                }
                Direction.Axis across = remaining(axis, lateral);
                if (other.min(across) > clipped.min(across) + Support.EPSILON || other.max(across) < clipped.max(across) - Support.EPSILON) {
                    continue;
                }
                clipped = clipInterval(clipped, lateral, other.min(lateral), other.max(lateral));
            }
        }
        return clipped;
    }

    private static Direction.Axis remaining(Direction.Axis first, Direction.Axis second) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis != first && axis != second) return axis;
        }
        return second;
    }

    private static AABB clipInterval(AABB box, Direction.Axis axis, double from, double to) {
        double lower = Math.max(0.0, from - box.min(axis));
        double upper = Math.max(0.0, box.max(axis) - to);
        if (lower <= Support.EPSILON && upper <= Support.EPSILON) return box;
        double min = lower >= upper ? box.min(axis) : to;
        double max = lower >= upper ? from : box.max(axis);
        return switch (axis) {
            case X -> new AABB(min, box.minY, box.minZ, max, box.maxY, box.maxZ);
            case Y -> new AABB(box.minX, min, box.minZ, box.maxX, max, box.maxZ);
            case Z -> new AABB(box.minX, box.minY, min, box.maxX, box.maxY, max);
        };
    }

    @Nullable
    private static AABB boxAt(VoxelShape shape, Direction face, Vec3 hit) {
        List<AABB> boxes = shape.toAabbs();
        if (boxes.isEmpty()) return null;
        Direction.Axis axis = face.getAxis();
        int step = face.getAxisDirection().getStep();
        double surface = Double.NaN;
        for (AABB box : boxes) {
            if (!within(box, axis, hit)) {
                continue;
            }
            double value = step > 0 ? box.max(axis) : box.min(axis);
            if (Double.isNaN(surface) || (step > 0 ? value > surface : value < surface)) {
                surface = value;
            }
        }
        if (Double.isNaN(surface)) return null;
        List<AABB> level = new ArrayList<>();
        AABB merged = null;
        for (AABB box : boxes) {
            double value = step > 0 ? box.max(axis) : box.min(axis);
            if (Math.abs(value - surface) > Support.EPSILON) {
                continue;
            }
            if (merged == null && within(box, axis, hit)) {
                merged = box;
            } else {
                level.add(box);
            }
        }
        boolean grew = merged != null;
        while (grew) {
            grew = false;
            for (int index = 0; index < level.size(); index++) {
                AABB box = level.get(index);
                if (touches(merged, box, axis)) {
                    merged = merged.minmax(box);
                    level.remove(index);
                    grew = true;
                    break;
                }
            }
        }
        return merged;
    }

    private static boolean touches(AABB first, AABB second, Direction.Axis axis) {
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other == axis) {
                continue;
            }
            if (first.max(other) < second.min(other) - Support.EPSILON || second.max(other) < first.min(other) - Support.EPSILON) return false;
        }
        return true;
    }

    private static boolean within(AABB box, Direction.Axis axis, Vec3 hit) {
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other == axis) {
                continue;
            }
            double value = other.choose(hit.x, hit.y, hit.z);
            if (value < box.min(other) - Support.EPSILON || value > box.max(other) + Support.EPSILON) return false;
        }
        return true;
    }

    private static double snap(double value) {
        return Math.signum(value) * Math.floor(Math.abs(value) * 16.0 + 0.5) / 16.0;
    }

    public record Surface(VoxelShape outline, VoxelShape support, List<Plate> plates) {
    }

    public record Plate(VoxelShape shape, Direction face) {
    }
}
