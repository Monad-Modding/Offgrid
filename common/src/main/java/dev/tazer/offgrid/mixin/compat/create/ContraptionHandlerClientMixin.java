//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import dev.tazer.offgrid.compat.create.CarriedCells;
import dev.tazer.offgrid.compat.create.CarriedOverlayHit;
import dev.tazer.offgrid.compat.create.CarriedOverlays;
import dev.tazer.offgrid.compat.create.ContraptionCarrier;
import dev.tazer.offgrid.network.OffgridNetwork;
import dev.tazer.offgrid.network.UseCarriedOverlay;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Pseudo
@IfModPresent("create")
@Mixin(ContraptionHandlerClient.class)
public abstract class ContraptionHandlerClientMixin {

    @ModifyReturnValue(method = "rayTraceContraption", at = @At("RETURN"), remap = false)
    @Nullable
    private static BlockHitResult offgrid$clipCarriedOverlays(@Nullable BlockHitResult hit, Vec3 origin, Vec3 target, AbstractContraptionEntity entity) {
        ContraptionCarrier carrier = (ContraptionCarrier) (Object) entity;
        CarriedCells carried = carrier.offgrid$getCarried();
        if (carried == null || carried.offgrid$getCarriedCells().isEmpty()) {
            carrier.offgrid$setLastHit(hit);
            return hit;
        }
        Contraption contraption = (Contraption) (Object) carried;
        Map<BlockPos, BlockState> hosts = new HashMap<>();
        for (BlockPos local : carried.offgrid$getCarriedCells().keySet()) {
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(local);
            if (info != null) {
                hosts.put(local, info.state());
            }
        }
        BlockHitResult best = CarriedOverlays.clip(carried, hosts, carrier.offgrid$toLocal(origin), carrier.offgrid$toLocal(target), hit);
        carrier.offgrid$setLastHit(best);
        return best;
    }

    @WrapOperation(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/AbstractContraptionEntity;handlePlayerInteraction", remap = false), remap = false, require = 0)
    private static boolean offgrid$useCarriedOverlay(AbstractContraptionEntity entity, Player player, BlockPos pos, Direction face, InteractionHand hand, Operation<Boolean> original) {
        CarriedOverlayHit hit = offgrid$carriedHit(entity, pos);
        if (hit == null) return original.call(entity, player, pos, face, hand);
        CarriedCells carried = ((ContraptionCarrier) (Object) entity).offgrid$getCarried();
        if (carried != null && CarriedOverlays.isUsable(carried.offgrid$getCarriedCells().get(pos).overlays().get(hit.getIndex()).state())) {
            OffgridNetwork.sendToServer(OffgridNetwork.USE_CARRIED, new UseCarriedOverlay(((Entity) (Object) entity).getId(), pos.asLong(), hit.getIndex()));
        }
        return false;
    }

    @WrapOperation(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/ContraptionHandlerClient;handleSpecialInteractions", remap = false), remap = false, require = 0)
    private static boolean offgrid$skipSpecialInteractions(AbstractContraptionEntity entity, Player player, BlockPos pos, Direction face, InteractionHand hand, Operation<Boolean> original) {
        return offgrid$carriedHit(entity, pos) == null && original.call(entity, player, pos, face, hand);
    }

    @Unique
    @Nullable
    private static CarriedOverlayHit offgrid$carriedHit(AbstractContraptionEntity entity, BlockPos pos) {
        BlockHitResult last = ((ContraptionCarrier) (Object) entity).offgrid$getLastHit();
        return last instanceof CarriedOverlayHit hit && hit.getBlockPos().equals(pos) ? hit : null;
    }
}
//?}
