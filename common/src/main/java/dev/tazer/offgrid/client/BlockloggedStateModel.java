//? if >=26 {
/*package dev.tazer.offgrid.client;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.BiFunction;

public class BlockloggedStateModel implements BlockStateModel {

    private static BiFunction<BlockStateModel, List<AABB>, BlockStateModel> factory = BlockloggedStateModel::new;

    protected final BlockStateModel model;
    protected final List<AABB> open;

    public BlockloggedStateModel(BlockStateModel model, List<AABB> open) {
        this.model = model;
        this.open = open;
    }

    public static BlockStateModel wrap(BlockStateModel model, List<AABB> open) {
        return factory.apply(model, open);
    }

    public static void setFactory(BiFunction<BlockStateModel, List<AABB>, BlockStateModel> created) {
        factory = created;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        int start = output.size();
        this.model.collectParts(random, output);
        this.cutParts(output, start);
    }

    protected void cutParts(List<BlockStateModelPart> output, int start) {
        for (int index = start; index < output.size(); index++) {
            output.set(index, this.cut(output.get(index)));
        }
    }

    protected BlockStateModelPart cut(BlockStateModelPart part) {
        return new BlockloggedStateModelPart(part, this.open);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.model.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.model.materialFlags();
    }
}
*///?}
