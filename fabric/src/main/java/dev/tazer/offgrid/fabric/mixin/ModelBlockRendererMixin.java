//? if <26 {
package dev.tazer.offgrid.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.fabric.client.FabricMixedSlabModel;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    @Shadow
    public abstract void tesselateWithAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay);

    @Shadow
    public abstract void tesselateWithoutAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay);

    @Inject(method = "tesselateWithAO", at = @At("HEAD"), cancellable = true)
    private void offgrid$tesselateHalvesWithAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, CallbackInfo ci) {
        if (!(model instanceof FabricMixedSlabModel)) return;
        ci.cancel();
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return;
        this.tesselateWithAO(level, offgrid$model(entity.getFirst()), entity.getFirst(), pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
        this.tesselateWithAO(level, offgrid$model(entity.getSecond()), entity.getSecond(), pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
    }

    @Inject(method = "tesselateWithoutAO", at = @At("HEAD"), cancellable = true)
    private void offgrid$tesselateHalvesWithoutAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, CallbackInfo ci) {
        if (!(model instanceof FabricMixedSlabModel)) return;
        ci.cancel();
        if (!(level.getBlockEntity(pos) instanceof MixedSlabBlockEntity entity) || !entity.isComplete()) return;
        this.tesselateWithoutAO(level, offgrid$model(entity.getFirst()), entity.getFirst(), pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
        this.tesselateWithoutAO(level, offgrid$model(entity.getSecond()), entity.getSecond(), pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
    }

    private static BakedModel offgrid$model(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }
}
//?}
