package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record SurfaceOffsetSync(int entity, Vec3 offset) {

    public static SurfaceOffsetSync read(FriendlyByteBuf buf) {
        return new SurfaceOffsetSync(buf.readVarInt(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public static void write(FriendlyByteBuf buf, SurfaceOffsetSync packet) {
        buf.writeVarInt(packet.entity);
        buf.writeDouble(packet.offset.x);
        buf.writeDouble(packet.offset.y);
        buf.writeDouble(packet.offset.z);
    }
}
