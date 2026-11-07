package dev.tazer.offgrid.fabric.mixin.compat.starlight;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.compat.light.OverlayLight;
import dev.tazer.offgrid.platform.condition.IfAnyModPresent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Pseudo
@IfAnyModPresent({"starlight", "scalablelux"})
@Mixin(BlockStarLightEngine.class)
public abstract class BlockStarLightEngineMixin {

    @ModifyReturnValue(method = "getSources", at = @At("RETURN"))
    private List<BlockPos> offgrid$overlaySources(List<BlockPos> sources, LightChunkGetter lightAccess, ChunkAccess chunk) {
        return OverlayLight.withSources(sources, chunk);
    }

    @ModifyExpressionValue(method = {"checkBlock", "calculateLightValue"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int offgrid$overlayEmission(int original, @Local(argsOnly = true) LightChunkGetter lightAccess, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        return OverlayLight.emission(lightAccess.getLevel(), x, y, z, original);
    }

    @ModifyExpressionValue(method = "lightChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int offgrid$overlayChunkEmission(int original, @Local(argsOnly = true) LightChunkGetter lightAccess, @Local BlockPos pos) {
        return OverlayLight.emission(lightAccess.getLevel(), pos, original);
    }
}
