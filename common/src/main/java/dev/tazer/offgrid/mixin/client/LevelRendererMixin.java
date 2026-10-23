package dev.tazer.offgrid.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=26.2 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.client.OverlayCracks;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else if >=26 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.client.MixedSlabCracks;
import dev.tazer.offgrid.client.OverlayBlockEntities;
import dev.tazer.offgrid.client.OverlayCracks;
import dev.tazer.offgrid.client.OverlayOutline;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
import dev.tazer.offgrid.client.OverlayOutline;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
//?}

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    //? if >=26.2 {
    /*@WrapOperation(method = "submitBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 offgrid$offsetCracks(BlockState state, BlockPos pos, Operation<Vec3> original) {
        return original.call(state, pos).add(OverlayCracks.shift(pos, state));
    }

    @Inject(method = "submitBlockDestroyAnimation", at = @At("TAIL"))
    private void offgrid$submitOverlayCracks(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState levelRenderState, CallbackInfo ci) {
        OverlayCracks.submit(poseStack, collector, levelRenderState.cameraRenderState.pos);
    }
    *///?} else if >=26 {
    /*@Shadow
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

    @Inject(method = "extractLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleBlockEntities", shift = At.Shift.AFTER))
    private void offgrid$extractOverlayBlockEntities(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
        OverlayBlockEntities.extract(partialTick, this.levelRenderState.blockEntityRenderStates);
    }

    @WrapOperation(method = "submitBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitBreakingBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;JI)V"))
    private void offgrid$offsetCracks(SubmitNodeCollector collector, PoseStack poseStack, BlockStateModel model, long seed, int progress, Operation<Void> original, @Local BlockBreakingRenderState state) {
        Vec3 shift = OverlayCracks.shift(state.blockPos(), state.blockState());
        poseStack.pushPose();
        poseStack.translate(shift.x, shift.y, shift.z);
        original.call(collector, poseStack, model, seed, progress);
        poseStack.popPose();
    }

    @Inject(method = "submitBlockDestroyAnimation", at = @At("TAIL"))
    private void offgrid$submitOverlayCracks(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState levelRenderState, CallbackInfo ci) {
        OverlayCracks.submit(poseStack, collector, levelRenderState.cameraRenderState.pos);
    }
    *///?} else {
    @WrapOperation(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape offgrid$overlayOutline(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, Operation<VoxelShape> original) {
        VoxelShape overlay = OverlayOutline.shape(level, pos);
        return overlay == null ? original.call(state, level, pos, context) : overlay;
    }
    //?}
}
