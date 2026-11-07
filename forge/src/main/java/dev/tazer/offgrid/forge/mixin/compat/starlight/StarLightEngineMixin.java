package dev.tazer.offgrid.forge.mixin.compat.starlight;

import ca.spottedleaf.starlight.common.light.StarLightEngine;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.compat.light.OverlayLight;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("starlight")
@Mixin(value = StarLightEngine.class, remap = false)
public abstract class StarLightEngineMixin {

    @WrapOperation(method = "performLightDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
    private int offgrid$overlayEmission(BlockState state, BlockGetter level, BlockPos pos, Operation<Integer> original) {
        return OverlayLight.emission(level, pos, original.call(state, level, pos));
    }

    @ModifyExpressionValue(method = "performLightDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I", remap = true))
    private int offgrid$overlayCachedEmission(int original, @Local(argsOnly = true) LightChunkGetter lightAccess, @Local(name = "offX") int x, @Local(name = "offY") int y, @Local(name = "offZ") int z) {
        return OverlayLight.emission(lightAccess.getLevel(), x, y, z, original);
    }
}
