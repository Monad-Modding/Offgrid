//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import dev.tazer.offgrid.compat.create.OverlayPrinter;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("create")
@Mixin(SchematicannonBlockEntity.class)
public abstract class SchematicannonBlockEntityMixin {

    @WrapOperation(method = "tickPrinter", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/schematics/SchematicPrinter;shouldPlaceCurrent", remap = false), remap = false)
    private boolean offgrid$shouldPrintOverlay(SchematicPrinter printer, Level level, SchematicPrinter.PlacementPredicate predicate, Operation<Boolean> original) {
        Boolean overlay = ((OverlayPrinter) (Object) printer).offgrid$shouldPrintOverlay(level);
        return overlay != null ? overlay : original.call(printer, level, predicate);
    }
}
//?}
