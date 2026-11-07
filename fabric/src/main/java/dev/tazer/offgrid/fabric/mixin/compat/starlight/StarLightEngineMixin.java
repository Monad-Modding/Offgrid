package dev.tazer.offgrid.fabric.mixin.compat.starlight;

import ca.spottedleaf.starlight.common.light.StarLightEngine;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.compat.light.OverlayLight;
import dev.tazer.offgrid.platform.condition.IfAnyModPresent;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfAnyModPresent({"starlight", "scalablelux"})
@Mixin(StarLightEngine.class)
public abstract class StarLightEngineMixin {

    @ModifyExpressionValue(method = "performLightDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int offgrid$overlayEmission(int original, @Local(argsOnly = true) LightChunkGetter lightAccess, @Local(name = "offX") int x, @Local(name = "offY") int y, @Local(name = "offZ") int z) {
        return OverlayLight.emission(lightAccess.getLevel(), x, y, z, original);
    }
}
