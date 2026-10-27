package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Covers {

    private static final int MAX_SNOW_LAYERS = 8;
    private static final List<Cover> COVERS = new CopyOnWriteArrayList<>();

    private Covers() {
    }

    public static void addCover(Cover cover) {
        COVERS.add(cover);
    }

    @Nullable
    private static Cover of(BlockState state) {
        if (COVERS.isEmpty()) return null;
        for (Cover cover : COVERS) {
            if (cover.covers(state)) return cover;
        }
        return null;
    }

    public static boolean isCovered(BlockState state) {
        return of(state) != null;
    }

    public static BlockState raw(BlockGetter getter, BlockPos pos, BlockState state) {
        Cover cover = of(state);
        if (cover == null) return state;
        BlockState raw = cover.raw(getter, pos, state);
        return raw == null || raw.isAir() ? state : raw;
    }

    public static int snow(BlockGetter getter, BlockPos pos, BlockState state) {
        Cover cover = of(state);
        return cover == null ? 0 : cover.layers(getter, pos, state);
    }

    public static void queue(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel) || COVERS.isEmpty() || !isCovered(Cells.realState(level, pos))) return;
        OffgridIndex index = OffgridIndex.of(level);
        if (index != null) {
            index.getTasks().getUncovers().add(pos.asLong());
        }
    }

    public static void uncover(ServerLevel level, BlockPos pos) {
        BlockState covered = Cells.realState(level, pos);
        Cover cover = of(covered);
        Cell cell = Cells.get(level, pos);
        if (cover == null || cell == null || cell.isEmpty()) return;
        BlockState raw = cover.raw(level, pos, covered);
        if (raw == null || raw.isAir() || isCovered(raw)) return;
        int layers = Math.min(cover.layers(level, pos, covered), MAX_SNOW_LAYERS);
        level.setBlock(pos, raw, Block.UPDATE_ALL);
        if (layers > 0) {
            keepSnow(level, pos, raw, layers);
        }
    }

    private static void keepSnow(ServerLevel level, BlockPos pos, BlockState host, int layers) {
        Overlay existing = Layers.layerIn(level, pos);
        int kept = 0;
        if (existing == null) {
            for (int candidate = layers; candidate > 0 && kept == 0; candidate--) {
                BlockState snow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, candidate);
                if (Blocklogging.of(snow, host) != null && Overlays.survives(level, pos, snow, Vec3.ZERO, null, null) && Overlays.add(level, pos, snow, Vec3.ZERO, null) != null) {
                    kept = candidate;
                }
            }
        } else if (existing.getState().is(Blocks.SNOW)) {
            int current = existing.getState().getValue(SnowLayerBlock.LAYERS);
            for (int total = Math.min(current + layers, MAX_SNOW_LAYERS); total > current && kept == 0; total--) {
                BlockState grown = existing.getState().setValue(SnowLayerBlock.LAYERS, total);
                int added = total - current;
                if (Blocklogging.of(grown, host) != null && OverlayContext.call(level, pos, existing, () -> level.setBlock(pos, grown, Block.UPDATE_ALL))) {
                    kept = added;
                }
            }
        }
        if (kept < layers) {
            Block.dropResources(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers - kept), level, pos);
        }
    }

    public interface Cover {

        boolean covers(BlockState state);

        @Nullable
        BlockState raw(BlockGetter getter, BlockPos pos, BlockState state);

        int layers(BlockGetter getter, BlockPos pos, BlockState state);
    }
}
