package dev.tazer.offgrid.forge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.client.MixedSlabCracks;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

    @ModifyVariable(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraftforge/client/model/data/ModelData;)V", at = @At("HEAD"), argsOnly = true, remap = false)
    private BlockState offgrid$crackOneHalf(BlockState state, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockAndTintGetter level) {
        return MixedSlabCracks.half(state, level, pos);
    }
}
