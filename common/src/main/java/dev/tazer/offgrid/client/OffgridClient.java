package dev.tazer.offgrid.client;

import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.OpenOverlaySign;
import dev.tazer.offgrid.network.SurfaceOffsetSync;
import dev.tazer.offgrid.world.Blocklogging;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.Overlays;
import dev.tazer.offgrid.world.SurfaceMounted;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
//? if <26 {
import dev.tazer.offgrid.compat.create.CarriedOverlays;
//?}
//? if >=26.3 {
/*import net.minecraft.world.level.block.entity.SignTextSlot;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OffgridClient {

    private OffgridClient() {
    }

    public static void init() {
        Cells.setClientListener(ClientCells::markDirty);
        OffgridNetwork.CHUNK.setClientHandler(ClientCells::applyChunk);
        OffgridNetwork.CELL.setClientHandler(ClientCells::applyCell);
        OffgridNetwork.EVENT.setClientHandler(ClientCells::applyEvent);
        OffgridNetwork.DESTRUCTION.setClientHandler(OverlayCracks::apply);
        OffgridNetwork.OPEN_SIGN.setClientHandler(OffgridClient::openSign);
        OffgridNetwork.SURFACE.setClientHandler(OffgridClient::mountOnSurface);
        OffgridNetwork.SETTINGS.setClientHandler(OffgridSettings::receive);
        OffgridNetwork.MOVING_CELL.setClientHandler(ClientCells::applyMovingCell);
        //? if <26 {
        OffgridNetwork.CARRIED_CELL.setClientHandler(packet -> CarriedOverlays.apply(Minecraft.getInstance().level, packet));
        //?}
    }

    public static List<String> debugLines(Level level, BlockPos pos) {
        Cell cell = Cells.get(level, pos);
        if (cell == null || cell.isEmpty()) return List.of();
        List<String> lines = new ArrayList<>();
        if (cell.offset() != null) {
            lines.add("offset: " + pixels(cell.offset()));
        }
        for (Overlay overlay : cell.overlays()) {
            BlockState state = overlay.getState();
            lines.add("");
            lines.add(ChatFormatting.UNDERLINE + "Targeted Overlay: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            lines.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(state.getBlock())));
            for (Property<?> property : state.getProperties()) {
                lines.add(property.getName() + ": " + valueName(state, property));
            }
            if (!Overlays.isZero(overlay.getOffset())) {
                lines.add("offset: " + pixels(overlay.getOffset()));
            }
            if (Blocklogging.at(level, pos, overlay) != null) {
                lines.add("blocklogged: " + ChatFormatting.GREEN + "true");
            }
            //? if <26 {
            state.getTags().map(tag -> "#" + tag.location()).forEach(lines::add);
            //?}
        }
        return lines;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        T value = state.getValue(property);
        String name = property.getName(value);
        if (Boolean.TRUE.equals(value)) return ChatFormatting.GREEN + name;
        if (Boolean.FALSE.equals(value)) return ChatFormatting.RED + name;
        return name;
    }

    private static String pixels(Vec3 offset) {
        return pixel(offset.x) + ", " + pixel(offset.y) + ", " + pixel(offset.z) + " px";
    }

    private static String pixel(double value) {
        double pixels = value * 16.0;
        long whole = Math.round(pixels);
        return Math.abs(pixels - whole) < 1.0E-4 ? Long.toString(whole) : String.format(Locale.ROOT, "%.2f", pixels);
    }

    private static void mountOnSurface(SurfaceOffsetSync packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(packet.entity()) instanceof SurfaceMounted mounted) {
            mounted.offgrid$setSurfaceOffset(packet.offset());
        }
    }

    private static void openSign(OpenOverlaySign packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        BlockPos pos = BlockPos.of(packet.pos());
        Overlay overlay = Cells.find(level, pos, packet.id());
        if (overlay == null) return;
        BlockEntity blockEntity = overlay.materialize(level, pos);
        if (blockEntity instanceof SignBlockEntity sign) {
            //? if >=26.3 {
            /*minecraft.setScreen(new SignEditScreen(sign, packet.front() ? SignTextSlot.FRONT : SignTextSlot.BACK, minecraft.isTextFilteringEnabled()));
            *///?} else {
            minecraft.setScreen(new SignEditScreen(sign, packet.front(), minecraft.isTextFilteringEnabled()));
            //?}
        }
    }
}
