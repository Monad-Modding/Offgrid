package dev.tazer.offgrid.network;

//? if >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OffgridPayload<T>(PacketType<T> packet, T value) implements CustomPacketPayload {

    @Override
    public CustomPacketPayload.Type<OffgridPayload<T>> type() {
        return this.packet.getPayloadType();
    }

    public void handleOnClient() {
        this.packet.handleOnClient(this.value);
    }
}
//?}
