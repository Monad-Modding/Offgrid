package dev.tazer.offgrid.mixin.entity;

import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if <1.21 {
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {

    @Shadow
    private boolean fixed;

    @Inject(method = "survives", at = @At("HEAD"), cancellable = true)
    private void offgrid$surviveOnSurface(CallbackInfoReturnable<Boolean> cir) {
        if (this.fixed) return;
        Boolean survives = SurfaceMounts.survivesOverride((ItemFrame) (Object) this);
        if (survives != null) {
            cir.setReturnValue(survives);
        }
    }
    //? if <1.21 {

    /*@Inject(method = "recalculateBoundingBox", at = @At("TAIL"))
    private void offgrid$mountOnSurface(CallbackInfo ci) {
        SurfaceMounts.shift((ItemFrame) (Object) this);
    }
    *///?}
}
