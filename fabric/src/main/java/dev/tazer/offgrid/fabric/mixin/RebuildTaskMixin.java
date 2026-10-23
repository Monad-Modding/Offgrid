//? if <1.21 {
/*package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.tazer.offgrid.client.OverlayGeometry;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public abstract class RebuildTaskMixin {

    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", ordinal = 0))
    private void offgrid$compileOverlays(float x, float y, float z, ChunkBufferBuilderPack pack, CallbackInfoReturnable<?> cir, @Local RenderChunkRegion region, @Local(ordinal = 0) BlockPos origin, @Local Set<RenderType> layers) {
        OverlayGeometry.render(region, origin, type -> {
            BufferBuilder builder = pack.builder(type);
            if (layers.add(type)) {
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            }
            return builder;
        });
    }
}
*///?}
