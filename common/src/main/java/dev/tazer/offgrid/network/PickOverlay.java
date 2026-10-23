package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record PickOverlay(long pos, int id, boolean includeData) {

    public static PickOverlay read(FriendlyByteBuf buf) {
        return new PickOverlay(buf.readLong(), buf.readVarInt(), buf.readBoolean());
    }

    public static void write(FriendlyByteBuf buf, PickOverlay packet) {
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.id);
        buf.writeBoolean(packet.includeData);
    }
}
