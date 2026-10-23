package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenOverlaySign(long pos, int id, boolean front) {

    public static OpenOverlaySign read(FriendlyByteBuf buf) {
        return new OpenOverlaySign(buf.readLong(), buf.readVarInt(), buf.readBoolean());
    }

    public static void write(FriendlyByteBuf buf, OpenOverlaySign packet) {
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.id);
        buf.writeBoolean(packet.front);
    }
}
