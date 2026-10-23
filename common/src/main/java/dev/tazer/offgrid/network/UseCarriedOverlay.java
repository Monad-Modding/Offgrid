package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record UseCarriedOverlay(int entity, long pos, int index) {

    public static UseCarriedOverlay read(FriendlyByteBuf buf) {
        return new UseCarriedOverlay(buf.readVarInt(), buf.readLong(), buf.readVarInt());
    }

    public static void write(FriendlyByteBuf buf, UseCarriedOverlay packet) {
        buf.writeVarInt(packet.entity);
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.index);
    }
}
