package dev.tazer.offgrid.mixin.world;

import dev.tazer.offgrid.world.Connections;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.Overlays;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin implements OffgridIndexHolder {

    @Unique
    private final OffgridIndex offgrid$index = new OffgridIndex();

    @Override
    public OffgridIndex offgrid$getIndex() {
        return this.offgrid$index;
    }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void offgrid$overlayState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        OverlayContext.Frame frame = OverlayContext.at((Level) (Object) this, pos);
        if (frame != null && frame.getOverlay() != null) {
            cir.setReturnValue(frame.getOverlay().getState());
            return;
        }
        BlockState viewed = Connections.viewedAt((Level) (Object) this, pos);
        if (viewed != null) {
            cir.setReturnValue(viewed);
        }
    }

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void offgrid$overlayFluid(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        OverlayContext.Frame frame = OverlayContext.at((Level) (Object) this, pos);
        if (frame != null && frame.getOverlay() != null) {
            cir.setReturnValue(frame.getOverlay().getState().getFluidState());
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void offgrid$overlaySetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        OverlayContext.Frame frame = OverlayContext.at(level, pos);
        if (frame != null && frame.getOverlay() != null) {
            cir.setReturnValue(Overlays.applyState(level, frame.getHost(), frame.getOverlay(), state, flags));
            return;
        }
        if (Overlays.refusesChange(level, pos, state)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private void offgrid$overlayBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        Level level = (Level) (Object) this;
        OverlayContext.Frame frame = OverlayContext.at(level, pos);
        if (frame != null && frame.getOverlay() != null) {
            cir.setReturnValue(frame.getOverlay().materialize(level, frame.getHost()));
        }
    }

    @Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true)
    private void offgrid$keepHostBlockEntity(BlockPos pos, CallbackInfo ci) {
        if (OverlayContext.isActiveAt((Level) (Object) this, pos)) {
            ci.cancel();
        }
    }
}
