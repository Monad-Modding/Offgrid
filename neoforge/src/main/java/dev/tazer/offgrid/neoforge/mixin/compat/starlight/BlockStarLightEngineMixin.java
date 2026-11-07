package dev.tazer.offgrid.neoforge.mixin.compat.starlight;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.compat.light.OverlayLight;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Pseudo
@IfModPresent("scalablelux")
@Mixin(BlockStarLightEngine.class)
public abstract class BlockStarLightEngineMixin {

    @ModifyReturnValue(method = "getSources", at = @At("RETURN"))
    private List<BlockPos> offgrid$overlaySources(List<BlockPos> sources, LightChunkGetter lightAccess, ChunkAccess chunk) {
        return OverlayLight.withSources(sources, chunk);
    }

    @WrapOperation(method = {"checkBlock", "calculateLightValue", "lightChunk"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
    private int offgrid$overlayEmission(BlockState state, BlockGetter level, BlockPos pos, Operation<Integer> original) {
        return OverlayLight.emission(level, pos, original.call(state, level, pos));
    }
}
