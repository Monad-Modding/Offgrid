package dev.tazer.offgrid.client;

import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
//? if >=26 {
/*import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;

import java.util.ArrayList;
import java.util.List;
*///?} else {
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;
//?}

public final class MixedSlabColors {

    public static final int LAYERS = 16;
    public static final int SECOND_TINT_OFFSET = LAYERS;

    private MixedSlabColors() {
    }

    //? if >=26 {
    /*public static List<BlockTintSource> tintSources() {
        List<BlockTintSource> sources = new ArrayList<>(LAYERS * 2);
        for (int index = 0; index < LAYERS * 2; index++) {
            sources.add(new HalfTint(index >= SECOND_TINT_OFFSET, index % LAYERS));
        }
        return List.copyOf(sources);
    }

    private record HalfTint(boolean second, int layer) implements BlockTintSource {

        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            BlockState half = MixedSlabs.half(level, pos, this.second);
            BlockTintSource source = half == null ? null : Minecraft.getInstance().getBlockColors().getTintSource(half, this.layer);
            return source == null ? -1 : source.colorInWorld(half, level, pos);
        }

        @Override
        public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            BlockState half = MixedSlabs.half(level, pos, this.second);
            BlockTintSource source = half == null ? null : Minecraft.getInstance().getBlockColors().getTintSource(half, this.layer);
            return source == null ? -1 : source.colorAsTerrainParticle(half, level, pos);
        }
    }
    *///?} else {
    public static int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex < 0 || level == null || pos == null) return -1;
        boolean second = tintIndex >= SECOND_TINT_OFFSET;
        BlockState half = MixedSlabs.half(level, pos, second);
        if (half == null) return -1;
        return Minecraft.getInstance().getBlockColors().getColor(half, level, pos, second ? tintIndex - SECOND_TINT_OFFSET : tintIndex);
    }
    //?}
}
