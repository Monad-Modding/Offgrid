//? if >=26 {
/*package dev.tazer.offgrid.neoforge.client;

import dev.tazer.offgrid.client.BlockloggedStateModel;
import dev.tazer.offgrid.client.BlockloggedStateModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class NeoForgeBlockloggedStateModel extends BlockloggedStateModel {

    public NeoForgeBlockloggedStateModel(BlockStateModel model, List<AABB> open) {
        super(model, open);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int start = parts.size();
        this.model.collectParts(level, pos, state, random, parts);
        this.cutParts(parts, start);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.model.particleMaterial(level, pos, state);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.model.materialFlags(level, pos, state);
    }

    @Override
    protected BlockStateModelPart cut(BlockStateModelPart part) {
        return new Part(part, this.open);
    }

    private static final class Part extends BlockloggedStateModelPart {

        private Part(BlockStateModelPart part, List<AABB> open) {
            super(part, open);
        }

        @Override
        public TriState ambientOcclusion() {
            return this.part.ambientOcclusion();
        }
    }
}
*///?}
