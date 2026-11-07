//? if <26 {
package dev.tazer.offgrid.fabric.mixin;

import dev.tazer.offgrid.world.BlockEntities;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderChunkRegion.class, priority = 1100)
public abstract class RenderChunkRegionMixin {

    @Inject(method = "getBlockEntityRenderData", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$viewOverlayRenderData(BlockPos pos, CallbackInfoReturnable<Object> cir) {
        BlockEntity viewed = BlockEntities.viewedAt(pos);
        if (viewed != null) {
            cir.setReturnValue(viewed.getRenderData());
        }
    }
}
//?}
