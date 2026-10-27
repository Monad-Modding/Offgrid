//? if <26.2 {
package dev.tazer.offgrid.neoforge.compat.additionalplacements;

import com.firemerald.additionalplacements.block.VerticalSlabBlock;
import com.firemerald.additionalplacements.block.interfaces.IPlacementBlock;
import com.firemerald.additionalplacements.block.interfaces.ISlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.slab.SlabFamily;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

public final class AdditionalPlacementsSlabs implements SlabFamily {

    public static Block parentOf(Block block) {
        return block instanceof VerticalSlabBlock vertical ? vertical.parentBlock : block;
    }

    @Override
    public boolean isSingle(BlockState state) {
        return state.getBlock() instanceof VerticalSlabBlock && !state.hasBlockEntity() && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
    }

    @Override
    public boolean isMember(Block block) {
        return block instanceof VerticalSlabBlock || block instanceof SlabBlock && block instanceof IPlacementBlock<?> placement && placement.additionalplacements$hasAdditionalStates();
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
        Block parent = parentOf(placing.getBlock());
        if (!this.isSingle(existing) || !(parent instanceof SlabBlock) || !this.isMember(parent)) return null;
        Direction side = this.side(existing);
        BlockState kept = MixedSlabs.dry(existing);
        BlockState added = MixedSlabs.dry(((ISlabBlock<?>) parent).additionalplacements$forPlacing(side.getOpposite(), parent.defaultBlockState()));
        if (!(added.getBlock() instanceof VerticalSlabBlock)) return null;
        boolean keptFirst = side.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        return keptFirst ? new Halves(kept, added, side.getAxis()) : new Halves(added, kept, side.getAxis());
    }

    @Override
    public boolean keepsShape(BlockState candidate, BlockState original) {
        return this.isSingle(candidate) && this.side(candidate) == this.side(original);
    }

    @Override
    public Direction side(BlockState single) {
        return ((VerticalSlabBlock) single.getBlock()).additionalplacements$getPlacing(single);
    }

    @Override
    public BlockState doubled(BlockState single) {
        return MixedSlabs.dry(single.setValue(SlabBlock.TYPE, SlabType.DOUBLE));
    }
}
//?}
