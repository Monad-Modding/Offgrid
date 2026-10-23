package dev.tazer.offgrid.forge.mixin;

import dev.tazer.offgrid.client.OverlayBreaking;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "addBlockHitEffects", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$skipHostHitEffects(BlockPos pos, BlockHitResult target, CallbackInfo ci) {
        if (OverlayBreaking.isAimingAt(pos)) {
            ci.cancel();
        }
    }
}
