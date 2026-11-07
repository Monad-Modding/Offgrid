//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import dev.tazer.offgrid.world.BlockEntities;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionRegion.class)
public abstract class RenderSectionRegionMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    private ClientLevel level;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.level);
    }

    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private void offgrid$viewOverlayBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        BlockEntity viewed = BlockEntities.viewedAt(pos);
        if (viewed != null) {
            cir.setReturnValue(viewed);
        }
    }
}
*///?}
