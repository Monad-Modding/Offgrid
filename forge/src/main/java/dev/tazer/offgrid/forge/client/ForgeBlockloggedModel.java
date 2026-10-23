package dev.tazer.offgrid.forge.client;

import dev.tazer.offgrid.client.QuadClipper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ForgeBlockloggedModel extends BakedModelWrapper<BakedModel> {

    private final List<AABB> open;

    public ForgeBlockloggedModel(BakedModel model, List<AABB> open) {
        super(model);
        this.open = open;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return QuadClipper.clip(super.getQuads(state, side, random), this.open);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        return QuadClipper.clip(super.getQuads(state, side, random, data, renderType), this.open);
    }
}

