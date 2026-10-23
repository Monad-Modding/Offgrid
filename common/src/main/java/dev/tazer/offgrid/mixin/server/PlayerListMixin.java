package dev.tazer.offgrid.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.OffgridSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void offgrid$sendSettings(CallbackInfo ci, @Local(argsOnly = true) ServerPlayer player) {
        OffgridSettings.sendTo(player);
    }
}
