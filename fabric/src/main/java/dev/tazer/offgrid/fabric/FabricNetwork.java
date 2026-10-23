package dev.tazer.offgrid.fabric;

import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?}

public final class FabricNetwork {

    private FabricNetwork() {
    }

    public static void register() {
        for (PacketType<?> type : OffgridNetwork.all()) {
            register(type);
        }
    }

    private static <T> void register(PacketType<T> type) {
        //? if >=26 {
        /*if (type.isServerbound()) {
            PayloadTypeRegistry.serverboundPlay().register(type.getPayloadType(), type.getStreamCodec());
            ServerPlayNetworking.registerGlobalReceiver(type.getPayloadType(), (payload, context) -> type.handleOnServer(payload.value(), context.player()));
        } else {
            PayloadTypeRegistry.clientboundPlay().register(type.getPayloadType(), type.getStreamCodec());
        }
        *///?} else if >=1.20.5 {
        if (type.isServerbound()) {
            PayloadTypeRegistry.playC2S().register(type.getPayloadType(), type.getStreamCodec());
            ServerPlayNetworking.registerGlobalReceiver(type.getPayloadType(), (payload, context) -> type.handleOnServer(payload.value(), context.player()));
        } else {
            PayloadTypeRegistry.playS2C().register(type.getPayloadType(), type.getStreamCodec());
        }
        //?} else {
        /*if (!type.isServerbound()) return;
        ServerPlayNetworking.registerGlobalReceiver(type.getId(), (server, player, listener, buf, sender) -> {
            T value = type.read(buf);
            server.execute(() -> type.handleOnServer(value, player));
        });
        *///?}
    }
}
