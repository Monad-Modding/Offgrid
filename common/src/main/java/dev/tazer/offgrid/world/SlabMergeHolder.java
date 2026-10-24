package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public interface SlabMergeHolder {

    Map<BlockPos, BlockState> offgrid$getSlabMerges();
}
