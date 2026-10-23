//? if >=26 {
/*package dev.tazer.offgrid.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.world.Connections;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public abstract class FaceCullingMixin {

    @WrapOperation(method = "shouldRenderFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState offgrid$cullAgainstBlocklogged(BlockAndTintGetter level, BlockPos neighbour, Operation<BlockState> original, @Local(argsOnly = true) BlockState state) {
        return Connections.forCulling(state, level, neighbour, original.call(level, neighbour));
    }
}
*///?} else {
package dev.tazer.offgrid.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.world.Connections;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class FaceCullingMixin {

    @WrapOperation(method = "shouldRenderFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState offgrid$cullAgainstBlocklogged(BlockGetter level, BlockPos neighbour, Operation<BlockState> original, @Local(argsOnly = true) BlockState state) {
        return Connections.forCulling(state, level, neighbour, original.call(level, neighbour));
    }
}
//?}
