package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record OverlayEvent(long pos, int id, int type, int param) {

    public static OverlayEvent read(FriendlyByteBuf buf) {
        return new OverlayEvent(buf.readLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void write(FriendlyByteBuf buf, OverlayEvent packet) {
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.id);
        buf.writeVarInt(packet.type);
        buf.writeVarInt(packet.param);
    }
}
