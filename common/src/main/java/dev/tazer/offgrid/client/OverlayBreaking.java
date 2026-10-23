package dev.tazer.offgrid.client;

import dev.tazer.offgrid.network.BreakOverlay;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayHitResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class OverlayBreaking {

    private static final int DESTROY_DELAY = 5;

    @Nullable
    private static OverlayHitResult target;
    private static float progress;
    private static int delay;

    private OverlayBreaking() {
    }

    @Nullable
    public static OverlayHitResult getTarget() {
        return target;
    }

    public static int getStage() {
        if (target == null || progress <= 0.0F) return -1;
        return Math.min(9, (int) (progress * 10.0F));
    }

    public static boolean isAimingAt(BlockPos host) {
        return Minecraft.getInstance().hitResult instanceof OverlayHitResult hit && hit.getBlockPos().equals(host);
    }

    public static boolean isTarget(BlockPos host, int id) {
        return target != null && target.getBlockPos().equals(host) && target.getOverlayId() == id;
    }

    public static boolean start(OverlayHitResult hit) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.gameMode == null) return false;
        BlockPos host = hit.getBlockPos();
        Overlay overlay = Cells.find(level, host, hit.getOverlayId());
        if (overlay == null || !mayBreak(minecraft, level, player, host, overlay)) return false;
        if (player.getAbilities().instabuild) {
            send(host, overlay, BreakOverlay.Phase.FINISH);
            destroy(minecraft, level, host, overlay);
            delay = DESTROY_DELAY;
            return true;
        }
        if (isTarget(host, overlay.getId())) return true;
        stop();
        target = hit;
        progress = 0.0F;
        send(host, overlay, BreakOverlay.Phase.START);
        OverlayContext.run(level, host, overlay, () -> overlay.getState().attack(level, host, player));
        float perTick = OverlayContext.call(level, host, overlay, () -> overlay.getState().getDestroyProgress(player, level, host));
        if (perTick >= 1.0F) {
            send(host, overlay, BreakOverlay.Phase.FINISH);
            destroy(minecraft, level, host, overlay);
            target = null;
            progress = 0.0F;
        }
        return true;
    }

    public static boolean tick(OverlayHitResult hit) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) return false;
        if (delay > 0) {
            delay--;
            return true;
        }
        if (player.getAbilities().instabuild) return start(hit);
        if (!isTarget(hit.getBlockPos(), hit.getOverlayId())) return start(hit);
        BlockPos host = hit.getBlockPos();
        Overlay overlay = Cells.find(level, host, hit.getOverlayId());
        if (overlay == null) {
            target = null;
            progress = 0.0F;
            return false;
        }
        progress += OverlayContext.call(level, host, overlay, () -> overlay.getState().getDestroyProgress(player, level, host));
        OverlayEffects.hit(level, host, overlay, hit.getDirection());
        if (progress >= 1.0F) {
            send(host, overlay, BreakOverlay.Phase.FINISH);
            destroy(minecraft, level, host, overlay);
            target = null;
            progress = 0.0F;
            delay = DESTROY_DELAY;
        }
        return true;
    }

    public static void stop() {
        if (target == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            OffgridNetwork.sendToServer(OffgridNetwork.BREAK, new BreakOverlay(target.getBlockPos().asLong(), target.getOverlayId(), BreakOverlay.Phase.ABORT));
        }
        target = null;
        progress = 0.0F;
    }

    private static boolean mayBreak(Minecraft minecraft, ClientLevel level, LocalPlayer player, BlockPos host, Overlay overlay) {
        if (player.blockActionRestricted(level, host, minecraft.gameMode.getPlayerMode())) return false;
        //? if >=1.21.5 {
        /*return OverlayContext.call(level, host, overlay, () -> player.getMainHandItem().canDestroyBlock(overlay.getState(), level, host, player));
        *///?} else {
        return OverlayContext.call(level, host, overlay, () -> player.getMainHandItem().getItem().canAttackBlock(overlay.getState(), level, host, player));
        //?}
    }

    private static void destroy(Minecraft minecraft, ClientLevel level, BlockPos host, Overlay overlay) {
        OverlayContext.run(level, host, overlay, () -> minecraft.gameMode.destroyBlock(host));
    }

    private static void send(BlockPos host, Overlay overlay, BreakOverlay.Phase phase) {
        OffgridNetwork.sendToServer(OffgridNetwork.BREAK, new BreakOverlay(host.asLong(), overlay.getId(), phase));
    }
}
