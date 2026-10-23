package dev.tazer.offgrid.fabric.client;

import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
//? if >=26 {
/*import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
*///?} else {
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.function.Supplier;
//?}

//? if >=26 {
/*public final class FabricMixedSlabModel extends WrapperBlockStateModel {

    public FabricMixedSlabModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return;
        emitHalf(emitter, level, pos, entity.getFirst(), false, random, cullTest);
        emitHalf(emitter, level, pos, entity.getSecond(), true, random, cullTest);
    }

    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return super.particleMaterial(level, pos, state);
        return model(entity.getSecond()).particleMaterial(level, pos, entity.getSecond());
    }

    private static void emitHalf(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState half, boolean second, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        if (second) {
            emitter.pushTransform(quad -> {
                if (quad.tintIndex() >= 0) {
                    quad.tintIndex(quad.tintIndex() + MixedSlabColors.SECOND_TINT_OFFSET);
                }
                return true;
            });
        }
        model(half).emitQuads(emitter, level, pos, half, random, cullTest);
        if (second) {
            emitter.popTransform();
        }
    }

    private static BlockStateModel model(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }
}
*///?} else {
public final class FabricMixedSlabModel extends ForwardingBakedModel {

    public FabricMixedSlabModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        if (!(view.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return;
        emitHalf(view, entity.getFirst(), false, pos, random, context);
        emitHalf(view, entity.getSecond(), true, pos, random, context);
    }

    private static void emitHalf(BlockAndTintGetter view, BlockState half, boolean second, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        RenderMaterial material = renderer.materialFinder().blendMode(BlendMode.fromRenderLayer(ItemBlockRenderTypes.getChunkRenderType(half))).find();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(half);
        context.pushTransform(quad -> {
            quad.material(material);
            if (second && quad.colorIndex() >= 0) {
                quad.colorIndex(quad.colorIndex() + MixedSlabColors.SECOND_TINT_OFFSET);
            }
            return true;
        });
        model.emitBlockQuads(view, half, pos, random, context);
        context.popTransform();
    }
}
//?}
