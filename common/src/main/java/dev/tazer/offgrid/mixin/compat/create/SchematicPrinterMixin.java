//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import dev.tazer.offgrid.compat.create.OverlayPrinter;
import dev.tazer.offgrid.compat.create.PrintedOverlay;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.Overlays;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@IfModPresent("create")
@Mixin(SchematicPrinter.class)
public abstract class SchematicPrinterMixin implements OverlayPrinter {

    @Unique
    private final List<PrintedOverlay> offgrid$overlays = new ArrayList<>();

    @Unique
    private List<PrintedOverlay> offgrid$planned = List.of();

    @Unique
    private int offgrid$overlayIndex = -1;

    @Unique
    private boolean offgrid$busy;

    @Unique
    private boolean offgrid$printedOverlay;

    @Unique
    @Nullable
    private BlockGetter offgrid$schematic;

    @Unique
    @Nullable
    private Level offgrid$world;

    @WrapOperation(method = "loadSchematic", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z", remap = true), remap = false)
    private boolean offgrid$collectOverlays(StructureTemplate template, ServerLevelAccessor schematic, BlockPos offset, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, Operation<Boolean> original) {
        boolean placed = original.call(template, schematic, offset, pivot, settings, random, flags);
        this.offgrid$schematic = schematic;
        this.offgrid$planned = PrintedOverlay.collect(schematic);
        this.offgrid$overlays.clear();
        this.offgrid$overlays.addAll(this.offgrid$planned);
        this.offgrid$overlayIndex = -1;
        return placed;
    }

    @Inject(method = "resetSchematic", at = @At("TAIL"), remap = false)
    private void offgrid$resetOverlays(CallbackInfo ci) {
        this.offgrid$planned = List.of();
        this.offgrid$overlays.clear();
        this.offgrid$overlayIndex = -1;
        this.offgrid$schematic = null;
        this.offgrid$world = null;
    }

    @Inject(method = "advanceCurrentPos", at = @At("RETURN"), cancellable = true, remap = false)
    private void offgrid$advanceToOverlays(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || this.offgrid$overlayIndex + 1 >= this.offgrid$overlays.size()) return;
        this.offgrid$overlayIndex++;
        cir.setReturnValue(true);
    }

    @Inject(method = "getCurrentTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$overlayTarget(CallbackInfoReturnable<BlockPos> cir) {
        PrintedOverlay overlay = this.offgrid$current();
        if (overlay != null) {
            cir.setReturnValue(overlay.pos());
        }
    }

    @Inject(method = "getCurrentRequirement", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$overlayRequirement(CallbackInfoReturnable<ItemRequirement> cir) {
        PrintedOverlay overlay = this.offgrid$current();
        if (overlay != null) {
            cir.setReturnValue(ItemRequirement.of(overlay.state(), null));
        }
    }

    @Inject(method = "shouldPlaceCurrent", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$shouldPrintOverlay(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) Level world) {
        this.offgrid$world = world;
        PrintedOverlay overlay = this.offgrid$current();
        if (overlay == null) return;
        if (world == null) {
            cir.setReturnValue(false);
            return;
        }
        boolean printable = overlay.isPrintable(world);
        if (!printable && this.offgrid$busy && world.isLoaded(overlay.pos()) && !overlay.isPlaced(world)) {
            this.offgrid$overlays.add(overlay);
        }
        cir.setReturnValue(printable);
    }

    @Inject(method = "handleCurrentTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$printOverlay(CallbackInfo ci) {
        PrintedOverlay overlay = this.offgrid$current();
        if (overlay == null) return;
        ci.cancel();
        if (!(this.offgrid$world instanceof ServerLevel level)) return;
        Overlay placed = Overlays.add(level, overlay.pos(), overlay.state(), overlay.offset(), overlay.tag());
        if (placed == null) return;
        this.offgrid$printedOverlay = true;
        SoundType sound = overlay.state().getSoundType();
        level.playSound(null, overlay.pos(), sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
    }

    @Inject(method = "handleCurrentTarget", at = @At("RETURN"), remap = false)
    private void offgrid$offsetPrintedBlock(CallbackInfo ci) {
        if (this.offgrid$current() != null || !(this.offgrid$world instanceof ServerLevel level)) return;
        BlockPos target = ((SchematicPrinter) (Object) this).getCurrentTarget();
        Vec3 offset = target == null ? null : this.offgrid$offsetAt(target);
        if (offset != null && this.offgrid$schematic != null) {
            PrintedOverlay.printOffset(level, target, this.offgrid$schematic.getBlockState(target), offset);
        }
    }

    @Inject(method = "markAllBlockRequirements", at = @At("RETURN"), cancellable = true, remap = false)
    private void offgrid$requireOverlays(CallbackInfoReturnable<Integer> cir, @Local(argsOnly = true) MaterialChecklist checklist, @Local(argsOnly = true) Level level) {
        int count = cir.getReturnValueI();
        for (PrintedOverlay overlay : this.offgrid$planned) {
            if (!level.isLoaded(overlay.pos())) {
                checklist.warnBlockNotLoaded();
                continue;
            }
            if (overlay.isPlaced(level)) {
                continue;
            }
            ItemRequirement requirement = ItemRequirement.of(overlay.state(), null);
            if (requirement.isEmpty() || requirement.isInvalid()) {
                continue;
            }
            checklist.require(requirement);
            count++;
        }
        cir.setReturnValue(count);
    }

    @Override
    public void offgrid$setBusy(boolean busy) {
        this.offgrid$busy = busy;
    }

    @Override
    @Nullable
    public Vec3 offgrid$offsetAt(BlockPos pos) {
        return PrintedOverlay.offsetAt(this.offgrid$schematic, pos);
    }

    @Override
    public boolean offgrid$takePrintedOverlay() {
        boolean printed = this.offgrid$printedOverlay;
        this.offgrid$printedOverlay = false;
        return printed;
    }

    @Unique
    @Nullable
    private PrintedOverlay offgrid$current() {
        return this.offgrid$overlayIndex >= 0 && this.offgrid$overlayIndex < this.offgrid$overlays.size() ? this.offgrid$overlays.get(this.offgrid$overlayIndex) : null;
    }
}
//?}
