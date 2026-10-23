package dev.tazer.offgrid.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.client.ClientInteractions;
import dev.tazer.offgrid.client.OverlayBreaking;
import dev.tazer.offgrid.world.OverlayHitResult;
import dev.tazer.offgrid.world.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21.4 {
/*import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.PickOverlay;
*///?}

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void offgrid$startOverlayDestroy(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.hitResult instanceof OverlayHitResult hit && hit.getBlockPos().equals(pos)) {
            cir.setReturnValue(OverlayBreaking.start(hit));
        } else {
            OverlayBreaking.stop();
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void offgrid$continueOverlayDestroy(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.hitResult instanceof OverlayHitResult hit && hit.getBlockPos().equals(pos)) {
            cir.setReturnValue(OverlayBreaking.tick(hit));
        } else {
            OverlayBreaking.stop();
        }
    }

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    private void offgrid$stopOverlayDestroy(CallbackInfo ci) {
        OverlayBreaking.stop();
    }

    //? if >=1.21.4 {
    /*@Inject(method = "handlePickItemFromBlock", at = @At("HEAD"), cancellable = true)
    private void offgrid$pickOverlay(BlockPos pos, boolean includeData, CallbackInfo ci) {
        if (this.minecraft.hitResult instanceof OverlayHitResult hit && hit.getBlockPos().equals(pos)) {
            OffgridNetwork.sendToServer(OffgridNetwork.PICK, new PickOverlay(pos.asLong(), hit.getOverlayId(), includeData));
            ci.cancel();
        }
    }

    *///?}
    @WrapMethod(method = "performUseItemOn")
    private InteractionResult offgrid$transactUse(LocalPlayer player, InteractionHand hand, BlockHitResult hit, Operation<InteractionResult> original) {
        return Transaction.run(player, () -> original.call(player, hand, hit));
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void offgrid$useOverlay(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (hit instanceof OverlayHitResult overlayHit) {
            cir.setReturnValue(ClientInteractions.use((MultiPlayerGameMode) (Object) this, player, hand, overlayHit));
        }
    }
}
