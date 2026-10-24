package dev.tazer.offgrid.slab;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

public final class HorizontalSlabs implements SlabFamily {

    @Override
    public boolean isSingle(BlockState state) {
        return state.getBlock() instanceof SlabBlock && !state.hasBlockEntity() && state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
    }

    @Override
    public boolean isMember(Block block) {
        return block instanceof SlabBlock;
    }

    @Override
    public boolean entersEmptyHalf(BlockState existing, BlockPlaceContext context) {
        if (!context.replacingClickedOnBlock()) return true;
        boolean upperHalf = context.getClickLocation().y - context.getClickedPos().getY() > 0.5;
        Direction face = context.getClickedFace();
        if (existing.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) return face == Direction.UP || upperHalf && face.getAxis().isHorizontal();
        return face == Direction.DOWN || !upperHalf && face.getAxis().isHorizontal();
    }

    @Override
    @Nullable
    public Halves combine(BlockState existing, BlockState placing) {
        if (!this.isSingle(existing) || !(placing.getBlock() instanceof SlabBlock)) return null;
        boolean bottom = existing.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        BlockState kept = MixedSlabs.dry(existing);
        BlockState added = MixedSlabs.dry(placing.setValue(SlabBlock.TYPE, bottom ? SlabType.TOP : SlabType.BOTTOM));
        return bottom ? new Halves(kept, added, Direction.Axis.Y) : new Halves(added, kept, Direction.Axis.Y);
    }

    @Override
    public boolean keepsShape(BlockState candidate, BlockState original) {
        return this.isSingle(candidate) && candidate.getValue(SlabBlock.TYPE) == original.getValue(SlabBlock.TYPE);
    }

    @Override
    public Direction side(BlockState single) {
        return single.getValue(SlabBlock.TYPE) == SlabType.BOTTOM ? Direction.DOWN : Direction.UP;
    }

    @Override
    public BlockState doubled(BlockState single) {
        return MixedSlabs.dry(single.setValue(SlabBlock.TYPE, SlabType.DOUBLE));
    }
}
