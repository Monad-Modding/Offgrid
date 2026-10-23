//? if <1.21 {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Overlays;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("sodium")
@Mixin(BlockRenderer.class)
public class BlockRendererMixin {

    @ModifyExpressionValue(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasOffsetFunction()Z"))
    private boolean offgrid$offsetBlocks(boolean original, @Local(argsOnly = true) BlockRenderContext context) {
        return original || Overlays.canOffset(context.state());
    }
}
*///?}
