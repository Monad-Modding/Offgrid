//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.contraptions.TranslatingContraption;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import dev.tazer.offgrid.compat.create.CarriedCells;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.AssembledCell;
import dev.tazer.offgrid.world.Blocklogging;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import dev.tazer.offgrid.world.OverlayShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21 {
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?} else {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Pseudo
@IfModPresent("create")
@Mixin(ContraptionCollider.class)
public abstract class ContraptionColliderMixin {

    //? if >=1.21 {
    @Inject(method = "getPotentiallyCollidedShapes", at = @At("TAIL"), remap = false)
    private static void offgrid$carriedOverlayShapes(Level level, Contraption contraption, AABB localBox, Shapes.DoubleLineConsumer out, CallbackInfo ci) {
        for (VoxelShape shape : offgrid$carriedShapes(contraption, localBox)) {
            shape.forAllBoxes(out);
        }
    }
    //?} else {
    /*@ModifyReturnValue(method = "getPotentiallyCollidedShapes", at = @At("RETURN"), remap = false)
    private static List<VoxelShape> offgrid$carriedOverlayShapes(List<VoxelShape> shapes, Level level, Contraption contraption, AABB localBox) {
        List<VoxelShape> carried = offgrid$carriedShapes(contraption, localBox);
        if (carried.isEmpty()) return shapes;
        List<VoxelShape> combined = new ArrayList<>(shapes);
        combined.addAll(carried);
        return combined;
    }
    *///?}

    @Inject(method = "isCollidingWithWorld", at = @At("RETURN"), cancellable = true, remap = false)
    private static void offgrid$collideWithOverlays(Level level, TranslatingContraption contraption, BlockPos anchor, Direction movement, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        for (BlockPos local : contraption.getOrCreateColliders(level, movement)) {
            BlockPos target = local.offset(anchor);
            if (!Cells.hasOverlays(level, target)) {
                continue;
            }
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(local);
            MovementBehaviour behaviour = info == null ? null : MovementBehaviour.REGISTRY.get(info.state());
            for (Overlay overlay : Cells.overlays(level, target)) {
                if (OverlayShapes.collision(level, target, overlay).isEmpty()) {
                    continue;
                }
                if (behaviour instanceof BlockBreakingMovementBehaviour breaking && OverlayContext.call(level, target, overlay, () -> breaking.canBreak(level, target, overlay.getState()))) {
                    continue;
                }
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Unique
    private static List<VoxelShape> offgrid$carriedShapes(Contraption contraption, AABB localBox) {
        if (!(contraption instanceof CarriedCells carried) || carried.offgrid$getCarriedCells().isEmpty()) return List.of();
        AABB reach = localBox.inflate(2.0);
        List<VoxelShape> shapes = new ArrayList<>();
        for (Map.Entry<BlockPos, AssembledCell> entry : carried.offgrid$getCarriedCells().entrySet()) {
            BlockPos local = entry.getKey();
            if (!reach.intersects(new AABB(local))) {
                continue;
            }
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(local);
            BlockState host = info == null ? Blocks.AIR.defaultBlockState() : info.state();
            for (AssembledCell.AssembledOverlay overlay : entry.getValue().overlays()) {
                Blocklogging.Blocklog cut = Blocklogging.of(overlay.state(), host, overlay.offset());
                VoxelShape shape = cut != null ? cut.collision() : overlay.state().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).move(overlay.offset().x, overlay.offset().y, overlay.offset().z);
                if (!shape.isEmpty()) {
                    shapes.add(shape.move(local.getX(), local.getY(), local.getZ()));
                }
            }
        }
        return shapes;
    }
}
//?}
