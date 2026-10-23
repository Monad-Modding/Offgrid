package dev.tazer.offgrid.forge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.tazer.offgrid.forge.client.ForgeOverlayGeometry;
import dev.tazer.offgrid.world.Connections;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public abstract class RebuildTaskMixin {

    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", ordinal = 0))
    private void offgrid$compileOverlays(float x, float y, float z, ChunkBufferBuilderPack pack, CallbackInfoReturnable<?> cir, @Local RenderChunkRegion region, @Local(ordinal = 0) BlockPos origin, @Local Set<RenderType> layers) {
        ForgeOverlayGeometry.render(region, origin, type -> {
            BufferBuilder builder = pack.builder(type);
            if (layers.add(type)) {
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            }
            return builder;
        });
    }

    @WrapOperation(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getModelData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraftforge/client/model/data/ModelData;)Lnet/minecraftforge/client/model/data/ModelData;", remap = false))
    private ModelData offgrid$viewLoggedConnectors(BakedModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data, Operation<ModelData> original) {
        return Connections.viewing(state, pos, () -> original.call(model, level, pos, state, data));
    }
}
