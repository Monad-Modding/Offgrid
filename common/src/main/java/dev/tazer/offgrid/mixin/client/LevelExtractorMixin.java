//? if >=26.2 {
/*package dev.tazer.offgrid.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.client.MixedSlabCracks;
import dev.tazer.offgrid.client.OverlayBlockEntities;
import dev.tazer.offgrid.client.OverlayOutline;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @WrapOperation(method = "extractBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape offgrid$overlayOutline(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, Operation<VoxelShape> original) {
        VoxelShape overlay = OverlayOutline.shape(level, pos);
        return overlay == null ? original.call(state, level, pos, context) : overlay;
    }

    @WrapOperation(method = "extractBlockDestroyAnimation", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/client/renderer/state/level/BlockBreakingRenderState;"))
    private BlockBreakingRenderState offgrid$crackOneHalf(BlockPos pos, BlockState state, int progress, Operation<BlockBreakingRenderState> original) {
        return original.call(pos, MixedSlabCracks.half(state, Minecraft.getInstance().level, pos), progress);
    }

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractVisibleBlockEntities", shift = At.Shift.AFTER))
    private void offgrid$extractOverlayBlockEntities(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
        OverlayBlockEntities.extract(partialTick, this.levelRenderState.blockEntityRenderStates);
    }
}
*///?}
