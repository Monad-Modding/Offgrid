package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridTags;
import dev.tazer.offgrid.mixin.access.FireBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Overlays {

    public static final int MAX_PER_CELL = 8;

    private static final int[] NONE = new int[0];

    private Overlays() {
    }

    public static boolean canOffset(BlockState state) {
        return state.is(OffgridTags.CAN_OFFSET) && (!state.is(OffgridTags.CAN_BLOCKLOG) || Blocklogging.isAxial(state));
    }

    public static boolean canOverlay(BlockState state) {
        return state.is(OffgridTags.CAN_OVERLAY);
    }

    public static boolean isRemoval(BlockState state) {
        return state.isAir() || state.getBlock() instanceof LiquidBlock;
    }

    public static boolean isZero(Vec3 offset) {
        return offset.x == 0.0 && offset.y == 0.0 && offset.z == 0.0;
    }

    public static boolean obstructed(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay ignore) {
        return OverlayContext.outside(() -> obstructedInWorld(level, host, state, offset, ignore, false));
    }

    public static boolean refusesChange(Level level, BlockPos pos, BlockState requested) {
        if (!Transaction.isOpen() || isRemoval(requested)) return false;
        Cell cell = OffgridIndex.cellAt(level, pos);
        if (cell == null || !cell.hasOverlays() && cell.offset() == null) return false;
        BlockState current = Cells.realState(level, pos);
        if (current.isAir() || current.canBeReplaced() || current.is(requested.getBlock())) return false;
        Vec3 offset = cell.offset() != null && canOffset(requested) ? cell.offset() : Vec3.ZERO;
        if (!OverlayContext.outside(() -> obstructedInWorld(level, pos, requested, offset, null, true))) return false;
        Vec3 currentOffset = cell.offset() != null && canOffset(current) ? cell.offset() : Vec3.ZERO;
        if (OverlayContext.outside(() -> obstructedInWorld(level, pos, current, currentOffset, null, true))) return false;
        Transaction.refuse();
        return true;
    }

    private static boolean obstructedInWorld(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay ignore, boolean replacingHost) {
        VoxelShape shape = OverlayShapes.of(level, host, state, offset, ignore);
        if (shape.isEmpty()) return false;
        for (Overlay existing : Cells.overlays(level, host)) {
            if (existing != ignore && existing.isSamePlacement(state, offset)) return true;
        }
        return collidesInWorld(level, host, shape, ignore, replacingHost);
    }

    public static boolean collides(Level level, BlockPos host, VoxelShape shape, @Nullable Overlay ignore, boolean replacingHost) {
        return OverlayContext.outside(() -> collidesInWorld(level, host, shape, ignore, replacingHost));
    }

    private static boolean collidesInWorld(Level level, BlockPos host, VoxelShape shape, @Nullable Overlay ignore, boolean replacingHost) {
        if (shape.isEmpty()) return false;
        AABB bounds = shape.bounds().move(host.getX(), host.getY(), host.getZ());
        BlockPos min = BlockPos.containing(bounds.minX + Support.EPSILON, bounds.minY + Support.EPSILON, bounds.minZ + Support.EPSILON);
        BlockPos max = BlockPos.containing(bounds.maxX - Support.EPSILON, bounds.maxY - Support.EPSILON, bounds.maxZ - Support.EPSILON);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            BlockPos cell = cursor.immutable();
            int dx = cell.getX() - host.getX();
            int dy = cell.getY() - host.getY();
            int dz = cell.getZ() - host.getZ();
            boolean self = dx == 0 && dy == 0 && dz == 0;
            if (level.hasChunkAt(cell) && !(self && replacingHost)) {
                BlockState occupant = Covers.raw(level, cell, Cells.realState(level, cell));
                if (!occupant.isAir() && !occupant.canBeReplaced()) {
                    VoxelShape occupantShape = occupant.getShape(level, cell, CollisionContext.empty());
                    if (!occupantShape.isEmpty() && Shapes.joinIsNotEmpty(shape, occupantShape.move(dx, dy, dz), BooleanOp.AND)) return true;
                }
            }
            for (Overlay existing : Cells.overlays(level, cell)) {
                if (existing == ignore) {
                    continue;
                }
                VoxelShape existingShape = OverlayShapes.of(level, cell, existing);
                if (!existingShape.isEmpty() && Shapes.joinIsNotEmpty(shape, existingShape.move(dx, dy, dz), BooleanOp.AND)) return true;
            }
        }
        return false;
    }

    public static boolean survives(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing, @Nullable BlockState hostReplacement) {
        return OverlayContext.outside(() -> Support.holds(level, host, state, offset, existing, hostReplacement, false));
    }

    public static boolean offsetHolds(Level level, BlockPos pos, BlockState state, Vec3 offset) {
        return OverlayContext.outside(() -> Support.holds(level, pos, state, offset, null, null, true));
    }

    @Nullable
    public static Overlay add(ServerLevel level, BlockPos host, BlockState state, Vec3 offset, @Nullable CompoundTag tag) {
        OffgridChunk data = Cells.data(level, host, true);
        if (data == null || data.getOrEmpty(host.asLong()).overlays().size() >= MAX_PER_CELL || obstructed(level, host, state, offset, null)) return null;
        Overlay overlay = new Overlay(data.allocateId(), state, offset, tag);
        overlay.materialize(level, host);
        Cells.update(level, host, cell -> cell.with(overlay));
        OverlayContext.run(level, host, overlay, () -> BlockStates.placed(state, level, host, Blocks.AIR.defaultBlockState()));
        level.updateNeighborsAt(host, state.getBlock());
        if (Connections.isConnector(state)) {
            Connections.update(level, host);
        }
        return overlay;
    }

    public static boolean dependsOn(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing, BlockPos changed) {
        return OverlayContext.outside(() -> Support.dependsOn(level, host, state, offset, existing, changed));
    }

    public static List<Support.Contact> contacts(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay existing) {
        return OverlayContext.outside(() -> Support.contacts(level, host, state, offset, existing));
    }

    public static void reevaluate(ServerLevel level, BlockPos pos) {
        Cell cell = Cells.get(level, pos);
        if (cell == null || cell.isEmpty()) return;
        BlockState state = Cells.realState(level, pos);
        if (cell.offset() != null) {
            if (state.isAir()) {
                Cells.update(level, pos, existing -> existing.withOffset(null));
            } else if (!offsetHolds(level, pos, state, cell.offset())) {
                level.destroyBlock(pos, true);
                state = Cells.realState(level, pos);
            }
        }
        for (Overlay overlay : List.copyOf(Cells.overlays(level, pos))) {
            if (!Cells.contains(level, pos, overlay)) {
                continue;
            }
            if (!survives(level, pos, overlay.getState(), overlay.getOffset(), overlay, null) || obstructed(level, pos, overlay.getState(), overlay.getOffset(), overlay)) {
                destroy(level, pos, overlay);
            }
        }
        if (state.isAir() && Cells.hasOverlays(level, pos)) {
            schedulePromotion(level, pos);
        }
    }

    public static void remove(Level level, BlockPos host, Overlay overlay, boolean effects) {
        if (!Cells.contains(level, host, overlay)) return;
        BlockState state = overlay.getState();
        BlockEntity blockEntity = overlay.getBlockEntity();
        OverlayContext.run(level, host, overlay, () -> BlockStates.removed(state, level, host, Blocks.AIR.defaultBlockState(), blockEntity));
        overlay.discardBlockEntity();
        Cells.update(level, host, cell -> cell.without(overlay), effects ? new int[]{overlay.getId()} : NONE);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.updateNeighborsAt(host, state.getBlock());
            revalidate(serverLevel, host);
            if (Connections.isConnector(state)) {
                Connections.update(serverLevel, host);
            }
        }
    }

    public static void destroy(ServerLevel level, BlockPos host, Overlay overlay) {
        if (!Cells.contains(level, host, overlay)) return;
        OverlayContext.run(level, host, overlay, () -> level.destroyBlock(host, true));
        if (Cells.contains(level, host, overlay)) {
            remove(level, host, overlay, true);
        }
    }

    public static void harvest(ServerLevel level, BlockPos host, Predicate<BlockState> breakable, Consumer<ItemStack> drops) {
        for (Overlay overlay : List.copyOf(Cells.overlays(level, host))) {
            if (!Cells.contains(level, host, overlay)) {
                continue;
            }
            BlockState state = overlay.getState();
            if (!OverlayContext.call(level, host, overlay, () -> breakable.test(state))) {
                continue;
            }
            BlockEntity blockEntity = overlay.getBlockEntity();
            List<ItemStack> stacks = OverlayContext.call(level, host, overlay, () -> Block.getDrops(state, level, host, blockEntity));
            remove(level, host, overlay, true);
            for (ItemStack stack : stacks) {
                drops.accept(stack);
            }
        }
    }

    public static void destroyAll(ServerLevel level, BlockPos host) {
        for (Overlay overlay : Cells.overlays(level, host)) {
            destroy(level, host, overlay);
        }
    }

    public static void burn(FireBlock fire, Level level, BlockPos pos, int chance, RandomSource random) {
        if (!(level instanceof ServerLevel server) || !Cells.hasOverlays(level, pos)) return;
        for (Overlay overlay : Cells.overlays(level, pos)) {
            int odds = ((FireBlockAccessor) fire).offgrid$getBurnOdds(overlay.getState());
            if (odds > 0 && random.nextInt(chance) < odds) {
                remove(server, pos, overlay, true);
            }
        }
    }

    public static void explode(ServerLevel level, List<BlockPos> blown) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return;
        for (BlockPos pos : blown) {
            if (Cells.hasOverlays(level, pos)) {
                destroyAll(level, pos);
            }
        }
    }

    public static boolean applyState(Level level, BlockPos host, Overlay overlay, BlockState requested, int flags) {
        if (!Cells.contains(level, host, overlay)) return !isRemoval(requested) && Placement.commit(level, host, overlay, requested);
        if (isRemoval(requested)) {
            OverlayContext.Frame frame = OverlayContext.at(level, host);
            remove(level, host, overlay, frame != null && frame.hasDestroyEffects());
            return true;
        }
        BlockState oldState = overlay.getState();
        BlockState newState = Blocklogging.refit(level, host, overlay, requested);
        if (newState == null || newState == oldState) return false;
        if (!oldState.is(newState.getBlock()) && worsens(level, host, overlay, oldState, newState)) {
            Transaction.refuse();
            return false;
        }
        BlockEntity blockEntity = overlay.getBlockEntity();
        overlay.setState(newState);
        OverlayContext.run(level, host, overlay, () -> {
            BlockStates.removed(oldState, level, host, newState, blockEntity);
            BlockStates.placed(newState, level, host, oldState);
        });
        if (newState.hasBlockEntity()) {
            overlay.materialize(level, host);
        }
        Cells.update(level, host, Cell::relit);
        if (level instanceof ServerLevel serverLevel && (flags & Block.UPDATE_NEIGHBORS) != 0) {
            serverLevel.updateNeighborsAt(host, oldState.getBlock());
            if (newState.hasAnalogOutputSignal()) {
                serverLevel.updateNeighbourForOutputSignal(host, newState.getBlock());
            }
        }
        return true;
    }

    private static boolean worsens(Level level, BlockPos host, Overlay overlay, BlockState oldState, BlockState newState) {
        Vec3 offset = overlay.getOffset();
        if (!survives(level, host, newState, offset, overlay, null) && survives(level, host, oldState, offset, overlay, null)) return true;
        return obstructed(level, host, newState, offset, overlay) && !obstructed(level, host, oldState, offset, overlay);
    }

    @Nullable
    public static Cell notifiedCell(Level level, BlockPos pos) {
        if (level.isClientSide()) return null;
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return null;
        Cell cell = index.getCell(pos);
        if (cell == null || !cell.hasOverlays() || OverlayContext.isActiveAt(level, pos)) return null;
        return cell;
    }

    public static void onBlockChanged(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.isEmpty()) return;
        Cell cell = index.getCell(pos);
        if (cell != null && Covers.isCovered(newState) && !cell.isEmpty()) {
            Covers.queue(level, pos);
        } else if (cell != null) {
            if (cell.offset() != null && !keepsOffset(level, pos, oldState, newState, cell.offset())) {
                Cells.update(level, pos, existing -> existing.withOffset(null));
            }
            if (cell.hasOverlays()) {
                validateHost(level, pos, newState);
            }
        }
        validateNeighbours(level, pos);
        queueOffsetNeighbours(level, index, pos);
        FluidState fluid = newState.getFluidState();
        if (!fluid.isEmpty()) {
            flood(level, pos, fluid);
        }
        if (Connections.involves(level, pos)) {
            Connections.update(level, pos);
        }
    }

    private static boolean keepsOffset(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState, Vec3 offset) {
        if (newState.is(oldState.getBlock())) return true;
        if (isRemoval(newState)) return false;
        if (Covers.isCovered(oldState)) return true;
        if (oldState.isAir()) return canOffset(newState) && offsetHolds(level, pos, newState, offset);
        if (oldState.canBeReplaced()) return false;
        return canOffset(newState);
    }

    private static void validateHost(ServerLevel level, BlockPos host, BlockState newState) {
        if (newState.isAir()) {
            schedulePromotion(level, host);
            return;
        }
        VoxelShape hostShape = OverlayContext.outside(() -> newState.getShape(level, host, CollisionContext.empty()));
        for (Overlay overlay : Cells.overlays(level, host)) {
            boolean keep = survives(level, host, overlay.getState(), overlay.getOffset(), overlay, newState);
            if (keep && !hostShape.isEmpty()) {
                VoxelShape shape = OverlayShapes.of(level, host, overlay);
                keep = shape.isEmpty() || !Shapes.joinIsNotEmpty(shape, hostShape, BooleanOp.AND);
            }
            if (!keep) {
                destroy(level, host, overlay);
            }
        }
    }

    private static void validateNeighbours(ServerLevel level, BlockPos changed) {
        for (Direction direction : Direction.values()) {
            BlockPos cell = changed.relative(direction);
            for (Overlay overlay : Cells.overlays(level, cell)) {
                if (!dependsOn(level, cell, overlay.getState(), overlay.getOffset(), overlay, changed)) {
                    continue;
                }
                if (!survives(level, cell, overlay.getState(), overlay.getOffset(), overlay, null)) {
                    destroy(level, cell, overlay);
                }
            }
        }
    }

    private static void revalidate(ServerLevel level, BlockPos changed) {
        for (Overlay overlay : Cells.overlays(level, changed)) {
            if (!survives(level, changed, overlay.getState(), overlay.getOffset(), overlay, null)) {
                destroy(level, changed, overlay);
            }
        }
        validateNeighbours(level, changed);
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            queueOffsetNeighbours(level, index, changed);
        }
    }

    private static void flood(ServerLevel level, BlockPos changed, FluidState fluid) {
        AABB changedBox = new AABB(changed);
        for (BlockPos cursor : BlockPos.betweenClosed(changed.offset(-1, -1, -1), changed.offset(1, 1, 1))) {
            BlockPos host = cursor.immutable();
            for (Overlay overlay : Cells.overlays(level, host)) {
                AABB bounds = OverlayShapes.bounds(level, host, overlay).move(host.getX(), host.getY(), host.getZ());
                if (!bounds.deflate(Support.EPSILON).intersects(changedBox)) {
                    continue;
                }
                FluidState around = OverlayFluids.around(level, host, overlay.getState(), overlay.getOffset(), changed, fluid);
                OverlayFluids.settle(level, host, overlay, around);
            }
        }
    }

    private static void queueOffsetNeighbours(ServerLevel level, OffgridIndex index, BlockPos changed) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = changed.relative(direction);
            Cell cell = index.getCell(neighbour);
            if (cell == null || cell.offset() == null) {
                continue;
            }
            BlockState state = Cells.realState(level, neighbour);
            if (dependsOn(level, neighbour, state, cell.offset(), null, changed)) {
                index.getTasks().getRevalidations().add(neighbour.asLong());
            }
        }
    }

    public static void revalidateOffset(ServerLevel level, BlockPos pos) {
        Cell cell = Cells.get(level, pos);
        if (cell == null || cell.offset() == null) return;
        BlockState state = Cells.realState(level, pos);
        if (state.isAir() || offsetHolds(level, pos, state, cell.offset())) return;
        level.destroyBlock(pos, true);
    }

    public static void schedulePromotion(ServerLevel level, BlockPos pos) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            index.getTasks().getPromotions().add(pos.asLong());
        }
    }

    public static void promote(ServerLevel level, BlockPos host) {
        BlockState current = Cells.realState(level, host);
        if (!current.isAir()) {
            validateHost(level, host, current);
            return;
        }
        Overlay chosen = null;
        for (Overlay overlay : Cells.overlays(level, host)) {
            if (survives(level, host, overlay.getState(), overlay.getOffset(), overlay, Blocks.AIR.defaultBlockState())) {
                chosen = overlay;
                break;
            }
        }
        if (chosen == null) {
            destroyAll(level, host);
            return;
        }
        Overlay promoted = chosen;
        Vec3 offset = promoted.getOffset();
        CompoundTag tag = promoted.saveBlockEntity();
        promoted.discardBlockEntity();
        Vec3 kept = isZero(offset) ? null : offset;
        Cells.updateSilently(level, host, cell -> cell.without(promoted).withOffset(kept));
        BlockState reshaped = Block.updateFromNeighbourShapes(promoted.getState(), level, host);
        BlockState state = reshaped.isAir() ? promoted.getState() : reshaped;
        level.setBlock(host, state, Block.UPDATE_ALL);
        BlockEntity real = level.getBlockEntity(host);
        if (tag != null && real != null) {
            BlockEntities.load(real, tag, level.registryAccess());
            real.setChanged();
        }
        broadcastBlock(level, host, real);
        Cells.sync(level, host);
    }

    private static void broadcastBlock(ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        ClientboundBlockUpdatePacket update = new ClientboundBlockUpdatePacket(level, pos);
        Packet<ClientGamePacketListener> data = blockEntity == null ? null : blockEntity.getUpdatePacket();
        for (ServerPlayer player : Cells.playersTracking(level, pos)) {
            player.connection.send(update);
            if (data != null) {
                player.connection.send(data);
            }
        }
    }
}
