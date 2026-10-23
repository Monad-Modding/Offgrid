package dev.tazer.offgrid.mixin.placement;

import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelPlacementMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void offgrid$mountPlacedEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        SurfaceMounts.mountPlaced(entity);
    }
}
