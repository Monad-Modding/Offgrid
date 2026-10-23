//? if <1.21 {
/*package dev.tazer.offgrid.mixin.entity;

import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {

    @Inject(method = "recalculateBoundingBox", at = @At("TAIL"))
    private void offgrid$mountOnSurface(CallbackInfo ci) {
        SurfaceMounts.shift((ItemFrame) (Object) this);
    }
}
*///?}
