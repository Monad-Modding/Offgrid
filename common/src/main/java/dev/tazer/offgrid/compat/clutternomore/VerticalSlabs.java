package dev.tazer.offgrid.compat.clutternomore;

import dev.tazer.clutternomore.block.VerticalSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.slab.SlabFamily;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class VerticalSlabs implements SlabFamily {

    @Override
    public boolean isSingle(BlockState state) {
        return state.getBlock() instanceof VerticalSlabBlock && !state.hasBlockEntity() && !state.getValue(VerticalSlabBlock.DOUBLE);
    }

    @Override
    public boolean isMember(Block block) {
        return block instanceof VerticalSlabBlock;
    }

    @Override
    public boolean entersEmptyHalf(BlockState existing, BlockPlaceContext context) {
        if (!context.replacingClickedOnBlock()) return true;
        double x = context.getClickLocation().x - context.getClickedPos().getX();
        double z = context.getClickLocation().z - context.getClickedPos().getZ();
        return switch (existing.getValue(VerticalSlabBlock.FACING)) {
            case NORTH -> z >= 0.5;
            case EAST -> x <= 0.5;
            case SOUTH -> z <= 0.5;
            case WEST -> x >= 0.5;
            default -> false;
        };
    }

    @Override
    @Nullable
    public Halves combine(BlockState existing, BlockState placing) {
        if (!this.isSingle(existing) || !(placing.getBlock() instanceof VerticalSlabBlock)) return null;
        Direction side = existing.getValue(VerticalSlabBlock.FACING);
        BlockState kept = MixedSlabs.dry(existing);
        BlockState added = MixedSlabs.dry(placing.setValue(VerticalSlabBlock.FACING, side.getOpposite()).setValue(VerticalSlabBlock.DOUBLE, false));
        boolean keptFirst = side.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        return keptFirst ? new Halves(kept, added, side.getAxis()) : new Halves(added, kept, side.getAxis());
    }

    @Override
    public boolean keepsShape(BlockState candidate, BlockState original) {
        return this.isSingle(candidate) && candidate.getValue(VerticalSlabBlock.FACING) == original.getValue(VerticalSlabBlock.FACING);
    }
}
