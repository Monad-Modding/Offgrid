//? if <26 {
package dev.tazer.offgrid.mixin.client.render;

import dev.tazer.offgrid.world.Connections;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderChunkRegion.class)
public abstract class RenderChunkRegionMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    protected Level level;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.level);
    }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void offgrid$viewLoggedConnector(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BlockState viewed = Connections.viewedAt((RenderChunkRegion) (Object) this, pos);
        if (viewed != null) {
            cir.setReturnValue(viewed);
        }
    }
}
//?}
