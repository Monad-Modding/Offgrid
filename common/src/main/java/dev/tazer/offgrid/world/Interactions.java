package dev.tazer.offgrid.world;

import dev.tazer.offgrid.mixin.access.ServerGamePacketListenerImplAccessor;
import dev.tazer.offgrid.network.BreakOverlay;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.OpenOverlaySign;
import dev.tazer.offgrid.network.OverlayDestruction;
import dev.tazer.offgrid.network.PickOverlay;
import dev.tazer.offgrid.network.UseOverlay;
//? if >=1.21 {
import net.minecraft.advancements.CriteriaTriggers;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.4 {
/*import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
*///?}
//? if >=26.3 {
/*import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.entity.SignTextSlot;
*///?}

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Interactions {

    private static final double REACH_BUFFER = 2.0;
    private static final double HIT_TOLERANCE = 1.0000001;
    private static final double DESTRUCTION_RANGE_SQR = 1024.0;
    private static final Map<UUID, BreakSession> BREAKING = new HashMap<>();
    private static final Map<UUID, SignEdit> SIGN_EDITS = new HashMap<>();

    private Interactions() {
    }

    public static void use(UseOverlay packet, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        player.connection.ackBlockChangesUpTo(packet.sequence());
        BlockHitResult hit = packet.hit();
        BlockPos host = hit.getBlockPos();
        if (!withinReach(player, host)) return;
        Overlay overlay = Cells.find(level, host, packet.id());
        ItemStack stack = player.getItemInHand(packet.hand());
        if (overlay != null && hits(hit, host, overlay) && stack.isItemEnabled(level.enabledFeatures()) && !level.isOutsideBuildHeight(host) && ((ServerGamePacketListenerImplAccessor) player.connection).offgrid$getAwaitingPositionFromClient() == null && level.mayInteract(player, host)) {
            player.resetLastActionTime();
            //? if >=26.3 {
            /*SwingAnimation animation = stack.getInteractAnimation();
            *///?}
            InteractionResult result = OverlayContext.call(level, host, overlay, () -> player.gameMode.useItemOn(player, level, stack, packet.hand(), hit));
            //? if >=1.21 {
            if (result.consumesAction()) {
                CriteriaTriggers.ANY_BLOCK_USE.trigger(player, host, stack.copy());
            }
            //?}
            //? if >=26.3 {
            /*if (result instanceof InteractionResult.Success success && success.shouldSwing()) {
                player.swingAndResetAttackStrength(packet.hand(), animation, success.swingSource() != InteractionResult.SwingSource.PREDICTED);
            }
            *///?} else if >=1.21.2 {
            /*if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.SERVER) {
                player.swing(packet.hand(), true);
            }
            *///?} else {
            if (result.shouldSwing()) {
                player.swing(packet.hand(), true);
            }
            //?}
        }
        resync(player, level, host);
        resync(player, level, host.relative(hit.getDirection()));
    }

    public static void breakOverlay(BreakOverlay packet, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos host = BlockPos.of(packet.pos());
        Overlay overlay = Cells.find(level, host, packet.id());
        switch (packet.phase()) {
            case START -> {
                if (!withinReach(player, host)) return;
                if (overlay == null || !level.mayInteract(player, host)) {
                    resync(player, level, host);
                    return;
                }
                clearDestruction(BREAKING.put(player.getUUID(), new BreakSession(level, player.getId(), packet.pos(), packet.id(), level.getGameTime(), -1)));
                OverlayContext.run(level, host, overlay, () -> overlay.getState().attack(level, host, player));
            }
            case ABORT -> clearDestruction(BREAKING.remove(player.getUUID()));
            case FINISH -> {
                BreakSession session = BREAKING.remove(player.getUUID());
                clearDestruction(session);
                if (!withinReach(player, host)) return;
                if (overlay == null || !level.mayInteract(player, host) || !player.isCreative() && !brokeInTime(level, host, overlay, player, session, packet)) {
                    resync(player, level, host);
                    return;
                }
                boolean destroyed = OverlayContext.call(level, host, overlay, () -> player.gameMode.destroyBlock(host));
                if (!destroyed) {
                    resync(player, level, host);
                }
            }
        }
    }

    public static void pick(PickOverlay packet, ServerPlayer player) {
        //? if >=1.21.4 {
        /*ServerLevel level = player.serverLevel();
        BlockPos host = BlockPos.of(packet.pos());
        if (!withinReach(player, host)) return;
        Overlay overlay = Cells.find(level, host, packet.id());
        if (overlay == null) return;
        overlay.materialize(level, host);
        OverlayContext.run(level, host, overlay, () -> player.connection.handlePickItemFromBlock(new ServerboundPickItemFromBlockPacket(host, packet.includeData())));
        *///?}
    }

    public static boolean openSign(ServerPlayer player, SignBlockEntity sign, boolean front) {
        OverlayContext.Frame frame = OverlayContext.at(player.serverLevel(), sign.getBlockPos());
        if (frame == null || frame.getOverlay() == null || frame.getOverlay().getBlockEntity() != sign) return false;
        SIGN_EDITS.put(player.getUUID(), new SignEdit(sign.getBlockPos().asLong(), frame.getOverlay().getId()));
        OffgridNetwork.sendToPlayer(player, OffgridNetwork.OPEN_SIGN, new OpenOverlaySign(sign.getBlockPos().asLong(), frame.getOverlay().getId(), front));
        return true;
    }

    public static boolean updateSign(ServerPlayer player, BlockPos pos, boolean front, List<FilteredText> lines) {
        SignEdit edit = SIGN_EDITS.get(player.getUUID());
        if (edit == null || edit.pos() != pos.asLong()) return false;
        SIGN_EDITS.remove(player.getUUID());
        ServerLevel level = player.serverLevel();
        Overlay overlay = Cells.find(level, pos, edit.id());
        if (overlay == null) return true;
        BlockEntity blockEntity = overlay.materialize(level, pos);
        if (blockEntity instanceof SignBlockEntity sign) {
            //? if >=26.3 {
            /*OverlayContext.run(level, pos, overlay, () -> sign.updateSignText(player, front ? SignTextSlot.FRONT : SignTextSlot.BACK, lines));
            *///?} else {
            OverlayContext.run(level, pos, overlay, () -> sign.updateSignText(player, front, lines));
            //?}
        }
        return true;
    }

    public static void tickDestruction(ServerLevel level) {
        if (BREAKING.isEmpty()) return;
        Iterator<Map.Entry<UUID, BreakSession>> iterator = BREAKING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BreakSession> entry = iterator.next();
            BreakSession session = entry.getValue();
            if (session.level() != level) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            BlockPos host = BlockPos.of(session.pos());
            Overlay overlay = Cells.find(level, host, session.id());
            if (player == null || player.serverLevel() != level || overlay == null) {
                iterator.remove();
                clearDestruction(session);
                continue;
            }
            float perTick = OverlayContext.call(level, host, overlay, () -> overlay.getState().getDestroyProgress(player, level, host));
            int stage = Math.min(9, (int) (perTick * (level.getGameTime() - session.startedAt() + 1) * 10.0F));
            if (stage != session.stage()) {
                entry.setValue(session.withStage(stage));
                sendDestruction(level, session.breaker(), host, session.id(), stage);
            }
        }
    }

    public static void forget(ServerPlayer player) {
        clearDestruction(BREAKING.remove(player.getUUID()));
        SIGN_EDITS.remove(player.getUUID());
    }

    private static boolean withinReach(ServerPlayer player, BlockPos host) {
        return Players.canReach(player, host, REACH_BUFFER);
    }

    private static boolean hits(BlockHitResult hit, BlockPos host, Overlay overlay) {
        Vec3 relative = hit.getLocation().subtract(Vec3.atCenterOf(host)).subtract(overlay.getOffset());
        return Math.abs(relative.x) < HIT_TOLERANCE && Math.abs(relative.y) < HIT_TOLERANCE && Math.abs(relative.z) < HIT_TOLERANCE;
    }

    private static boolean brokeInTime(ServerLevel level, BlockPos host, Overlay overlay, ServerPlayer player, @Nullable BreakSession session, BreakOverlay packet) {
        float perTick = OverlayContext.call(level, host, overlay, () -> overlay.getState().getDestroyProgress(player, level, host));
        if (perTick <= 0.0F) return false;
        if (perTick >= 1.0F) return true;
        if (session == null || session.pos() != packet.pos() || session.id() != packet.id()) return false;
        long elapsed = level.getGameTime() - session.startedAt();
        int expected = (int) Math.ceil(1.0 / perTick);
        return elapsed + 3 >= expected * 0.7;
    }

    private static void clearDestruction(@Nullable BreakSession session) {
        if (session != null && session.stage() >= 0) {
            sendDestruction(session.level(), session.breaker(), BlockPos.of(session.pos()), session.id(), -1);
        }
    }

    private static void sendDestruction(ServerLevel level, int breaker, BlockPos host, int id, int stage) {
        OverlayDestruction packet = new OverlayDestruction(breaker, host.asLong(), id, stage);
        for (ServerPlayer viewer : Cells.playersTracking(level, host)) {
            if (viewer.getId() == breaker) {
                continue;
            }
            if (SubLevelSpace.distanceSqr(level, host, viewer.position()) < DESTRUCTION_RANGE_SQR) {
                OffgridNetwork.sendToPlayer(viewer, OffgridNetwork.DESTRUCTION, packet);
            }
        }
    }

    private static void resync(ServerPlayer player, ServerLevel level, BlockPos pos) {
        player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
        Cells.sendTo(player, level, pos);
    }

    private record BreakSession(ServerLevel level, int breaker, long pos, int id, long startedAt, int stage) {

        private BreakSession withStage(int stage) {
            return new BreakSession(this.level, this.breaker, this.pos, this.id, this.startedAt, stage);
        }
    }

    private record SignEdit(long pos, int id) {
    }
}
