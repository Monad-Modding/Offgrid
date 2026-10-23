package dev.tazer.offgrid.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=26 {
/*import dev.tazer.offgrid.client.OverlayBlockEntities;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
*///?} else {
import dev.tazer.offgrid.world.Offsets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
//?}

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    //? if >=26 {
    /*@WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private <S extends BlockEntityRenderState> void offgrid$offsetBlockEntity(BlockEntityRenderer<?, S> renderer, S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Operation<Void> original) {
        Vec3 offset = OverlayBlockEntities.offsetOf(state);
        if (offset == null) {
            original.call(renderer, state, poseStack, collector, camera);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        original.call(renderer, state, poseStack, collector, camera);
        poseStack.popPose();
    }
    *///?} else {
    @WrapOperation(method = "setupAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private static <T extends BlockEntity> void offgrid$offsetBlockEntity(BlockEntityRenderer<T> renderer, T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, Operation<Void> original) {
        Vec3 offset = blockEntity.getLevel() == null ? null : Offsets.of(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
        if (offset == null) {
            original.call(renderer, blockEntity, partialTick, poseStack, buffers, light, overlay);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        Offsets.suppress(() -> original.call(renderer, blockEntity, partialTick, poseStack, buffers, light, overlay));
        poseStack.popPose();
    }
    //?}
}
