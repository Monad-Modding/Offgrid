package dev.tazer.offgrid.network;

import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public record CarriedCellSync(int entity, long pos, @Nullable AssembledCell cell) {

    public static CarriedCellSync read(FriendlyByteBuf buf) {
        int entity = buf.readVarInt();
        long pos = buf.readLong();
        return new CarriedCellSync(entity, pos, buf.readBoolean() ? AssembledCell.read(buf) : null);
    }

    public static void write(FriendlyByteBuf buf, CarriedCellSync packet) {
        buf.writeVarInt(packet.entity);
        buf.writeLong(packet.pos);
        buf.writeBoolean(packet.cell != null);
        if (packet.cell != null) {
            AssembledCell.write(buf, packet.cell);
        }
    }
}
