//? if <26 {
package dev.tazer.offgrid.compat.create;

import dev.tazer.offgrid.network.CarriedCellSync;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.UseCarriedOverlay;
import dev.tazer.offgrid.world.AssembledCell;
import dev.tazer.offgrid.world.Blocklogging;
import dev.tazer.offgrid.world.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CarriedOverlays {

    private static final double TRACKING_RANGE_SQR = 160.0 * 160.0;

    private CarriedOverlays() {
    }

    @Nullable
    public static BlockHitResult clip(CarriedCells carried, Map<BlockPos, BlockState> hosts, Vec3 localFrom, Vec3 localTo, @Nullable BlockHitResult blocks) {
        BlockHitResult best = blocks;
        double nearest = blocks == null ? Double.MAX_VALUE : blocks.getLocation().distanceToSqr(localFrom);
        for (Map.Entry<BlockPos, AssembledCell> entry : carried.offgrid$getCarriedCells().entrySet()) {
            BlockPos local = entry.getKey();
            List<AssembledCell.AssembledOverlay> overlays = entry.getValue().overlays();
            for (int index = 0; index < overlays.size(); index++) {
                VoxelShape shape = outline(overlays.get(index), hosts.get(local));
                BlockHitResult hit = shape.isEmpty() ? null : shape.clip(localFrom, localTo, local);
                if (hit == null) {
                    continue;
                }
                double distance = hit.getLocation().distanceToSqr(localFrom);
                if (distance < nearest) {
                    nearest = distance;
                    best = new CarriedOverlayHit(hit.getLocation(), hit.getDirection(), local, index);
                }
            }
        }
        return best;
    }

    public static boolean isUsable(BlockState state) {
        return state.getBlock() instanceof LeverBlock && state.hasProperty(BlockStateProperties.POWERED);
    }

    public static void use(UseCarriedOverlay packet, ServerPlayer player) {
        Entity entity = player.level().getEntity(packet.entity());
        if (!(entity instanceof ContraptionCarrier carrier)) return;
        CarriedCells carried = carrier.offgrid$getCarried();
        BlockPos local = BlockPos.of(packet.pos());
        AssembledCell cell = carried == null ? null : carried.offgrid$getCarriedCells().get(local);
        if (cell == null || packet.index() < 0 || packet.index() >= cell.overlays().size()) return;
        Vec3 global = carrier.offgrid$toGlobal(Vec3.atCenterOf(local));
        if (player.distanceToSqr(global) > Math.pow(Players.reach(player) + 2.0, 2.0)) return;
        AssembledCell.AssembledOverlay overlay = cell.overlays().get(packet.index());
        if (!isUsable(overlay.state())) return;
        BlockState toggled = overlay.state().cycle(BlockStateProperties.POWERED);
        List<AssembledCell.AssembledOverlay> overlays = new ArrayList<>(cell.overlays());
        overlays.set(packet.index(), new AssembledCell.AssembledOverlay(toggled, overlay.offset(), overlay.tag(), overlay.trigger()));
        AssembledCell changed = new AssembledCell(cell.origin(), cell.offset(), List.copyOf(overlays));
        carried.offgrid$getCarriedCells().put(local, changed);
        carried.offgrid$getCarriedBlockEntities().remove(overlay);
        Level level = player.level();
        level.playSound(null, global.x, global.y, global.z, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, toggled.getValue(BlockStateProperties.POWERED) ? 0.6F : 0.5F);
        CarriedCellSync sync = new CarriedCellSync(entity.getId(), packet.pos(), changed);
        for (ServerPlayer viewer : player.serverLevel().players()) {
            if (viewer.distanceToSqr(entity) <= TRACKING_RANGE_SQR) {
                OffgridNetwork.sendToPlayer(viewer, OffgridNetwork.CARRIED_CELL, sync);
            }
        }
    }

    public static void apply(Level level, CarriedCellSync packet) {
        if (!(level.getEntity(packet.entity()) instanceof ContraptionCarrier carrier)) return;
        CarriedCells carried = carrier.offgrid$getCarried();
        if (carried == null) return;
        BlockPos local = BlockPos.of(packet.pos());
        AssembledCell previous = packet.cell() == null ? carried.offgrid$getCarriedCells().remove(local) : carried.offgrid$getCarriedCells().put(local, packet.cell());
        if (previous != null) {
            for (AssembledCell.AssembledOverlay overlay : previous.overlays()) {
                carried.offgrid$getCarriedBlockEntities().remove(overlay);
            }
        }
    }

    private static VoxelShape outline(AssembledCell.AssembledOverlay overlay, @Nullable BlockState host) {
        Blocklogging.Blocklog cut = host == null ? null : Blocklogging.of(overlay.state(), host, overlay.offset());
        if (cut != null) return cut.outline();
        VoxelShape shape = overlay.state().getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
        return shape.isEmpty() ? shape : shape.move(overlay.offset().x, overlay.offset().y, overlay.offset().z);
    }
}
//?}
