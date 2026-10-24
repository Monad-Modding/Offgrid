//? if <26 {
package dev.tazer.offgrid.neoforge;

import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class NeoForgeMixedSlabBlockEntity extends MixedSlabBlockEntity {

    public static final ModelProperty<BlockState> FIRST = new ModelProperty<>();
    public static final ModelProperty<BlockState> SECOND = new ModelProperty<>();

    public NeoForgeMixedSlabBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public ModelData getModelData() {
        if (!this.isComplete()) return ModelData.EMPTY;
        return ModelData.builder().with(FIRST, this.getFirst()).with(SECOND, this.getSecond()).build();
    }

    @Override
    protected void onHalvesChanged() {
        this.requestModelDataUpdate();
    }
}
//?}
