package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class Support {

    public static final double EPSILON = 1.0E-4;

    private Support() {
    }

    public static Direction direction(BlockState state) {
        if (state.is(OffgridTags.FACING_ATTACHING)) {
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
            if (state.hasProperty(BlockStateProperties.FACING)) return state.getValue(BlockStateProperties.FACING).getOpposite();
        }
        if (state.hasProperty(BlockStateProperties.ATTACH_FACE)) {
            return switch (state.getValue(BlockStateProperties.ATTACH_FACE)) {
                case FLOOR -> Direction.DOWN;
                case CEILING -> Direction.UP;
                case WALL -> state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite() : Direction.DOWN;
            };
        }
        if (state.hasProperty(BlockStateProperties.HANGING) && state.getValue(BlockStateProperties.HANGING)) return Direction.UP;
        if (state.getBlock() instanceof CeilingHangingSignBlock) return Direction.UP;
        return Direction.DOWN;
    }

    public static boolean heldFrom(BlockState state, Direction support) {
        if (Blocklogging.isAxial(state)) return state.getValue(BlockStateProperties.AXIS) == support.getAxis();
        return direction(state) == support;
    }

    public static List<Direction> supports(BlockState state) {
        if (!Blocklogging.isAxial(state)) return List.of(direction(state));
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        return List.of(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE), Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
    }

    public static boolean holds(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing, @Nullable BlockState hostReplacement, boolean real) {
        if (!Overlays.isZero(offset)) return OffsetSources.justified(level, host, state, offset, existing, real);
        if (Blocklogging.canBlocklog(state)) return true;
        for (Direction support : supports(state)) {
            Contact contact = contact(level, host, state, offset, existing, support);
            if (contact == null) return true;
            BlockPos supportPos = contact.cell();
            if (!level.hasChunkAt(supportPos)) {
                continue;
            }
            BlockState supportState = supportPos.equals(host) && hostReplacement != null ? hostReplacement : Cells.realState(level, supportPos);
            if (holdsAgainst(level, host, state, offset, existing, supportPos, supportState, support)) return true;
        }
        return false;
    }

    public static boolean dependsOn(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing, BlockPos changed) {
        for (Direction support : supports(state)) {
            Contact contact = contact(level, host, state, offset, existing, support);
            if (contact != null && contact.cell().equals(changed)) return true;
        }
        return false;
    }

    public static List<Contact> contacts(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing) {
        List<Contact> contacts = new ArrayList<>(2);
        for (Direction support : supports(state)) {
            Contact contact = contact(level, host, state, offset, existing, support);
            if (contact != null) {
                contacts.add(contact);
            }
        }
        return contacts;
    }

    @Nullable
    public static Contact contact(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing, Direction support) {
        VoxelShape shape = OverlayShapes.of(level, host, state, offset, existing);
        if (shape.isEmpty()) return null;
        AABB bounds = shape.bounds();
        Direction.Axis axis = support.getAxis();
        int step = support.getAxisDirection().getStep();
        double plane = step < 0 ? bounds.min(axis) : bounds.max(axis);
        int delta = (int) Math.floor(plane + step * EPSILON);
        BlockPos cell = delta == 0 ? host : host.relative(Direction.fromAxisAndDirection(axis, delta < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE), Math.abs(delta));
        double[] centre = {(bounds.minX + bounds.maxX) * 0.5, (bounds.minY + bounds.maxY) * 0.5, (bounds.minZ + bounds.maxZ) * 0.5};
        centre[axis.ordinal()] = plane;
        return new Contact(cell, new Vec3(host.getX() + centre[0], host.getY() + centre[1], host.getZ() + centre[2]));
    }

    private static boolean holdsAgainst(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing, BlockPos supportPos, BlockState supportState, Direction support) {
        if (supportState.isAir() && !Cells.hasOverlays(level, supportPos)) return false;
        VoxelShape shape = OverlayShapes.of(level, host, state, offset, existing);
        if (shape.isEmpty()) return true;
        AABB bounds = shape.bounds().move(host.getX() - supportPos.getX(), host.getY() - supportPos.getY(), host.getZ() - supportPos.getZ());
        for (AABB face : faces(level, supportPos, supportState, existing, support, bounds)) {
            if (holdsCentre(face, bounds, support.getAxis())) return true;
        }
        return false;
    }

    public static Vec3 snap(Level level, BlockPos pos, BlockState state, Vec3 offset, BlockPos supportPos, BlockState supportState, Direction support) {
        return snap(level, pos, state, offset, supportPos, supportState, support, null);
    }

    public static Vec3 snap(Level level, BlockPos pos, BlockState state, Vec3 offset, BlockPos supportPos, BlockState supportState, Direction support, @Nullable Overlay existing) {
        if (supportState.isAir()) return offset;
        VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        if (shape.isEmpty()) return offset;
        AABB bounds = shape.bounds().move(offset.x + pos.getX() - supportPos.getX(), offset.y + pos.getY() - supportPos.getY(), offset.z + pos.getZ() - supportPos.getZ());
        Direction.Axis axis = support.getAxis();
        for (AABB face : faces(level, supportPos, supportState, existing, support, bounds)) {
            if (!holdsCentre(face, bounds, axis)) {
                continue;
            }
            double[] shift = new double[3];
            for (Direction.Axis lateral : Direction.Axis.values()) {
                if (lateral == axis) {
                    continue;
                }
                double width = face.max(lateral) - face.min(lateral);
                double footprint = bounds.max(lateral) - bounds.min(lateral);
                if (width < footprint - EPSILON) {
                    shift[lateral.ordinal()] = (face.min(lateral) + face.max(lateral) - bounds.min(lateral) - bounds.max(lateral)) * 0.5;
                }
            }
            return offset.add(shift[0], shift[1], shift[2]);
        }
        return offset;
    }

    private static List<AABB> faces(Level level, BlockPos supportPos, BlockState supportState, @Nullable Overlay existing, Direction support, AABB bounds) {
        Direction.Axis axis = support.getAxis();
        int step = support.getAxisDirection().getStep();
        double contact = step < 0 ? bounds.min(axis) : bounds.max(axis);
        VoxelShape supportShape = supportState.canBeReplaced() ? Shapes.empty() : supportState.getBlockSupportShape(level, supportPos);
        for (Overlay other : Cells.overlays(level, supportPos)) {
            if (other == existing || existing != null && existing.getId() >= 0 && other.getId() == existing.getId()) {
                continue;
            }
            VoxelShape otherShape = OverlayShapes.support(level, supportPos, other);
            if (!otherShape.isEmpty()) {
                supportShape = Shapes.or(supportShape, otherShape);
            }
        }
        boolean boundary = Math.abs(contact) <= EPSILON || Math.abs(contact - 1.0) <= EPSILON;
        List<AABB> faces = new ArrayList<>();
        for (AABB box : supportShape.toAabbs()) {
            double plane = step < 0 ? box.max(axis) : box.min(axis);
            boolean through = boundary && box.min(axis) < contact - EPSILON && box.max(axis) > contact + EPSILON;
            if (Math.abs(plane - contact) <= EPSILON || through) {
                faces.add(box);
            }
        }
        boolean merged = true;
        while (merged) {
            merged = mergeAdjacent(faces, axis);
        }
        return faces;
    }

    private static boolean mergeAdjacent(List<AABB> faces, Direction.Axis axis) {
        for (int first = 0; first < faces.size(); first++) {
            for (int second = first + 1; second < faces.size(); second++) {
                AABB joined = join(faces.get(first), faces.get(second), axis);
                if (joined != null) {
                    faces.set(first, joined);
                    faces.remove(second);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private static AABB join(AABB first, AABB second, Direction.Axis axis) {
        for (Direction.Axis along : Direction.Axis.values()) {
            if (along == axis) {
                continue;
            }
            Direction.Axis across = remaining(axis, along);
            boolean sameSpan = Math.abs(first.min(across) - second.min(across)) <= EPSILON && Math.abs(first.max(across) - second.max(across)) <= EPSILON;
            boolean touching = first.max(along) >= second.min(along) - EPSILON && second.max(along) >= first.min(along) - EPSILON;
            if (sameSpan && touching) return first.minmax(second);
        }
        return null;
    }

    private static Direction.Axis remaining(Direction.Axis first, Direction.Axis second) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis != first && axis != second) return axis;
        }
        return first;
    }

    private static boolean holdsCentre(AABB face, AABB bounds, Direction.Axis axis) {
        for (Direction.Axis lateral : Direction.Axis.values()) {
            if (lateral == axis) {
                continue;
            }
            double centre = (bounds.min(lateral) + bounds.max(lateral)) * 0.5;
            if (centre < face.min(lateral) - EPSILON || centre > face.max(lateral) + EPSILON) return false;
        }
        return true;
    }

    public record Contact(BlockPos cell, Vec3 location) {
    }
}
