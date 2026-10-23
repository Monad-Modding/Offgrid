package dev.tazer.offgrid.client;

import dev.tazer.offgrid.mixin.client.ClientLevelAccessor;
import dev.tazer.offgrid.mixin.client.MultiPlayerGameModeAccessor;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.UseOverlay;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayHitResult;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public final class ClientInteractions {

    private ClientInteractions() {
    }

    public static InteractionResult use(MultiPlayerGameMode gameMode, LocalPlayer localPlayer, InteractionHand hand, OverlayHitResult hit) {
        ClientLevel level = (ClientLevel) localPlayer.level();
        BlockPos host = hit.getBlockPos();
        Overlay overlay = Cells.find(level, host, hit.getOverlayId());
        if (overlay == null) return InteractionResult.PASS;
        MultiPlayerGameModeAccessor accessor = (MultiPlayerGameModeAccessor) gameMode;
        accessor.offgrid$ensureHasSentCarriedItem();
        try (BlockStatePredictionHandler prediction = ((ClientLevelAccessor) level).offgrid$getBlockStatePredictionHandler().startPredicting()) {
            int sequence = prediction.currentSequence();
            InteractionResult result = OverlayContext.call(level, host, overlay, () -> accessor.offgrid$performUseItemOn(localPlayer, hand, hit));
            OffgridNetwork.sendToServer(OffgridNetwork.USE, new UseOverlay(overlay.getId(), hand, hit, sequence));
            return result;
        }
    }
}
