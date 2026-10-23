package dev.tazer.offgrid.mixin.server;

import dev.tazer.offgrid.world.Interactions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.3 {
/*import net.minecraft.world.level.block.entity.SignTextSlot;
*///?}

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "openTextEdit", at = @At("HEAD"), cancellable = true)
    //? if >=26.3 {
    /*private void offgrid$openOverlaySign(SignBlockEntity sign, SignTextSlot slot, CallbackInfo ci) {
        boolean front = slot == SignTextSlot.FRONT;
    *///?} else {
    private void offgrid$openOverlaySign(SignBlockEntity sign, boolean front, CallbackInfo ci) {
    //?}
        if (Interactions.openSign((ServerPlayer) (Object) this, sign, front)) {
            ci.cancel();
        }
    }
}
