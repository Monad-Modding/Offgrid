//? if >=1.21 {
package dev.tazer.offgrid.mixin.compat.sodium;

import dev.tazer.offgrid.compat.iris.IrisOverlayTags;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@IfModPresent("sodium")
@IfModPresent("iris")
@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class ChunkBuilderMeshingTaskMixin {

    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("HEAD"))
    private void offgrid$trackBuffers(ChunkBuildContext context, CancellationToken token, CallbackInfoReturnable<?> cir) {
        IrisOverlayTags.setBuffers(context.buffers);
    }

    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("RETURN"))
    private void offgrid$releaseBuffers(ChunkBuildContext context, CancellationToken token, CallbackInfoReturnable<?> cir) {
        IrisOverlayTags.setBuffers(null);
    }
}
//?}
