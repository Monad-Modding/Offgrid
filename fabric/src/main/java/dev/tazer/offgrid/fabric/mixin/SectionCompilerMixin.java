//? if >=26 {
/*package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.tazer.offgrid.client.OverlayGeometry;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Set;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> startedLayers, SectionBufferBuilderPack pack, ChunkSectionLayer layer);

    @WrapOperation(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0))
    private Set<Map.Entry<ChunkSectionLayer, BufferBuilder>> offgrid$compileOverlays(Map<ChunkSectionLayer, BufferBuilder> startedLayers, Operation<Set<Map.Entry<ChunkSectionLayer, BufferBuilder>>> original, @Local(argsOnly = true) SectionPos sectionPos, @Local(argsOnly = true) RenderSectionRegion region, @Local(argsOnly = true) SectionBufferBuilderPack pack) {
        OverlayGeometry.render(region, sectionPos.origin(), OverlayGeometry.output(layer -> this.getOrBeginLayer(startedLayers, pack, layer)));
        return original.call(startedLayers);
    }
}
*///?} else if >=1.21 {
package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.tazer.offgrid.client.OverlayGeometry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Set;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> layers, SectionBufferBuilderPack pack, RenderType type);

    @WrapOperation(method = "compile", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0))
    private Set<Map.Entry<RenderType, BufferBuilder>> offgrid$compileOverlays(Map<RenderType, BufferBuilder> layers, Operation<Set<Map.Entry<RenderType, BufferBuilder>>> original, @Local(argsOnly = true) SectionPos sectionPos, @Local(argsOnly = true) RenderChunkRegion region, @Local(argsOnly = true) SectionBufferBuilderPack pack) {
        OverlayGeometry.render(region, sectionPos.origin(), type -> this.getOrBeginLayer(layers, pack, type));
        return original.call(layers);
    }
}
//?}
