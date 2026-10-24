//? if >=26 {
/*package dev.tazer.offgrid.neoforge.client;

import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class NeoForgeMixedSlabModel implements BlockStateModel {

    private final BlockStateModel fallback;

    public NeoForgeMixedSlabModel(BlockStateModel fallback) {
        this.fallback = fallback;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        this.fallback.collectParts(random, parts);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return;
        model(entity.getFirst()).collectParts(level, pos, entity.getFirst(), random, parts);
        List<BlockStateModelPart> second = new ArrayList<>();
        model(entity.getSecond()).collectParts(level, pos, entity.getSecond(), random, second);
        for (BlockStateModelPart part : second) {
            parts.add(new RetintedPart(part));
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.fallback.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return this.fallback.particleMaterial();
        return model(entity.getSecond()).particleMaterial(level, pos, entity.getSecond());
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.fallback.materialFlags();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return this.fallback.materialFlags();
        return model(entity.getFirst()).materialFlags(level, pos, entity.getFirst()) | model(entity.getSecond()).materialFlags(level, pos, entity.getSecond());
    }

    private static BlockStateModel model(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    private static BakedQuad retint(BakedQuad quad) {
        BakedQuad.MaterialInfo info = quad.materialInfo();
        if (info.tintIndex() < 0) return quad;
        //? if >=26.3 {
        /^BakedQuad.MaterialInfo retinted = new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), info.itemGlintRenderType(), info.itemGlintSpecialRenderType(), info.tintIndex() + MixedSlabColors.SECOND_TINT_OFFSET, info.shadeDirectionOverride(), info.lightEmission(), info.ambientOcclusion());
        ^///?} else {
        BakedQuad.MaterialInfo retinted = new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), info.tintIndex() + MixedSlabColors.SECOND_TINT_OFFSET, info.shade(), info.lightEmission(), info.ambientOcclusion());
        //?}
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.direction(), retinted, quad.bakedNormals(), quad.bakedColors());
    }

    private record RetintedPart(BlockStateModelPart part) implements BlockStateModelPart {

        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            List<BakedQuad> quads = this.part.getQuads(direction);
            List<BakedQuad> retinted = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                retinted.add(retint(quad));
            }
            return retinted;
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean useAmbientOcclusion() {
            return this.part.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.part.particleMaterial();
        }

        @Override
        @BakedQuad.MaterialFlags
        public int materialFlags() {
            return this.part.materialFlags();
        }
    }
}
*///?} else {
package dev.tazer.offgrid.neoforge.client;

import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.neoforge.NeoForgeMixedSlabBlockEntity;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class NeoForgeMixedSlabModel implements IDynamicBakedModel {

    private final BakedModel fallback;

    public NeoForgeMixedSlabModel(BakedModel fallback) {
        this.fallback = fallback;
    }

    private static BakedModel modelOf(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity)) return data;
        return entity.getModelData();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random, @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockState first = data.get(NeoForgeMixedSlabBlockEntity.FIRST);
        BlockState second = data.get(NeoForgeMixedSlabBlockEntity.SECOND);
        if (first == null || second == null) return List.of();
        List<BakedQuad> quads = new ArrayList<>();
        this.addHalf(quads, first, false, side, random, renderType);
        this.addHalf(quads, second, true, side, random, renderType);
        return quads;
    }

    private void addHalf(List<BakedQuad> quads, BlockState half, boolean second, @Nullable Direction side, RandomSource random, @Nullable RenderType renderType) {
        BakedModel model = modelOf(half);
        if (renderType != null && !model.getRenderTypes(half, random, ModelData.EMPTY).contains(renderType)) return;
        for (BakedQuad quad : model.getQuads(half, side, random, ModelData.EMPTY, renderType)) {
            quads.add(second && quad.isTinted() ? new BakedQuad(quad.getVertices(), quad.getTintIndex() + MixedSlabColors.SECOND_TINT_OFFSET, quad.getDirection(), quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion()) : quad);
        }
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource random, @NotNull ModelData data) {
        BlockState first = data.get(NeoForgeMixedSlabBlockEntity.FIRST);
        BlockState second = data.get(NeoForgeMixedSlabBlockEntity.SECOND);
        if (first == null || second == null) return ChunkRenderTypeSet.all();
        return ChunkRenderTypeSet.union(modelOf(first).getRenderTypes(first, random, ModelData.EMPTY), modelOf(second).getRenderTypes(second, random, ModelData.EMPTY));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState second = data.get(NeoForgeMixedSlabBlockEntity.SECOND);
        return second == null ? this.fallback.getParticleIcon() : modelOf(second).getParticleIcon(ModelData.EMPTY);
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
//?}
