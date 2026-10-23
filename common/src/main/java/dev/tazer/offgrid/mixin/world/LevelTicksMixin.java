package dev.tazer.offgrid.mixin.world;

import dev.tazer.offgrid.world.OverlayTicker;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin {

    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    private void offgrid$overlayTick(ScheduledTick<?> tick, CallbackInfo ci) {
        if (OverlayTicker.reroute((LevelTicks<?>) (Object) this, tick)) {
            ci.cancel();
        }
    }
}
