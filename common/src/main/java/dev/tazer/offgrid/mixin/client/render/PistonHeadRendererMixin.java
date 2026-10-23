package dev.tazer.offgrid.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.world.MovingBlockData;
import dev.tazer.offgrid.world.MovingBlockDataHolder;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=26 {
/*import dev.tazer.offgrid.client.MovingOverlayPart;
import dev.tazer.offgrid.client.MovingPistonParts;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.RenderShape;

import java.util.ArrayList;
import java.util.List;
*///?} else {
import dev.tazer.offgrid.client.AssembledOverlayRender;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
//?}

@Mixin(PistonHeadRenderer.class)
public abstract class PistonHeadRendererMixin {

    //? if >=26 {
    /*@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;createMovingBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Holder;Lnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/block/MovingBlockRenderState;"))
    private MovingBlockRenderState offgrid$splitMixedSlab(BlockPos pos, BlockState state, Holder<Biome> biome, ClientLevel level, Operation<MovingBlockRenderState> original, @Local(argsOnly = true) PistonMovingBlockEntity piston, @Local(argsOnly = true) PistonHeadRenderState renderState) {
        MovingBlockData data = ((MovingBlockDataHolder) piston).offgrid$getMovingBlockData();
        MovingPistonParts parts = (MovingPistonParts) renderState;
        if (!pos.equals(piston.getBlockPos().relative(piston.getMovementDirection().getOpposite()))) return original.call(pos, state, biome, level);
        List<MovingOverlayPart> overlays = new ArrayList<>();
        if (data != null && data.cell() != null && state == piston.getMovedState()) {
            for (AssembledCell.AssembledOverlay overlay : data.cell().overlays()) {
                if (overlay.state().getRenderShape() == RenderShape.MODEL) {
                    overlays.add(new MovingOverlayPart(original.call(pos, overlay.state(), biome, level), overlay.offset()));
                }
            }
        }
        parts.offgrid$setOverlays(List.copyOf(overlays));
        if (data == null || !data.hasHalves() || !(state.getBlock() instanceof MixedSlabBlock)) {
            parts.offgrid$setSecondHalf(null);
            return original.call(pos, state, biome, level);
        }
        parts.offgrid$setSecondHalf(original.call(pos, data.second(), biome, level));
        return original.call(pos, data.first(), biome, level);
    }

    //? if >=26.2 {
    /^@WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitMovingBlock(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/MovingBlockRenderState;I)V", ordinal = 0))
    private void offgrid$submitSecondHalf(SubmitNodeCollector collector, PoseStack poseStack, MovingBlockRenderState block, int order, Operation<Void> original, @Local(argsOnly = true) PistonHeadRenderState state) {
        original.call(collector, poseStack, block, order);
        MovingPistonParts parts = (MovingPistonParts) state;
        MovingBlockRenderState second = parts.offgrid$getSecondHalf();
        if (second != null) {
            original.call(collector, poseStack, second, order);
        }
        for (MovingOverlayPart overlay : parts.offgrid$getOverlays()) {
            poseStack.pushPose();
            poseStack.translate(overlay.offset().x, overlay.offset().y, overlay.offset().z);
            original.call(collector, poseStack, overlay.block(), order);
            poseStack.popPose();
        }
    }
    ^///?} else {
    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitMovingBlock(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/MovingBlockRenderState;)V", ordinal = 0))
    private void offgrid$submitSecondHalf(SubmitNodeCollector collector, PoseStack poseStack, MovingBlockRenderState block, Operation<Void> original, @Local(argsOnly = true) PistonHeadRenderState state) {
        original.call(collector, poseStack, block);
        MovingPistonParts parts = (MovingPistonParts) state;
        MovingBlockRenderState second = parts.offgrid$getSecondHalf();
        if (second != null) {
            original.call(collector, poseStack, second);
        }
        for (MovingOverlayPart overlay : parts.offgrid$getOverlays()) {
            poseStack.pushPose();
            poseStack.translate(overlay.offset().x, overlay.offset().y, overlay.offset().z);
            original.call(collector, poseStack, overlay.block());
            poseStack.popPose();
        }
    }
    //?}
    *///?} else {
    @WrapOperation(method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;renderBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;ZI)V"))
    private void offgrid$renderMixedSlab(PistonHeadRenderer renderer, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource buffers, Level level, boolean extended, int overlay, Operation<Void> original, @Local(argsOnly = true) PistonMovingBlockEntity piston, @Local(argsOnly = true) float partialTick) {
        MovingBlockData data = ((MovingBlockDataHolder) piston).offgrid$getMovingBlockData();
        if (data == null || !data.hasHalves() || !(state.getBlock() instanceof MixedSlabBlock)) {
            original.call(renderer, pos, state, poseStack, buffers, level, extended, overlay);
        } else {
            original.call(renderer, pos, data.first(), poseStack, buffers, level, extended, overlay);
            original.call(renderer, pos, data.second(), poseStack, buffers, level, extended, overlay);
        }
        if (data == null || data.cell() == null || state != piston.getMovedState()) return;
        int light = LevelRenderer.getLightColor(level, pos);
        for (AssembledCell.AssembledOverlay carried : data.cell().overlays()) {
            AssembledOverlayRender.render(level, pos, state, carried, ((MovingBlockDataHolder) piston).offgrid$getRenderedEntities(), poseStack, buffers, light, partialTick);
        }
    }
    //?}
}
