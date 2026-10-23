//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.client.FlatPaintingRender;
import dev.tazer.offgrid.client.FlatPaintingState;
import dev.tazer.offgrid.world.FlatPaintings;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.world.entity.decoration.painting.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
package dev.tazer.offgrid.mixin.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.client.FlatPaintingRender;
import dev.tazer.offgrid.world.FlatPaintings;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.world.entity.decoration.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
//? if >=26.3 {
/*import com.mojang.math.Axis;
import net.minecraft.util.LightCoordsUtil;
*///?} else if >=26.2 {
/*import net.minecraft.util.LightCoordsUtil;
import org.joml.Quaternionfc;
*///?} else if >=26 {
/*import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Quaternionfc;
*///?}

@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin {

    //? if >=26 {
    /*@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/painting/Painting;Lnet/minecraft/client/renderer/entity/state/PaintingRenderState;F)V", at = @At("TAIL"))
    private void offgrid$extractFlat(Painting painting, PaintingRenderState state, float partialTicks, CallbackInfo ci) {
        if (!FlatPaintings.isFlat(painting) || state.variant == null) return;
        ((FlatPaintingState) state).offgrid$setTop(FlatPaintings.top(painting));
        int width = state.variant.width();
        int height = state.variant.height();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                //? if >=26.2 {
                /^state.lightCoordsPerBlock[column + row * width] = LightCoordsUtil.getLightCoords(painting.level(), FlatPaintings.lightPos(painting, width, height, column, row));
                ^///?} else {
                state.lightCoordsPerBlock[column + row * width] = LevelRenderer.getLightCoords(painting.level(), FlatPaintings.lightPos(painting, width, height, column, row));
                //?}
            }
        }
    }

    //? if >=26.3 {
    /^@WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/PaintingRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V"))
    private void offgrid$layFlat(PoseStack poseStack, Axis axis, float degrees, Operation<Void> original, @Local(argsOnly = true) PaintingRenderState state) {
        if (state.direction != null && state.direction.getAxis().isVertical()) {
            FlatPaintingRender.rotate(poseStack, state.direction, ((FlatPaintingState) state).offgrid$getTop());
            return;
        }
        original.call(poseStack, axis, degrees);
    }
    ^///?} else {
    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/PaintingRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"))
    private void offgrid$layFlat(PoseStack poseStack, Quaternionfc rotation, Operation<Void> original, @Local(argsOnly = true) PaintingRenderState state) {
        if (state.direction != null && state.direction.getAxis().isVertical()) {
            FlatPaintingRender.rotate(poseStack, state.direction, ((FlatPaintingState) state).offgrid$getTop());
            return;
        }
        original.call(poseStack, rotation);
    }
    //?}
    *///?} else {
    @Inject(method = "render(Lnet/minecraft/world/entity/decoration/Painting;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void offgrid$renderFlat(Painting painting, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (FlatPaintings.isFlat(painting)) {
            FlatPaintingRender.render(painting, poseStack, buffers);
            ci.cancel();
        }
    }
    //?}
}
