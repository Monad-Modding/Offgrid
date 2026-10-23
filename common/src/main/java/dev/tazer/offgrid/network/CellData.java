package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record CellData(long pos, @Nullable Vec3 offset, List<OverlayData> overlays) {

    public static CellData read(FriendlyByteBuf buf) {
        long pos = buf.readLong();
        Vec3 offset = buf.readBoolean() ? new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()) : null;
        int count = buf.readVarInt();
        List<OverlayData> overlays = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            overlays.add(OverlayData.read(buf));
        }
        return new CellData(pos, offset, overlays);
    }

    public static void write(FriendlyByteBuf buf, CellData data) {
        buf.writeLong(data.pos);
        buf.writeBoolean(data.offset != null);
        if (data.offset != null) {
            buf.writeDouble(data.offset.x);
            buf.writeDouble(data.offset.y);
            buf.writeDouble(data.offset.z);
        }
        buf.writeVarInt(data.overlays.size());
        for (OverlayData overlay : data.overlays) {
            OverlayData.write(buf, overlay);
        }
    }
}
