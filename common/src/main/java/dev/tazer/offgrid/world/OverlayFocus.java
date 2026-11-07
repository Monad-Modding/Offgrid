package dev.tazer.offgrid.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class OverlayFocus {

    private static final double REACH_BUFFER = 4.0;

    private OverlayFocus() {
    }

    public static void focus(Level level, Player player, BlockPos host, Overlay overlay) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null) return;
        Long2ObjectOpenHashMap<Focus> next = without(index, player.getUUID());
        if (overlay.getState().hasBlockEntity()) {
            next.put(host.asLong(), new Focus(player.getUUID(), overlay.getId()));
        }
        index.setFocuses(next);
    }

    public static void clear(Level level, Player player) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.getFocuses().isEmpty()) return;
        index.setFocuses(without(index, player.getUUID()));
    }

    @Nullable
    public static BlockEntity blockEntityAt(Level level, BlockPos pos) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null) return null;
        Long2ObjectMap<Focus> focuses = index.getFocuses();
        if (focuses.isEmpty()) return null;
        Focus focus = focuses.get(pos.asLong());
        if (focus == null || Scope.get(Scope.FRAME) != null) return null;
        Overlay overlay = Cells.find(level, pos, focus.overlayId());
        return overlay == null ? null : overlay.getBlockEntity();
    }

    public static boolean isFocused(Level level, BlockPos pos) {
        OffgridIndex index = OffgridIndex.of(level);
        return index != null && !index.getFocuses().isEmpty() && index.getFocuses().containsKey(pos.asLong());
    }

    public static void tick(Level level) {
        OffgridIndex index = OffgridIndex.of(level);
        if (index == null || index.getFocuses().isEmpty()) return;
        Long2ObjectOpenHashMap<Focus> next = null;
        for (Long2ObjectMap.Entry<Focus> entry : index.getFocuses().long2ObjectEntrySet()) {
            BlockPos host = BlockPos.of(entry.getLongKey());
            Focus focus = entry.getValue();
            Player player = level.getPlayerByUUID(focus.player());
            Overlay overlay = Cells.find(level, host, focus.overlayId());
            if (player == null || player.isRemoved() || overlay == null || overlay.getBlockEntity() == null || !Players.canReach(player, host, REACH_BUFFER)) {
                if (next == null) {
                    next = new Long2ObjectOpenHashMap<>(index.getFocuses());
                }
                next.remove(entry.getLongKey());
            }
        }
        if (next != null) {
            index.setFocuses(next);
        }
    }

    private static Long2ObjectOpenHashMap<Focus> without(OffgridIndex index, UUID player) {
        Long2ObjectOpenHashMap<Focus> next = new Long2ObjectOpenHashMap<>(index.getFocuses());
        next.values().removeIf(focus -> focus.player().equals(player));
        return next;
    }

    public record Focus(UUID player, int overlayId) {
    }
}
