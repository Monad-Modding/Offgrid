package dev.tazer.offgrid.network;

import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.world.Interactions;
import net.minecraft.server.level.ServerPlayer;
//? if <26 {
import dev.tazer.offgrid.compat.create.CarriedOverlays;
//?}

import java.util.List;

public final class OffgridNetwork {

    public static final PacketType<ChunkSync> CHUNK = PacketType.clientbound("chunk", ChunkSync.class, ChunkSync::write, ChunkSync::read);
    public static final PacketType<CellSync> CELL = PacketType.clientbound("cell", CellSync.class, CellSync::write, CellSync::read);
    public static final PacketType<OverlayEvent> EVENT = PacketType.clientbound("event", OverlayEvent.class, OverlayEvent::write, OverlayEvent::read);
    public static final PacketType<OverlayDestruction> DESTRUCTION = PacketType.clientbound("destruction", OverlayDestruction.class, OverlayDestruction::write, OverlayDestruction::read);
    public static final PacketType<OpenOverlaySign> OPEN_SIGN = PacketType.clientbound("open_sign", OpenOverlaySign.class, OpenOverlaySign::write, OpenOverlaySign::read);
    public static final PacketType<SurfaceOffsetSync> SURFACE = PacketType.clientbound("surface", SurfaceOffsetSync.class, SurfaceOffsetSync::write, SurfaceOffsetSync::read);
    public static final PacketType<OffgridSettings> SETTINGS = PacketType.clientbound("settings", OffgridSettings.class, OffgridSettings::write, OffgridSettings::read);
    public static final PacketType<UseOverlay> USE = PacketType.serverbound("use", UseOverlay.class, UseOverlay::write, UseOverlay::read, Interactions::use);
    public static final PacketType<BreakOverlay> BREAK = PacketType.serverbound("break", BreakOverlay.class, BreakOverlay::write, BreakOverlay::read, Interactions::breakOverlay);
    public static final PacketType<MovingCell> MOVING_CELL = PacketType.clientbound("moving_cell", MovingCell.class, MovingCell::write, MovingCell::read);
    //? if <26 {
    public static final PacketType<CarriedCellSync> CARRIED_CELL = PacketType.clientbound("carried_cell", CarriedCellSync.class, CarriedCellSync::write, CarriedCellSync::read);
    public static final PacketType<UseCarriedOverlay> USE_CARRIED = PacketType.serverbound("use_carried", UseCarriedOverlay.class, UseCarriedOverlay::write, UseCarriedOverlay::read, CarriedOverlays::use);
    //?}
    public static final PacketType<PickOverlay> PICK = PacketType.serverbound("pick", PickOverlay.class, PickOverlay::write, PickOverlay::read, Interactions::pick);

    private OffgridNetwork() {
    }

    public static List<PacketType<?>> all() {
        //? if <26 {
        return List.of(CHUNK, CELL, EVENT, DESTRUCTION, OPEN_SIGN, SURFACE, SETTINGS, MOVING_CELL, CARRIED_CELL, USE, BREAK, USE_CARRIED, PICK);
        //?} else {
        /*return List.of(CHUNK, CELL, EVENT, DESTRUCTION, OPEN_SIGN, SURFACE, SETTINGS, MOVING_CELL, USE, BREAK, PICK);
        *///?}
    }

    public static <T> void sendToServer(PacketType<T> type, T packet) {
        Services.PLATFORM.sendToServer(type, packet);
    }

    public static <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        Services.PLATFORM.sendToPlayer(player, type, packet);
    }
}
