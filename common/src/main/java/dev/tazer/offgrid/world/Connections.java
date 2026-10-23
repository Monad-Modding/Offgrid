package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class Connections {

    private static final String NONE = "none";
    private static final int SETTLE_BUDGET = 256;

    private Connections() {
    }

    public static <T> T viewing(BlockState asking, BlockPos pos, Supplier<T> action) {
        if (!isConnector(asking)) return action.get();
        return Scope.with(Scope.VIEWER, new Viewer(asking, pos.immutable()), action);
    }

    @Nullable
    public static BlockState viewedAt(BlockGetter getter, BlockPos pos) {
        Viewer viewer = Scope.get(Scope.VIEWER);
        if (viewer == null) return null;
        if (viewer.pos().equals(pos)) return viewer.asking();
        return Scope.with(Scope.VIEWER, null, () -> loggedAt(getter, pos));
    }

    public static boolean isConnector(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CrossCollisionBlock || block instanceof WallBlock;
    }

    @Nullable
    public static BlockState loggedAt(BlockGetter getter, BlockPos pos) {
        OffgridIndex index = OffgridIndex.of(getter);
        if (index == null || index.isEmpty()) return null;
        Cell cell = index.getCell(pos);
        if (cell == null || !cell.hasOverlays()) return null;
        for (Overlay overlay : cell.overlays()) {
            BlockState state = overlay.getState();
            if (isConnector(state) && Blocklogging.at(getter, pos, state, overlay.getOffset()) != null) return state;
        }
        return null;
    }

    public static BlockState forShape(BlockState asking, BlockGetter getter, BlockPos pos, BlockPos neighbour, BlockState neighbourState) {
        if (!isConnector(asking)) return neighbourState;
        BlockState logged = loggedAt(getter, neighbour);
        if (logged == null) return neighbourState;
        Direction toward = between(neighbour, pos);
        return toward != null && Blocklogging.canArm(logged, Blocklogging.hostState(getter, neighbour), toward) ? logged : neighbourState;
    }

    public static BlockState forCulling(BlockState asking, BlockGetter getter, BlockPos neighbour, BlockState neighbourState) {
        if (!isConnector(asking)) return neighbourState;
        BlockState logged = loggedAt(getter, neighbour);
        return logged != null ? logged : neighbourState;
    }

    public static void update(ServerLevel level, BlockPos pos) {
        Scope scope = Scope.current();
        enqueueAround(pos);
        if (scope.isSettling()) return;
        scope.setSettling(true);
        try {
            int budget = SETTLE_BUDGET;
            while (!scope.getSettleQueue().isEmpty() && budget-- > 0) {
                BlockPos next = scope.getSettleQueue().poll();
                scope.getSettleQueued().remove(next.asLong());
                settle(level, next);
            }
        } finally {
            scope.setSettling(false);
            scope.getSettleQueue().clear();
            scope.getSettleQueued().clear();
        }
    }

    public static boolean involves(BlockGetter getter, BlockPos pos) {
        if (loggedAt(getter, pos) != null) return true;
        for (Direction direction : Direction.values()) {
            if (loggedAt(getter, pos.relative(direction)) != null) return true;
        }
        return false;
    }

    public static boolean connectsToward(BlockState state, Direction direction) {
        if (!isConnector(state)) return false;
        Property<?> property = side(state, direction);
        return property != null && !isNone(state, property);
    }

    public static boolean continuesThrough(BlockState state, Direction.Axis axis) {
        return connectsToward(state, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE)) && connectsToward(state, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
    }

    public static BlockState disconnect(BlockState state, Direction direction) {
        if (!isConnector(state)) return state;
        Property<?> property = side(state, direction);
        return property == null ? state : withSide(state, property, false);
    }

    public static BlockState connect(BlockState state, Direction direction) {
        if (!isConnector(state)) return state;
        Property<?> property = side(state, direction);
        return property == null || !isNone(state, property) ? state : withSide(state, property, true);
    }

    public static BlockState along(BlockState state, Direction.Axis axis) {
        BlockState along = state;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            along = direction.getAxis() == axis ? connect(along, direction) : disconnect(along, direction);
        }
        return settlePost(along);
    }

    public static BlockState bare(BlockState state) {
        BlockState bare = state;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            bare = disconnect(bare, direction);
        }
        return settlePost(bare);
    }

    public static BlockState settlePost(BlockState state) {
        if (!(state.getBlock() instanceof WallBlock) || !state.hasProperty(WallBlock.UP)) return state;
        boolean north = connectsToward(state, Direction.NORTH);
        boolean east = connectsToward(state, Direction.EAST);
        boolean south = connectsToward(state, Direction.SOUTH);
        boolean west = connectsToward(state, Direction.WEST);
        boolean straight = north && south && !east && !west || east && west && !north && !south;
        return straight ? state : state.setValue(WallBlock.UP, true);
    }

    private static void enqueueAround(BlockPos pos) {
        enqueue(pos);
        for (Direction direction : Direction.values()) {
            enqueue(pos.relative(direction));
        }
    }

    private static void enqueue(BlockPos pos) {
        Scope scope = Scope.current();
        if (scope.getSettleQueued().add(pos.asLong())) {
            scope.getSettleQueue().add(pos.immutable());
        }
    }

    private static void settle(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return;
        BlockState host = Blocklogging.hostState(level, pos);
        for (Overlay overlay : Cells.overlays(level, pos)) {
            BlockState current = overlay.getState();
            if (!isConnector(current) || Blocklogging.at(level, pos, overlay) == null) {
                continue;
            }
            BlockState natural = OverlayContext.call(level, pos, overlay, () -> Block.updateFromNeighbourShapes(current, level, pos));
            BlockState fitted = Blocklogging.fit(natural, host, current);
            if (fitted != null && fitted != current && Overlays.applyState(level, pos, overlay, fitted, Block.UPDATE_CLIENTS)) {
                enqueueAround(pos);
            }
        }
        BlockState real = Cells.realState(level, pos);
        if (!isConnector(real) || !involves(level, pos)) return;
        BlockState natural = OverlayContext.outside(() -> Block.updateFromNeighbourShapes(real, level, pos));
        if (natural != real && !natural.isAir()) {
            level.setBlock(pos, natural, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            enqueueAround(pos);
        }
    }

    @Nullable
    private static Direction between(BlockPos from, BlockPos to) {
        for (Direction direction : Direction.values()) {
            if (from.getX() + direction.getStepX() == to.getX() && from.getY() + direction.getStepY() == to.getY() && from.getZ() + direction.getStepZ() == to.getZ()) return direction;
        }
        return null;
    }

    @Nullable
    private static Property<?> side(BlockState state, Direction direction) {
        String name = direction.getSerializedName();
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals(name)) return property;
        }
        return null;
    }

    private static <T extends Comparable<T>> boolean isNone(BlockState state, Property<T> property) {
        T value = state.getValue(property);
        if (value instanceof Boolean connected) return !connected;
        return NONE.equals(property.getName(value));
    }

    private static <T extends Comparable<T>> BlockState withSide(BlockState state, Property<T> property, boolean connected) {
        for (T value : property.getPossibleValues()) {
            boolean none = value instanceof Boolean flag ? !flag : NONE.equals(property.getName(value));
            if (none != connected) return state.setValue(property, value);
        }
        return state;
    }

    public record Viewer(BlockState asking, BlockPos pos) {
    }
}
