package dev.tazer.offgrid.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record OverlayData(int id, BlockState state, Vec3 offset, @Nullable CompoundTag tag) {

    public static OverlayData read(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        BlockState state = Block.stateById(buf.readVarInt());
        Vec3 offset = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        CompoundTag tag = buf.readBoolean() ? buf.readNbt() : null;
        return new OverlayData(id, state, offset, tag);
    }

    public static void write(FriendlyByteBuf buf, OverlayData data) {
        buf.writeVarInt(data.id);
        buf.writeVarInt(Block.getId(data.state));
        buf.writeDouble(data.offset.x);
        buf.writeDouble(data.offset.y);
        buf.writeDouble(data.offset.z);
        buf.writeBoolean(data.tag != null);
        if (data.tag != null) {
            buf.writeNbt(data.tag);
        }
    }
}
