//? if <1.21 {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@IfModPresent("sodium")
@Mixin(BlockRenderContext.class)
public interface BlockRenderContextAccessor {

    @Accessor("origin")
    Vector3f offgrid$getOrigin();
}
*///?}
