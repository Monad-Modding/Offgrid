package dev.tazer.offgrid.slab;

import dev.tazer.offgrid.registry.OffgridBlocks;
import dev.tazer.offgrid.world.MovingBlockData;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MixedSlabs {

    private static final double REACH_MARGIN = 1.0;
    private static final VoxelShape[][] HALF_SHAPES = {
            {Shapes.box(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), Shapes.box(0.5, 0.0, 0.0, 1.0, 1.0, 1.0)},
            {Shapes.box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), Shapes.box(0.0, 0.5, 0.0, 1.0, 1.0, 1.0)},
            {Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), Shapes.box(0.0, 0.0, 0.5, 1.0, 1.0, 1.0)}};
    private static final List<SlabFamily> FAMILIES = new ArrayList<>(List.of(new HorizontalSlabs()));

    private MixedSlabs() {
    }

    public static void addFamily(SlabFamily family) {
        FAMILIES.add(family);
    }

    @Nullable
    private static SlabFamily familyOf(BlockState state) {
        for (SlabFamily family : FAMILIES) {
            if (family.isSingle(state)) return family;
        }
        return null;
    }

    public static boolean canMergeInto(BlockState existing, BlockPlaceContext context) {
        SlabFamily family = familyOf(existing);
        if (family == null || !(context.getItemInHand().getItem() instanceof BlockItem item)) return false;
        Block held = item.getBlock();
        if (held == existing.getBlock() || !family.isMember(held) || held.defaultBlockState().hasBlockEntity()) return false;
        return family.entersEmptyHalf(existing, context);
    }

    public static boolean tryPlace(BlockPlaceContext context, BlockState placing) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);
        if (placing.hasBlockEntity() || placing.getBlock() == existing.getBlock()) return false;
        SlabFamily family = familyOf(existing);
        SlabFamily.Halves halves = family == null ? null : family.combine(existing, placing);
        if (halves == null) return false;
        if (!level.setBlock(pos, mixedState(halves.axis(), halves.first(), halves.second()), Block.UPDATE_ALL_IMMEDIATE)) return false;
        if (level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) {
            entity.setHalves(halves.first(), halves.second());
        }
        return true;
    }

    public static boolean mergesInto(Level level, BlockPos from, BlockState moving, BlockPos to, BlockState fixed, Direction direction) {
        if (merge(moving, fixed, direction) == null) return false;
        return !OffgridIndex.hasCell(level, from.getX(), from.getY(), from.getZ()) && !OffgridIndex.hasCell(level, to.getX(), to.getY(), to.getZ());
    }

    @Nullable
    public static Merge merge(BlockState moving, BlockState fixed, Direction direction) {
        SlabFamily family = familyOf(fixed);
        if (family == null || !family.isSingle(moving)) return null;
        Direction side = family.side(moving);
        if (side != family.side(fixed).getOpposite() || side == direction) return null;
        SlabFamily.Halves halves = family.combine(fixed, moving);
        if (halves == null) return null;
        BlockState state = moving.getBlock() == fixed.getBlock() ? family.doubled(fixed) : mixedState(halves.axis(), halves.first(), halves.second());
        MovingBlockData.Stationary stationary = family.side(fixed).getAxisDirection() == Direction.AxisDirection.POSITIVE ? MovingBlockData.Stationary.SECOND : MovingBlockData.Stationary.FIRST;
        return new Merge(state, halves.first(), halves.second(), stationary);
    }

    public static BlockState mixedState(Direction.Axis axis, @Nullable BlockState first, @Nullable BlockState second) {
        BlockState state = OffgridBlocks.getMixedSlab().defaultBlockState().setValue(MixedSlabBlock.AXIS, axis);
        if (first == null || second == null) return state;
        int light = Math.min(15, Math.max(first.getLightEmission(), second.getLightEmission()));
        boolean opaque = first.canOcclude() && second.canOcclude();
        return state.setValue(MixedSlabBlock.LIGHT, light).setValue(MixedSlabBlock.OPAQUE, opaque).setValue(MixedSlabBlock.WEATHERING, isAging(first) || isAging(second));
    }

    public static boolean isAging(BlockState half) {
        return half.getBlock() instanceof ChangeOverTimeBlock<?> aging && aging.getNext(half).isPresent();
    }

    @Nullable
    public static MixedSlabBlockEntity entity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity && entity.isComplete() ? entity : null;
    }

    @Nullable
    public static BlockState half(BlockGetter level, BlockPos pos, boolean second) {
        MixedSlabBlockEntity entity = entity(level, pos);
        return entity == null ? null : entity.getHalf(second);
    }

    public static void replaceHalf(Level level, BlockPos pos, boolean second, BlockState half) {
        MixedSlabBlockEntity entity = entity(level, pos);
        if (entity == null) return;
        replaceHalves(level, pos, entity, second ? entity.getFirst() : half, second ? half : entity.getSecond());
    }

    public static void replaceHalves(Level level, BlockPos pos, MixedSlabBlockEntity entity, BlockState first, BlockState second) {
        BlockState current = level.getBlockState(pos);
        if (!(current.getBlock() instanceof MixedSlabBlock)) return;
        BlockState updated = mixedState(current.getValue(MixedSlabBlock.AXIS), first, second);
        entity.setHalves(first, second);
        if (updated != current) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    public static boolean keepsShape(BlockState candidate, BlockState original) {
        if (candidate.hasBlockEntity()) return false;
        for (SlabFamily family : FAMILIES) {
            if (family.isSingle(original)) return family.keepsShape(candidate, original);
        }
        return false;
    }

    public static PushReaction pushReaction(BlockGetter level, BlockPos pos, PushReaction original) {
        MixedSlabBlockEntity entity = entity(level, pos);
        if (entity == null) return original;
        if (isImmovable(entity.getFirst(), level, pos) || isImmovable(entity.getSecond(), level, pos)) return PushReaction.BLOCK;
        return original;
    }

    private static boolean isImmovable(BlockState half, BlockGetter level, BlockPos pos) {
        return half.getPistonPushReaction() == PushReaction.BLOCK || half.getDestroySpeed(level, pos) == -1.0F;
    }

    public static BlockState dry(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, false) : state;
    }

    public static VoxelShape halfShape(Direction.Axis axis, boolean second) {
        return HALF_SHAPES[axis.ordinal()][second ? 1 : 0];
    }

    public static boolean targetsSecond(Entity viewer, BlockPos pos, Direction.Axis axis) {
        Vec3 eye = viewer.getEyePosition();
        double reach = viewer instanceof Player player ? Players.reach(player) : 6.0;
        Vec3 end = eye.add(viewer.getViewVector(1.0F).scale(reach + REACH_MARGIN));
        Optional<Vec3> hit = new AABB(pos).clip(eye, end);
        Vec3 point = hit.orElse(eye);
        double along = axis.choose(point.x - pos.getX(), point.y - pos.getY(), point.z - pos.getZ());
        if (Math.abs(along - 0.5) < 1.0E-4) return axis.choose(eye.x - pos.getX(), eye.y - pos.getY(), eye.z - pos.getZ()) > 0.5;
        return along > 0.5;
    }

    public static boolean targetsSecond(HitResult target, BlockPos pos, Direction.Axis axis) {
        return targetsSecond(target.getLocation(), pos, axis);
    }

    public static boolean targetsSecond(Vec3 location, BlockPos pos, Direction.Axis axis) {
        return axis.choose(location.x - pos.getX(), location.y - pos.getY(), location.z - pos.getZ()) > 0.5;
    }

    public static boolean containsSecond(Vec3 point, BlockPos pos, Direction.Axis axis) {
        if (axis == Direction.Axis.Y) return true;
        return axis.choose(point.x - pos.getX(), point.y - pos.getY(), point.z - pos.getZ()) > 0.5;
    }

    public record Merge(BlockState state, BlockState first, BlockState second, MovingBlockData.Stationary stationary) {
    }
}
