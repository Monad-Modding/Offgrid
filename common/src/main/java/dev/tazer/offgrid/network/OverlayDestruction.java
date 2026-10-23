package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record OverlayDestruction(int breaker, long pos, int id, int stage) {

    public static OverlayDestruction read(FriendlyByteBuf buf) {
        return new OverlayDestruction(buf.readVarInt(), buf.readLong(), buf.readVarInt(), buf.readByte());
    }

    public static void write(FriendlyByteBuf buf, OverlayDestruction packet) {
        buf.writeVarInt(packet.breaker);
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.id);
        buf.writeByte(packet.stage);
    }
}
