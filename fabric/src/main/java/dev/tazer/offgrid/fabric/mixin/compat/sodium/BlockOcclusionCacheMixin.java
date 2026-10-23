//? if <1.21 {
/*package dev.tazer.offgrid.fabric.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Connections;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("sodium")
@Mixin(BlockOcclusionCache.class)
public abstract class BlockOcclusionCacheMixin {

    @WrapOperation(method = "shouldDrawSide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState offgrid$cullAgainstBlocklogged(BlockGetter level, BlockPos neighbour, Operation<BlockState> original, @Local(argsOnly = true) BlockState state) {
        return Connections.forCulling(state, level, neighbour, original.call(level, neighbour));
    }
}
*///?}
