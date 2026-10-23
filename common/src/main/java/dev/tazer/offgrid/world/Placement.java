package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.mixin.access.BlockItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Placement {


    private static int nextClientId = -1;

    private Placement() {
    }

    public static InteractionResult place(BlockItem item, BlockPlaceContext context, Function<BlockPlaceContext, InteractionResult> vanilla) {
        BlockState placing = item.getBlock().defaultBlockState();
        if (Scope.get(Scope.PLAN) != null || context.getItemInHand().getItem() != item) return vanilla.apply(context);
        if (!Overlays.canOffset(placing) && !Blocklogging.canBlocklog(placing)) return OverlayContext.outside(() -> placeBeneath(item, context, vanilla));
        Decision decision = decide(item, context);
        return switch (decision.kind()) {
            case VANILLA -> vanilla.apply(context);
            case FAIL -> fail(context);
            case OFFSET -> placeOffset(context, decision, vanilla);
            case OVERLAY -> placeOverlay(context, decision, vanilla);
        };
    }

    private static Decision decide(BlockItem item, BlockPlaceContext context) {
        Level level = context.getLevel();
        boolean replacing = context.replacingClickedOnBlock();
        if (replacing && !Blocklogging.isBlockloggable(Cells.realState(level, context.getClickedPos()))) return Decision.VANILLA_DECISION;
        OffgridSettings settings = OffgridSettings.of(level);
        if (settings.allowsBlocklogging() && Blocklogging.canBlocklog(item.getBlock().defaultBlockState())) {
            Decision blocklog = OverlayContext.outside(() -> planBlocklog(item, context));
            if (blocklog != null) return blocklog;
        }
        if (replacing) return Decision.VANILLA_DECISION;
        if (!settings.offsetting()) return Decision.VANILLA_DECISION;
        Direction face = context.getClickedFace();
        BlockPos target = context.getClickedPos();
        BlockPos support = target.relative(face.getOpposite());
        BlockState state = within(Plan.probe(level, target, face), () -> ((BlockItemAccessor) item).offgrid$getPlacementState(context));
        if (state == null || !Overlays.canOffset(state) || !Support.heldFrom(state, face.getOpposite())) return Decision.VANILLA_DECISION;
        if (Cells.realState(level, support).isAir() && !Cells.hasOverlays(level, support)) return Decision.VANILLA_DECISION;
        Player player = context.getPlayer();
        if (player != null && player.isSecondaryUseActive() && vanillaWouldPlace(context, state)) return Decision.VANILLA_DECISION;
        return OverlayContext.outside(() -> plan(level, context, state, face, target, support, settings));
    }

    @Nullable
    private static Decision planBlocklog(BlockItem item, BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction face = context.getClickedFace();
        Vec3 click = context.getClickLocation();
        BlockPos cell = BlockPos.containing(click.x + face.getStepX() * Support.EPSILON, click.y + face.getStepY() * Support.EPSILON, click.z + face.getStepZ() * Support.EPSILON);
        if (!level.hasChunkAt(cell)) return null;
        BlockState host = Cells.realState(level, cell);
        if (!Blocklogging.isBlockloggable(host) || Cells.overlays(level, cell).size() >= Overlays.MAX_PER_CELL) return null;
        BlockPlaceContext inside = new OverlayPlaceContext(context, cell);
        Overlay probe = new Overlay(-1, Blocks.AIR.defaultBlockState(), Vec3.ZERO, null);
        BlockState placed = OverlayContext.call(level, cell, probe, () -> ((BlockItemAccessor) item).offgrid$getPlacementState(inside));
        if (placed == null) return null;
        BlockState state = Blocklogging.isAxial(placed) ? placed : Blocklogging.fit(placed, host, Connections.along(placed, context.getHorizontalDirection().getClockWise().getAxis()));
        if (state == null) return null;
        Vec3 offset = blocklogOffset(level, cell, context.getClickedPos().relative(face.getOpposite()), face, host, state, click);
        if (offset == null) return null;
        Player player = context.getPlayer();
        if (player != null && player.isSecondaryUseActive() && vanillaWouldPlace(context, state)) return null;
        return new Decision(Kind.OVERLAY, cell, offset, state);
    }

    @Nullable
    private static Vec3 blocklogOffset(Level level, BlockPos cell, BlockPos clicked, Direction face, BlockState host, BlockState state, Vec3 click) {
        for (Vec3 centred : Surfaces.centresOn(level, clicked, face, click, state)) {
            Vec3 offset = Blocklogging.lateral(state, centred);
            if (Blocklogging.of(state, host, offset) != null && !Overlays.obstructed(level, cell, state, offset, null)) return offset;
        }
        return null;
    }

    private static Decision plan(Level level, BlockPlaceContext context, BlockState state, Direction face, BlockPos target, BlockPos support, OffgridSettings settings) {
        Vec3 surface = Surfaces.offsetFor(level, support, face, context.getClickLocation(), state);
        if (surface == null) return Decision.FAIL_DECISION;
        Vec3 offset = Blocklogging.lateral(state, surface);
        if (!Surfaces.fits(level, target, state, offset)) return Decision.FAIL_DECISION;
        boolean crosses = crossesInto(state, offset, face);
        BlockState occupant = Cells.realState(level, target);
        boolean occupied = !occupant.isAir() && !occupant.canBeReplaced();
        if (!crosses && !occupied && !Cells.hasOverlays(level, target)) {
            return Overlays.isZero(offset) ? Decision.VANILLA_DECISION : new Decision(Kind.OFFSET, target, offset, state);
        }
        if (!Overlays.canOverlay(state) || !settings.allowsOverlaying()) return Decision.VANILLA_DECISION;
        if (crosses) return new Decision(Kind.OVERLAY, support, offset.add(face.getStepX(), face.getStepY(), face.getStepZ()), state);
        return new Decision(Kind.OVERLAY, target, offset, state);
    }

    private static InteractionResult placeBeneath(BlockItem item, BlockPlaceContext context, Function<BlockPlaceContext, InteractionResult> vanilla) {
        Level level = context.getLevel();
        BlockPos cell = context.getClickedPos();
        if (context.canPlace() || !level.hasChunkAt(cell) || !OffgridSettings.of(level).allowsOverlaying()) return vanilla.apply(context);
        BlockState resident = Cells.realState(level, cell);
        if (resident.isAir() || !Overlays.canOverlay(resident) || Cells.overlays(level, cell).size() >= Overlays.MAX_PER_CELL) return vanilla.apply(context);
        Vec3 stored = Offsets.of(level, cell, resident);
        Vec3 offset = stored == null ? Vec3.ZERO : stored;
        BlockPlaceContext into = new BeneathPlaceContext(context, cell);
        Overlay probe = new Overlay(-1, Blocks.AIR.defaultBlockState(), Vec3.ZERO, null);
        BlockState host = OverlayContext.call(level, cell, probe, () -> ((BlockItemAccessor) item).offgrid$getPlacementState(into));
        if (host == null || Overlays.canOverlay(host) || !fitsBeneath(level, cell, host, resident, offset)) return vanilla.apply(context);
        CompoundTag tag = BlockEntities.saveAt(level, cell);
        OffgridChunk data = Cells.data(level, cell, !level.isClientSide());
        int id = level.isClientSide() ? nextClientId-- : data == null ? -1 : data.allocateId();
        if (id == -1) return vanilla.apply(context);
        Overlay moved = new Overlay(id, resident, offset, tag);
        moved.materialize(level, cell);
        Cells.update(level, cell, existing -> existing.with(moved));
        BlockEntity detached = level.getBlockEntity(cell);
        if (detached != null) {
            level.removeBlockEntity(cell);
        }
        InteractionResult result = vanilla.apply(into);
        if (!result.consumesAction() || !Cells.realState(level, cell).is(host.getBlock())) {
            moved.discardBlockEntity();
            Cells.update(level, cell, existing -> existing.without(moved));
            if (detached != null && Cells.realState(level, cell) == resident) {
                detached.clearRemoved();
                level.setBlockEntity(detached);
            }
        } else if (level instanceof ServerLevel serverLevel && Cells.contains(level, cell, moved)) {
            OverlayContext.run(level, cell, moved, () -> BlockStates.placed(resident, serverLevel, cell, Blocks.AIR.defaultBlockState()));
            moved.materialize(level, cell);
            Cells.sync(serverLevel, cell);
        }
        return result;
    }

    private static boolean fitsBeneath(Level level, BlockPos cell, BlockState host, BlockState resident, Vec3 offset) {
        VoxelShape hostShape = host.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        VoxelShape residentShape = OverlayShapes.of(resident, offset);
        if (!hostShape.isEmpty() && !residentShape.isEmpty() && Shapes.joinIsNotEmpty(hostShape, residentShape, BooleanOp.AND)) return false;
        for (Overlay overlay : Cells.overlays(level, cell)) {
            VoxelShape overlayShape = OverlayShapes.of(level, cell, overlay);
            if (!overlayShape.isEmpty() && !hostShape.isEmpty() && Shapes.joinIsNotEmpty(hostShape, overlayShape, BooleanOp.AND)) return false;
        }
        return Overlays.survives(level, cell, resident, offset, null, host);
    }

    private static InteractionResult placeOffset(BlockPlaceContext context, Decision decision, Function<BlockPlaceContext, InteractionResult> vanilla) {
        Level level = context.getLevel();
        InteractionResult result = within(Plan.offset(level, decision.cell(), decision.offset()), () -> vanilla.apply(context));
        if (!result.consumesAction() || !Overlays.canOffset(Cells.realState(level, decision.cell()))) return result;
        if (level instanceof ServerLevel serverLevel) {
            Offsets.set(serverLevel, decision.cell(), decision.offset());
            serverLevel.updateNeighborsAt(decision.cell(), Cells.realState(level, decision.cell()).getBlock());
        } else {
            Cells.update(level, decision.cell(), cell -> cell.withOffset(decision.offset()));
        }
        return result;
    }

    private static InteractionResult placeOverlay(BlockPlaceContext context, Decision decision, Function<BlockPlaceContext, InteractionResult> vanilla) {
        Level level = context.getLevel();
        BlockPos cell = decision.cell();
        OffgridChunk data = Cells.data(level, cell, !level.isClientSide());
        int id = level.isClientSide() ? nextClientId-- : data == null ? -1 : data.allocateId();
        if (id == -1 && !level.isClientSide()) return fail(context);
        FluidState fluid = OverlayFluids.around(level, cell, decision.state(), decision.offset(), null, null);
        Overlay blank = new Overlay(id, source(fluid).createLegacyBlock(), decision.offset(), null);
        Plan plan = Plan.overlay(level, cell, blank);
        BlockPlaceContext inside = new OverlayPlaceContext(context, cell);
        InteractionResult result = within(plan, () -> OverlayContext.call(level, cell, blank, () -> vanilla.apply(inside)));
        if (!plan.isCommitted()) return result.consumesAction() ? fail(context) : result;
        if (level instanceof ServerLevel serverLevel && blank.getBlockEntity() != null && Cells.contains(level, cell, blank)) {
            Cells.sync(serverLevel, cell);
        }
        return result;
    }

    @Nullable
    public static Plan active() {
        return Scope.get(Scope.PLAN);
    }

    public static boolean commit(Level level, BlockPos host, Overlay overlay, BlockState state) {
        Plan plan = Scope.get(Scope.PLAN);
        if (plan == null || plan.getBlank() != overlay || !plan.getCell().equals(host) || plan.isCommitted()) return false;
        Vec3 offset = overlay.getOffset();
        BlockState hostState = Cells.realState(level, host);
        if (Blocklogging.isFreeStanding(state, offset) && Blocklogging.isBlockloggable(hostState)) {
            BlockState fitted = Blocklogging.fit(state, hostState, null);
            if (fitted == null) return false;
            state = fitted;
        }
        if (!Overlays.canOverlay(state) && Blocklogging.at(level, host, state, offset) == null || !OverlayFluids.holds(state, overlay.getState().getFluidState())) return false;
        if (Cells.overlays(level, host).size() >= Overlays.MAX_PER_CELL) return false;
        if (!Overlays.survives(level, host, state, offset, null, null) || Overlays.obstructed(level, host, state, offset, null)) return false;
        BlockState previous = overlay.getState();
        overlay.setState(state);
        overlay.materialize(level, host);
        plan.markCommitted();
        Cells.update(level, host, cell -> cell.with(overlay));
        BlockStates.placed(state, level, host, previous);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.updateNeighborsAt(host, state.getBlock());
            if (Connections.isConnector(state)) {
                Connections.update(serverLevel, host);
            }
        }
        return true;
    }

    public static boolean assistSurvival(BlockState state, LevelReader reader, BlockPos pos) {
        if (!(reader instanceof Level level)) return false;
        Plan plan = Scope.get(Scope.PLAN);
        if (plan != null && plan.getLevel() == level && plan.getCell().equals(pos)) {
            return switch (plan.getKind()) {
                case PROBE -> Support.heldFrom(state, plan.getFace().getOpposite()) && supportsFace(level, pos.relative(plan.getFace().getOpposite()), plan.getFace());
                case OFFSET -> Overlays.offsetHolds(level, pos, state, plan.getOffset());
                case OVERLAY -> Overlays.survives(level, pos, state, plan.getOffset(), null, null);
            };
        }
        OverlayContext.Frame frame = OverlayContext.at(level, pos);
        if (frame != null) {
            Overlay overlay = frame.getOverlay();
            return overlay != null && Overlays.survives(level, pos, state, overlay.getOffset(), overlay, null);
        }
        Vec3 offset = Offsets.of(level, pos, state);
        return offset != null && Overlays.offsetHolds(level, pos, state, offset);
    }

    private static boolean supportsFace(Level level, BlockPos support, Direction face) {
        if (!level.hasChunkAt(support)) return false;
        BlockState state = Cells.realState(level, support);
        if (!state.isAir() && !state.canBeReplaced() && !state.getBlockSupportShape(level, support).isEmpty()) return true;
        return Cells.hasOverlays(level, support);
    }

    private static boolean vanillaWouldPlace(BlockPlaceContext context, BlockState state) {
        if (!context.canPlace()) return false;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return state.canSurvive(level, pos) && level.isUnobstructed(state, pos, CollisionContext.empty());
    }

    private static boolean crossesInto(BlockState state, Vec3 offset, Direction face) {
        VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        if (shape.isEmpty()) return false;
        AABB bounds = shape.bounds().move(offset.x, offset.y, offset.z);
        Direction.Axis axis = face.getAxis();
        return face.getAxisDirection().getStep() > 0 ? bounds.min(axis) < -Support.EPSILON : bounds.max(axis) > 1.0 + Support.EPSILON;
    }

    private static FluidState source(FluidState fluid) {
        if (fluid.isEmpty() || fluid.isSource() || !(fluid.getType() instanceof FlowingFluid flowing)) return fluid;
        return flowing.getSource(false);
    }

    private static InteractionResult fail(BlockPlaceContext context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            player.containerMenu.sendAllDataToRemote();
        }
        return InteractionResult.FAIL;
    }

    private static <T> T within(Plan plan, Supplier<T> action) {
        return Scope.with(Scope.PLAN, plan, action);
    }

    private static final class OverlayPlaceContext extends BlockPlaceContext {

        private OverlayPlaceContext(BlockPlaceContext context, BlockPos cell) {
            super(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), new BlockHitResult(context.getClickLocation(), context.getClickedFace(), cell.relative(context.getClickedFace().getOpposite()), context.isInside()));
            this.replaceClicked = false;
        }
    }

    private static final class BeneathPlaceContext extends BlockPlaceContext {

        private BeneathPlaceContext(BlockPlaceContext context, BlockPos cell) {
            super(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), new BlockHitResult(context.getClickLocation(), context.getClickedFace(), cell, context.isInside()));
            this.replaceClicked = true;
        }
    }

    private enum Kind {
        VANILLA,
        FAIL,
        OFFSET,
        OVERLAY
    }

    private record Decision(Kind kind, BlockPos cell, Vec3 offset, BlockState state) {

        private static final Decision VANILLA_DECISION = new Decision(Kind.VANILLA, BlockPos.ZERO, Vec3.ZERO, Blocks.AIR.defaultBlockState());
        private static final Decision FAIL_DECISION = new Decision(Kind.FAIL, BlockPos.ZERO, Vec3.ZERO, Blocks.AIR.defaultBlockState());
    }

    public enum PlanKind {
        PROBE,
        OFFSET,
        OVERLAY
    }

    public static final class Plan {

        private final PlanKind kind;
        private final Level level;
        private final BlockPos cell;
        private final Vec3 offset;
        private final Direction face;
        @Nullable
        private final Overlay blank;

        private boolean committed;

        private Plan(PlanKind kind, Level level, BlockPos cell, Vec3 offset, Direction face, @Nullable Overlay blank) {
            this.kind = kind;
            this.level = level;
            this.cell = cell.immutable();
            this.offset = offset;
            this.face = face;
            this.blank = blank;
        }

        private static Plan probe(Level level, BlockPos cell, Direction face) {
            return new Plan(PlanKind.PROBE, level, cell, Vec3.ZERO, face, null);
        }

        private static Plan offset(Level level, BlockPos cell, Vec3 offset) {
            return new Plan(PlanKind.OFFSET, level, cell, offset, Direction.DOWN, null);
        }

        private static Plan overlay(Level level, BlockPos cell, Overlay blank) {
            return new Plan(PlanKind.OVERLAY, level, cell, blank.getOffset(), Direction.DOWN, blank);
        }

        public PlanKind getKind() {
            return this.kind;
        }

        public Level getLevel() {
            return this.level;
        }

        public BlockPos getCell() {
            return this.cell;
        }

        public Vec3 getOffset() {
            return this.offset;
        }

        public Direction getFace() {
            return this.face;
        }

        @Nullable
        public Overlay getBlank() {
            return this.blank;
        }

        public boolean isCommitted() {
            return this.committed;
        }

        private void markCommitted() {
            this.committed = true;
        }
    }
}
