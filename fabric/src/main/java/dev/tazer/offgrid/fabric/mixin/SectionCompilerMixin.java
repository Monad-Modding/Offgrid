//? if >=26 {
/*package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.tazer.offgrid.client.OverlayGeometry;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @Inject(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0))
    private void offgrid$compileOverlays(SectionPos sectionPos, RenderSectionRegion region, VertexSorting sorting, SectionBufferBuilderPack pack, CallbackInfoReturnable<SectionCompiler.Results> cir, @Local(ordinal = 0) BlockQuadOutput quadOutput) {
        OverlayGeometry.render(region, sectionPos.origin(), quadOutput);
    }
}
*///?} else if >=1.21 {
package dev.tazer.offgrid.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.tazer.offgrid.client.OverlayGeometry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> layers, SectionBufferBuilderPack pack, RenderType type);

    @Inject(method = "compile", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0))
    private void offgrid$compileOverlays(SectionPos sectionPos, RenderChunkRegion region, VertexSorting sorting, SectionBufferBuilderPack pack, CallbackInfoReturnable<SectionCompiler.Results> cir, @Local Map<RenderType, BufferBuilder> layers) {
        OverlayGeometry.render(region, sectionPos.origin(), type -> this.getOrBeginLayer(layers, pack, type));
    }
}
//?}
