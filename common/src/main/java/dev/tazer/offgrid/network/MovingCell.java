package dev.tazer.offgrid.network;

import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.network.FriendlyByteBuf;

public record MovingCell(long pos, AssembledCell cell) {

    public static MovingCell read(FriendlyByteBuf buf) {
        long pos = buf.readLong();
        AssembledCell cell = AssembledCell.read(buf);
        return new MovingCell(pos, cell);
    }

    public static void write(FriendlyByteBuf buf, MovingCell packet) {
        buf.writeLong(packet.pos);
        AssembledCell.write(buf, packet.cell);
    }
}
