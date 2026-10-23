//? if <26 {
package dev.tazer.offgrid.mixin.client;

import dev.tazer.offgrid.client.OverlayBreaking;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void offgrid$skipHostCrack(BlockPos pos, Direction side, CallbackInfo ci) {
        if (OverlayBreaking.isAimingAt(pos)) {
            ci.cancel();
        }
    }
}
//?}
