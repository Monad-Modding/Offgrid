package dev.tazer.offgrid.mixin.world;

import dev.tazer.offgrid.world.Overlays;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.2 {
/*import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;

import java.util.List;
*///?} else {
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
//?}

//? if >=1.21.2 {
/*@Mixin(ServerExplosion.class)
*///?} else {
@Mixin(Explosion.class)
//?}
public abstract class ExplosionMixin {

    //? if >=1.21.2 {
    /*@Shadow
    @Final
    private ServerLevel level;

    @Inject(method = "interactWithBlocks", at = @At("HEAD"))
    private void offgrid$explodeOverlays(List<BlockPos> blown, CallbackInfo ci) {
        Overlays.explode(this.level, blown);
    }
    *///?} else {
    @Shadow
    @Final
    private Level level;

    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    private void offgrid$explodeOverlays(boolean spawnParticles, CallbackInfo ci) {
        Explosion explosion = (Explosion) (Object) this;
        if (this.level instanceof ServerLevel server && explosion.interactsWithBlocks()) {
            Overlays.explode(server, explosion.getToBlow());
        }
    }
    //?}
}
