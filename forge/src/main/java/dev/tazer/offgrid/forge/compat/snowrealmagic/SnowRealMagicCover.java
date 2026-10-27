package dev.tazer.offgrid.forge.compat.snowrealmagic;

import dev.tazer.offgrid.world.Covers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import snownee.snow.block.SnowVariant;

public final class SnowRealMagicCover implements Covers.Cover {

    @Override
    public boolean covers(BlockState state) {
        return state.getBlock() instanceof SnowVariant && state.hasBlockEntity();
    }

    @Override
    @Nullable
    public BlockState raw(BlockGetter getter, BlockPos pos, BlockState state) {
        return ((SnowVariant) state.getBlock()).getRaw(state, getter, pos);
    }

    @Override
    public int layers(BlockGetter getter, BlockPos pos, BlockState state) {
        return ((SnowVariant) state.getBlock()).layers(state, getter, pos);
    }
}
