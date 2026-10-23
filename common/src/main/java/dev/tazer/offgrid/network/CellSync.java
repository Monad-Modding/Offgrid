package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

public record CellSync(CellData cell, int[] broken) {

    public static CellSync read(FriendlyByteBuf buf) {
        return new CellSync(CellData.read(buf), buf.readVarIntArray());
    }

    public static void write(FriendlyByteBuf buf, CellSync packet) {
        CellData.write(buf, packet.cell);
        buf.writeVarIntArray(packet.broken);
    }
}
