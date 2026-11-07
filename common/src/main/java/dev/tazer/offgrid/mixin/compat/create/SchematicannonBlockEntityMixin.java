//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.LaunchedItem;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import dev.tazer.offgrid.compat.create.OffsetLaunch;
import dev.tazer.offgrid.compat.create.OverlayPrinter;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@IfModPresent("create")
@Mixin(SchematicannonBlockEntity.class)
public abstract class SchematicannonBlockEntityMixin {

    @Shadow(remap = false)
    public List<LaunchedItem> flyingBlocks;

    @Shadow(remap = false)
    public int blocksPlaced;

    @Shadow(remap = false)
    public String statusMsg;

    @Shadow(remap = false)
    public SchematicPrinter printer;

    @Inject(method = "tickPrinter", at = @At("HEAD"), remap = false)
    private void offgrid$waitForFlyingBlocks(CallbackInfo ci) {
        ((OverlayPrinter) (Object) this.printer).offgrid$setBusy(!this.flyingBlocks.isEmpty());
    }

    @WrapOperation(method = "tickPrinter", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/schematics/SchematicPrinter;handleCurrentTarget", remap = false), remap = false)
    private void offgrid$countPrintedOverlay(SchematicPrinter printer, SchematicPrinter.BlockTargetHandler blocks, SchematicPrinter.EntityTargetHandler entities, Operation<Void> original) {
        original.call(printer, blocks, entities);
        if (((OverlayPrinter) (Object) printer).offgrid$takePrintedOverlay()) {
            this.blocksPlaced++;
            this.statusMsg = "placing";
        }
    }

    @Inject(method = "launchBlock", at = @At("TAIL"), remap = false)
    private void offgrid$carryOffset(CallbackInfo ci) {
        if (this.flyingBlocks.isEmpty() || !(this.flyingBlocks.get(this.flyingBlocks.size() - 1) instanceof OffsetLaunch launch)) return;
        BlockPos target = this.flyingBlocks.get(this.flyingBlocks.size() - 1).target;
        launch.offgrid$setOffset(((OverlayPrinter) (Object) this.printer).offgrid$offsetAt(target));
    }
}
//?}
