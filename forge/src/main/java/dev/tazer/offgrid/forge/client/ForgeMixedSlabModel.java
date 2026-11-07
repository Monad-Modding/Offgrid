package dev.tazer.offgrid.forge.client;

import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ForgeMixedSlabModel implements IDynamicBakedModel {

    private static final ModelProperty<BlockState> FIRST = new ModelProperty<>();
    private static final ModelProperty<BlockState> SECOND = new ModelProperty<>();
    private static final ModelProperty<ModelData> FIRST_DATA = new ModelProperty<>();
    private static final ModelProperty<ModelData> SECOND_DATA = new ModelProperty<>();

    private final BakedModel fallback;

    public ForgeMixedSlabModel(BakedModel fallback) {
        this.fallback = fallback;
    }

    private static BakedModel modelOf(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return data;
        return ModelData.builder().with(FIRST, entity.getFirst()).with(SECOND, entity.getSecond()).with(FIRST_DATA, halfData(level, pos, entity, false)).with(SECOND_DATA, halfData(level, pos, entity, true)).build();
    }

    private static ModelData halfData(BlockAndTintGetter level, BlockPos pos, MixedSlabBlockEntity entity, boolean second) {
        BlockState half = entity.getHalf(second);
        BlockEntity halfEntity = entity.getHalfEntity(second);
        if (half == null || halfEntity == null) return ModelData.EMPTY;
        AtomicReference<ModelData> data = new AtomicReference<>(ModelData.EMPTY);
        MixedSlabs.viewingHalf(level, pos, half, halfEntity, () -> data.set(modelOf(half).getModelData(level, pos, half, halfEntity.getModelData())));
        return data.get();
    }

    private static ModelData halfData(ModelData data, boolean second) {
        ModelData half = data.get(second ? SECOND_DATA : FIRST_DATA);
        return half == null ? ModelData.EMPTY : half;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random, @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockState first = data.get(FIRST);
        BlockState second = data.get(SECOND);
        if (first == null || second == null) return List.of();
        List<BakedQuad> quads = new ArrayList<>();
        this.addHalf(quads, first, false, side, random, halfData(data, false), renderType);
        this.addHalf(quads, second, true, side, random, halfData(data, true), renderType);
        return quads;
    }

    private void addHalf(List<BakedQuad> quads, BlockState half, boolean second, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        BakedModel model = modelOf(half);
        if (renderType != null && !model.getRenderTypes(half, random, data).contains(renderType)) return;
        for (BakedQuad quad : model.getQuads(half, side, random, data, renderType)) {
            quads.add(second && quad.isTinted() ? new BakedQuad(quad.getVertices(), quad.getTintIndex() + MixedSlabColors.SECOND_TINT_OFFSET, quad.getDirection(), quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion()) : quad);
        }
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource random, @NotNull ModelData data) {
        BlockState first = data.get(FIRST);
        BlockState second = data.get(SECOND);
        if (first == null || second == null) return ChunkRenderTypeSet.of(RenderType.solid());
        return ChunkRenderTypeSet.union(modelOf(first).getRenderTypes(first, random, halfData(data, false)), modelOf(second).getRenderTypes(second, random, halfData(data, true)));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState second = data.get(SECOND);
        return second == null ? this.fallback.getParticleIcon() : modelOf(second).getParticleIcon(halfData(data, true));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.fallback.getParticleIcon();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
