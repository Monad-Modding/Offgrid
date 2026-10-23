package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record ChunkSync(long chunk, List<CellData> cells) {

    public static ChunkSync read(FriendlyByteBuf buf) {
        long chunk = buf.readLong();
        int count = buf.readVarInt();
        List<CellData> cells = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cells.add(CellData.read(buf));
        }
        return new ChunkSync(chunk, cells);
    }

    public static void write(FriendlyByteBuf buf, ChunkSync packet) {
        buf.writeLong(packet.chunk);
        buf.writeVarInt(packet.cells.size());
        for (CellData cell : packet.cells) {
            CellData.write(buf, cell);
        }
    }
}
