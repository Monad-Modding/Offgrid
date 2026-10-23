package dev.tazer.offgrid;

import dev.tazer.offgrid.network.OffgridNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record OffgridSettings(boolean offsetting, boolean overlaying, boolean blocklogging, boolean surfaceAttaching, boolean anyFacePaintings) {

    @Nullable
    private static volatile OffgridSettings received;

    public static OffgridSettings local() {
        OffgridConfig config = OffgridConfig.get();
        return new OffgridSettings(config.offsetting.value(), config.overlaying.value(), config.blocklogging.value(), config.surfaceAttaching.value(), config.anyFacePaintings.value());
    }

    public static OffgridSettings of(Level level) {
        OffgridSettings synced = received;
        return level.isClientSide() && synced != null ? synced : local();
    }

    public static void receive(OffgridSettings settings) {
        received = settings;
    }

    public static void sendTo(ServerPlayer player) {
        OffgridNetwork.sendToPlayer(player, OffgridNetwork.SETTINGS, local());
    }

    public static OffgridSettings read(FriendlyByteBuf buf) {
        return new OffgridSettings(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void write(FriendlyByteBuf buf, OffgridSettings settings) {
        buf.writeBoolean(settings.offsetting);
        buf.writeBoolean(settings.overlaying);
        buf.writeBoolean(settings.blocklogging);
        buf.writeBoolean(settings.surfaceAttaching);
        buf.writeBoolean(settings.anyFacePaintings);
    }

    public boolean allowsOverlaying() {
        return this.offsetting && this.overlaying;
    }

    public boolean allowsBlocklogging() {
        return this.allowsOverlaying() && this.blocklogging;
    }
}
