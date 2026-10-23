package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record BreakOverlay(long pos, int id, Phase phase) {

    public static BreakOverlay read(FriendlyByteBuf buf) {
        return new BreakOverlay(buf.readLong(), buf.readVarInt(), buf.readEnum(Phase.class));
    }

    public static void write(FriendlyByteBuf buf, BreakOverlay packet) {
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.id);
        buf.writeEnum(packet.phase);
    }

    public enum Phase {
        START,
        ABORT,
        FINISH
    }
}
