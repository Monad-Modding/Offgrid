package dev.tazer.offgrid.mixin.server;

import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Interactions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
//? if >=26.3 {
/*import net.minecraft.world.level.block.entity.SignTextSlot;
*///?}

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "updateSignText", at = @At("HEAD"), cancellable = true)
    private void offgrid$updateOverlaySign(ServerboundSignUpdatePacket packet, List<FilteredText> lines, CallbackInfo ci) {
        //? if >=26.3 {
        /*boolean handled = Interactions.updateSign(this.player, packet.pos(), packet.slot() == SignTextSlot.FRONT, lines);
        *///?} else {
        boolean handled = Interactions.updateSign(this.player, packet.getPos(), packet.isFrontText(), lines);
        //?}
        if (handled) {
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At("RETURN"))
    private void offgrid$resyncUsedCells(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        //? if >=26.3 {
        /*BlockHitResult hit = packet.hitResult();
        *///?} else {
        BlockHitResult hit = packet.getHitResult();
        //?}
        BlockPos pos = hit.getBlockPos();
        Cells.sendTo(this.player, this.player.serverLevel(), pos);
        Cells.sendTo(this.player, this.player.serverLevel(), pos.relative(hit.getDirection()));
    }
}
