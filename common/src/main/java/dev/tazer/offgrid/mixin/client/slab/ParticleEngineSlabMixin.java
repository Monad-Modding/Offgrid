//? if <26 {
package dev.tazer.offgrid.mixin.client.slab;

import dev.tazer.offgrid.client.MixedSlabParticles;
import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabBlockEntity;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineSlabMixin {

    @Shadow
    protected ClientLevel level;

    @Shadow
    public abstract void destroy(BlockPos pos, BlockState state);

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void offgrid$destroyBothHalves(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!(state.getBlock() instanceof MixedSlabBlock)) return;
        MixedSlabBlockEntity entity = MixedSlabs.entity(this.level, pos);
        if (entity == null) return;
        ci.cancel();
        this.destroy(pos, entity.getFirst());
        this.destroy(pos, entity.getSecond());
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void offgrid$crackTargetedHalf(BlockPos pos, Direction side, CallbackInfo ci) {
        if (MixedSlabParticles.crack(this.level, pos, side)) {
            ci.cancel();
        }
    }
}
//?}
