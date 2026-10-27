//? if <26 {
package dev.tazer.offgrid.neoforge.compat.quark;

import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.slab.SlabFamily;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.building.block.VerticalSlabBlock;

public final class QuarkVerticalSlabs implements SlabFamily {

    @Override
    public boolean isSingle(BlockState state) {
        return state.getBlock() instanceof VerticalSlabBlock && !state.hasBlockEntity() && state.getValue(VerticalSlabBlock.TYPE) != VerticalSlabBlock.VerticalSlabType.DOUBLE;
    }

    @Override
    public boolean isMember(Block block) {
        return block instanceof VerticalSlabBlock;
    }

    @Override
    public boolean entersEmptyHalf(BlockState existing, BlockPlaceContext context) {
        if (!context.replacingClickedOnBlock()) return true;
        Direction side = this.side(existing);
        double along = side.getAxis().choose(context.getClickLocation().x - context.getClickedPos().getX(), context.getClickLocation().y - context.getClickedPos().getY(), context.getClickLocation().z - context.getClickedPos().getZ());
        return side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? along <= 0.5 : along >= 0.5;
    }

    @Override
    @Nullable
    public Halves combine(BlockState existing, BlockState placing) {
        if (!this.isSingle(existing) || !(placing.getBlock() instanceof VerticalSlabBlock)) return null;
        Direction side = this.side(existing);
        BlockState kept = MixedSlabs.dry(existing);
        BlockState added = MixedSlabs.dry(placing.setValue(VerticalSlabBlock.TYPE, VerticalSlabBlock.VerticalSlabType.fromDirection(side)));
        boolean keptFirst = side.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        return keptFirst ? new Halves(kept, added, side.getAxis()) : new Halves(added, kept, side.getAxis());
    }

    @Override
    public boolean keepsShape(BlockState candidate, BlockState original) {
        return this.isSingle(candidate) && candidate.getValue(VerticalSlabBlock.TYPE) == original.getValue(VerticalSlabBlock.TYPE);
    }

    @Override
    public Direction side(BlockState single) {
        return single.getValue(VerticalSlabBlock.TYPE).direction.getOpposite();
    }

    @Override
    public BlockState doubled(BlockState single) {
        return MixedSlabs.dry(single.setValue(VerticalSlabBlock.TYPE, VerticalSlabBlock.VerticalSlabType.DOUBLE));
    }
}
//?}
