package dev.tazer.offgrid.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public record UseOverlay(int id, InteractionHand hand, BlockHitResult hit, int sequence) {

    public static UseOverlay read(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        //? if >=26.3 {
        /*BlockHitResult hit = BlockHitResult.STREAM_CODEC.decode(buf);
        *///?} else {
        BlockHitResult hit = buf.readBlockHitResult();
        //?}
        return new UseOverlay(id, hand, hit, buf.readVarInt());
    }

    public static void write(FriendlyByteBuf buf, UseOverlay packet) {
        buf.writeVarInt(packet.id);
        buf.writeEnum(packet.hand);
        //? if >=26.3 {
        /*BlockHitResult.STREAM_CODEC.encode(buf, packet.hit);
        *///?} else {
        buf.writeBlockHitResult(packet.hit);
        //?}
        buf.writeVarInt(packet.sequence);
    }
}
