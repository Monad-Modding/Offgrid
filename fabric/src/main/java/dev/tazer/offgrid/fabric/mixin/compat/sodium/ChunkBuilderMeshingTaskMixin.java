//? if >=26 {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelRenderHooks;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Function;

@Pseudo
@IfModPresent("sodium")
@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin {

    @WrapOperation(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformLevelRenderHooks;runChunkMeshAppenders(Ljava/util/List;Ljava/util/function/Function;Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/core/BlockPos;)V"))
    private void offgrid$meshOverlays(PlatformLevelRenderHooks hooks, List<?> renderers, Function<ChunkSectionLayer, VertexConsumer> buffers, LevelSlice slice, BlockPos origin, Operation<Void> original) {
        original.call(hooks, renderers, buffers, slice, origin);
        OverlayGeometry.render(slice, origin, buffers);
    }
}
*///?} else if >=1.21 {
package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelRenderHooks;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Function;

@Pseudo
@IfModPresent("sodium")
@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin {

    @Shadow
    @Final
    private ChunkRenderContext renderContext;

    @WrapOperation(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformLevelRenderHooks;runChunkMeshAppenders(Ljava/util/List;Ljava/util/function/Function;Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;)V"))
    private void offgrid$meshOverlays(PlatformLevelRenderHooks hooks, List<?> renderers, Function<RenderType, VertexConsumer> buffers, LevelSlice slice, Operation<Void> original) {
        original.call(hooks, renderers, buffers, slice);
        OverlayGeometry.render(slice, this.renderContext.getOrigin().origin(), buffers);
    }
}
//?} else {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Offsets;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@IfModPresent("sodium")
@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin {

    @Shadow
    @Final
    private ChunkRenderContext renderContext;

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "NEW", target = "it/unimi/dsi/fastutil/objects/Reference2ReferenceOpenHashMap"))
    private void offgrid$meshOverlays(ChunkBuildContext buildContext, CancellationToken token, CallbackInfoReturnable<ChunkBuildOutput> cir) {
        WorldSlice slice = buildContext.cache.getWorldSlice();
        BlockPos origin = this.renderContext.getOrigin().origin();
        List<OverlayGeometry.Entry> entries = OverlayGeometry.collect(slice, origin);
        if (entries.isEmpty()) return;
        BlockRenderContext context = new BlockRenderContext(slice);
        BlockRenderer renderer = buildContext.cache.getBlockRenderer();
        BlockModelShaper models = buildContext.cache.getBlockModels();
        Offsets.suppress(() -> {
            for (OverlayGeometry.Entry entry : entries) {
                BlockState state = entry.state();
                context.update(entry.host(), BlockPos.ZERO, state, entry.model(models.getBlockModel(state)), state.getSeed(entry.host()));
                ((BlockRenderContextAccessor) context).offgrid$getOrigin().set(entry.x(origin), entry.y(origin), entry.z(origin));
                renderer.renderModel(context, buildContext.buffers);
            }
        });
    }
}
*///?}
