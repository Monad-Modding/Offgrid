package dev.tazer.offgrid.world;

import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.OffgridTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Layers {

    private static final int MAX_SNOW_LAYERS = 8;

    private Layers() {
    }

    public static boolean isLayer(BlockState state) {
        return state.is(OffgridTags.LAYERS);
    }

    public static boolean isHost(BlockState host) {
        return !host.isAir() && !isLayer(host) && !host.canBeReplaced() && !(host.getBlock() instanceof LiquidBlock) && !host.is(Blocks.MOVING_PISTON);
    }

    public static boolean yields(BlockState layer, BlockPlaceContext context) {
        if (!isLayer(layer) || OverlayContext.current() != null || !(context.getItemInHand().getItem() instanceof BlockItem item)) return false;
        return OffgridSettings.of(context.getLevel()).allowsLayering() && cutsThrough(layer, item.getBlock().defaultBlockState());
    }

    private static boolean cutsThrough(BlockState layer, BlockState placing) {
        return cutsLayers(placing) && isHost(placing) && Blocklogging.of(layer, placing) != null;
    }

    private static boolean cutsLayers(BlockState placing) {
        return !isLayer(placing) && !Overlays.canOffset(placing) && !placing.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()).isEmpty();
    }

    @Nullable
    public static Overlay layerIn(Level level, BlockPos pos) {
        for (Overlay overlay : Cells.overlays(level, pos)) {
            if (isLayer(overlay.getState()) && Overlays.isZero(overlay.getOffset()) && Blocklogging.at(level, pos, overlay) != null) return overlay;
        }
        return null;
    }

    public static InteractionResult preserving(BlockItem item, BlockPlaceContext context, Supplier<InteractionResult> place) {
        Level level = context.getLevel();
        if (Scope.get(Scope.REPLACED_LAYERS) != null || OverlayContext.current() != null || !OffgridSettings.of(level).allowsLayering()) return place.get();
        if (!cutsLayers(item.getBlock().defaultBlockState())) return place.get();
        List<Replaced> replaced = new ArrayList<>(2);
        InteractionResult result = Scope.with(Scope.REPLACED_LAYERS, replaced, place);
        if (result.consumesAction()) {
            for (Replaced layer : replaced) {
                restore(level, layer);
            }
        }
        return result;
    }

    public static void onReplaced(Level level, BlockPos pos, BlockState previous, BlockState state) {
        List<Replaced> replaced = Scope.get(Scope.REPLACED_LAYERS);
        if (replaced == null || !isLayer(previous) || !isHost(state)) return;
        replaced.add(new Replaced(level, pos.immutable(), previous));
    }

    private static void restore(Level level, Replaced replaced) {
        if (replaced.level() != level) return;
        BlockPos pos = replaced.pos();
        BlockState layer = replaced.layer();
        if (Blocklogging.of(layer, Cells.realState(level, pos)) == null || layerIn(level, pos) != null || !Overlays.survives(level, pos, layer, Vec3.ZERO, null, null)) return;
        if (level instanceof ServerLevel serverLevel) {
            Overlays.add(serverLevel, pos, layer, Vec3.ZERO, null);
        } else if (Cells.overlays(level, pos).size() < Overlays.MAX_PER_CELL && !Overlays.obstructed(level, pos, layer, Vec3.ZERO, null)) {
            Overlay predicted = new Overlay(Placement.clientId(), layer, Vec3.ZERO, null);
            Cells.update(level, pos, cell -> cell.with(predicted));
        }
    }

    public static void snowfall(ServerLevel level, Biome biome, BlockPos top, int maxLayers) {
        if (maxLayers <= 0 || !OffgridSettings.of(level).allowsLayering()) return;
        BlockPos cell = top;
        BlockState host = Cells.realState(level, cell);
        if (!isHost(host)) {
            if (!host.isAir()) return;
            cell = top.below();
            host = Cells.realState(level, cell);
            if (!isHost(host)) return;
        }
        BlockState snow = Blocks.SNOW.defaultBlockState();
        if (Covers.isCovered(host) || Blocklogging.of(snow, host) == null || sheltered(host)) return;
        BlockPos target = cell;
        Overlay existing = layerIn(level, target);
        if (existing != null) {
            BlockState current = existing.getState();
            if (!current.is(Blocks.SNOW)) return;
            int layers = current.getValue(SnowLayerBlock.LAYERS);
            if (layers >= Math.min(maxLayers, MAX_SNOW_LAYERS) || !OverlayContext.call(level, target, existing, () -> biome.shouldSnow(level, target))) return;
            BlockState grown = current.setValue(SnowLayerBlock.LAYERS, layers + 1);
            if (Blocklogging.of(grown, host) != null) {
                OverlayContext.run(level, target, existing, () -> level.setBlockAndUpdate(target, grown));
            }
            return;
        }
        Overlay probe = new Overlay(-1, Blocks.AIR.defaultBlockState(), Vec3.ZERO, null);
        if (OverlayContext.call(level, target, probe, () -> biome.shouldSnow(level, target)) && Overlays.survives(level, target, snow, Vec3.ZERO, null, null)) {
            Overlays.add(level, target, snow, Vec3.ZERO, null);
        }
    }

    private static boolean sheltered(BlockState host) {
        VoxelShape outline = host.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        if (outline.isEmpty()) return false;
        VoxelShape columns = Shapes.empty();
        for (AABB box : outline.toAabbs()) {
            columns = Shapes.or(columns, Shapes.box(Math.max(0.0, box.minX), 0.0, Math.max(0.0, box.minZ), Math.min(1.0, box.maxX), 1.0, Math.min(1.0, box.maxZ)));
        }
        return Shapes.join(Shapes.block(), columns, BooleanOp.ONLY_FIRST).isEmpty();
    }

    public record Replaced(Level level, BlockPos pos, BlockState layer) {
    }

    public static final class HostOutline extends AbstractList<AABB> {

        private final List<AABB> boxes;

        public HostOutline(List<AABB> boxes) {
            this.boxes = List.copyOf(boxes);
        }

        @Override
        public AABB get(int index) {
            return this.boxes.get(index);
        }

        @Override
        public int size() {
            return this.boxes.size();
        }
    }
}
