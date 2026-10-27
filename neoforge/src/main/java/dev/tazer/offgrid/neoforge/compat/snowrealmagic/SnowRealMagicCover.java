//? if <26.2 {
package dev.tazer.offgrid.neoforge.compat.snowrealmagic;

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
        //? if >=1.21 {
        return ((SnowVariant) state.getBlock()).srm$getRaw(state, getter, pos);
        //?} else {
        /*return ((SnowVariant) state.getBlock()).getRaw(state, getter, pos);
        *///?}
    }

    @Override
    public int layers(BlockGetter getter, BlockPos pos, BlockState state) {
        //? if >=1.21 {
        return ((SnowVariant) state.getBlock()).srm$layers(state, getter, pos);
        //?} else {
        /*return ((SnowVariant) state.getBlock()).layers(state, getter, pos);
        *///?}
    }
}
//?}
