//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@IfModPresent("create")
@Mixin(SchematicPrinter.class)
public abstract class SchematicPrinterMixin implements OverlayPrinter {

    @Unique
    private List<PrintedOverlay> offgrid$overlays = List.of();

    @Unique
    private int offgrid$overlayIndex = -1;

    @Unique
    @Nullable
    private Level offgrid$world;

    @WrapOperation(method = "loadSchematic", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z", remap = true), remap = false)
    private boolean offgrid$collectOverlays(StructureTemplate template, ServerLevelAccessor schematic, BlockPos offset, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, Operation<Boolean> original) {
        boolean placed = original.call(template, schematic, offset, pivot, settings, random, flags);
        this.offgrid$overlays = PrintedOverlay.collect(schematic);
        this.offgrid$overlayIndex = -1;
        return placed;
    }

    @Inject(method = "resetSchematic", at = @At("TAIL"), remap = false)
    private void offgrid$resetOverlays(CallbackInfo ci) {
        this.offgrid$overlays = List.of();
        this.offgrid$overlayIndex = -1;
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

    @Override
    @Nullable
    public Boolean offgrid$shouldPrintOverlay(Level level) {
        this.offgrid$world = level;
        PrintedOverlay overlay = this.offgrid$current();
        return overlay == null ? null : level != null && overlay.isPrintable(level);
    }

    @Inject(method = "handleCurrentTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void offgrid$printOverlay(SchematicPrinter.BlockTargetHandler blocks, SchematicPrinter.EntityTargetHandler entities, CallbackInfo ci) {
        PrintedOverlay overlay = this.offgrid$current();
        if (overlay == null) return;
        ci.cancel();
        if (!(this.offgrid$world instanceof ServerLevel level)) return;
        Overlay placed = Overlays.add(level, overlay.pos(), overlay.state(), overlay.offset(), overlay.tag());
        if (placed != null) {
            SoundType sound = overlay.state().getSoundType();
            level.playSound(null, overlay.pos(), sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        }
    }

    @Inject(method = "markAllBlockRequirements", at = @At("RETURN"), cancellable = true, remap = false)
    private void offgrid$requireOverlays(MaterialChecklist checklist, Level level, SchematicPrinter.PlacementPredicate predicate, CallbackInfoReturnable<Integer> cir) {
        int count = cir.getReturnValueI();
        for (PrintedOverlay overlay : this.offgrid$overlays) {
            if (!overlay.isPrintable(level)) {
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

    @Unique
    @Nullable
    private PrintedOverlay offgrid$current() {
        return this.offgrid$overlayIndex >= 0 && this.offgrid$overlayIndex < this.offgrid$overlays.size() ? this.offgrid$overlays.get(this.offgrid$overlayIndex) : null;
    }
}
//?}
