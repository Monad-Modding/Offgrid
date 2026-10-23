package dev.tazer.offgrid.slab;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface SlabFamily {

    boolean isSingle(BlockState state);

    boolean isMember(Block block);

    boolean entersEmptyHalf(BlockState existing, BlockPlaceContext context);

    @Nullable
    Halves combine(BlockState existing, BlockState placing);

    boolean keepsShape(BlockState candidate, BlockState original);

    record Halves(BlockState first, BlockState second, Direction.Axis axis) {
    }
}
