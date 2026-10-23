package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.world.AssembledCell;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
//? if >=1.20.5 {
import net.minecraft.world.level.chunk.status.ChunkStatus;
//?} else {
/*import net.minecraft.world.level.chunk.ChunkStatus;
*///?}
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Unique
    private static final String offgrid$CELLS = "offgrid:cells";

    @Unique
    private final List<AssembledCell> offgrid$cells = new ArrayList<>();

    @Inject(method = "fillFromWorld", at = @At("TAIL"))
    private void offgrid$captureCells(CallbackInfo ci, @Local(argsOnly = true) Level level, @Local(argsOnly = true, ordinal = 0) BlockPos origin, @Local(argsOnly = true) Vec3i size) {
        this.offgrid$cells.clear();
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) return;
        for (BlockPos pos : BlockPos.betweenClosed(origin, origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
            AssembledCell cell = AssembledCell.copy(level, pos, pos.subtract(origin));
            if (cell != null) {
                this.offgrid$cells.add(cell);
            }
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void offgrid$saveCells(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.offgrid$cells.isEmpty()) return;
        ListTag cells = new ListTag();
        for (AssembledCell cell : this.offgrid$cells) {
            AssembledCell.CODEC.encodeStart(NbtOps.INSTANCE, cell).result().ifPresent(cells::add);
        }
        cir.getReturnValue().put(offgrid$CELLS, cells);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void offgrid$loadCells(CallbackInfo ci, @Local(argsOnly = true) CompoundTag tag) {
        this.offgrid$cells.clear();
        if (!(tag.get(offgrid$CELLS) instanceof ListTag cells)) return;
        for (Tag cell : cells) {
            AssembledCell.CODEC.parse(NbtOps.INSTANCE, cell).result().ifPresent(this.offgrid$cells::add);
        }
    }

    @Unique
    private static AssembledCell offgrid$process(ServerLevelAccessor accessor, BlockPos offset, BlockPos pivot, StructurePlaceSettings settings, AssembledCell cell) {
        if (settings.getProcessors().isEmpty()) return cell;
        List<AssembledCell.AssembledOverlay> processed = new ArrayList<>(cell.overlays().size());
        for (AssembledCell.AssembledOverlay overlay : cell.overlays()) {
            List<StructureTemplate.StructureBlockInfo> result = StructureTemplate.processBlockInfos(accessor, offset, pivot, settings, List.of(new StructureTemplate.StructureBlockInfo(cell.origin(), overlay.state(), overlay.tag())));
            if (result.isEmpty() || result.get(0).state().isAir()) {
                continue;
            }
            StructureTemplate.StructureBlockInfo info = result.get(0);
            processed.add(new AssembledCell.AssembledOverlay(info.state(), overlay.offset(), info.nbt(), overlay.trigger()));
        }
        return new AssembledCell(cell.origin(), cell.offset(), List.copyOf(processed));
    }

    @Inject(method = "placeInWorld", at = @At("RETURN"))
    private void offgrid$placeCells(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ServerLevelAccessor accessor, @Local(argsOnly = true, ordinal = 0) BlockPos offset, @Local(argsOnly = true, ordinal = 1) BlockPos pivot, @Local(argsOnly = true) StructurePlaceSettings settings) {
        if (!cir.getReturnValueZ() || this.offgrid$cells.isEmpty()) return;
        BoundingBox bounds = settings.getBoundingBox();
        for (AssembledCell template : this.offgrid$cells) {
            AssembledCell cell = offgrid$process(accessor, offset, pivot, settings, template);
            BlockPos target = StructureTemplate.calculateRelativePosition(settings, cell.origin()).offset(offset);
            if (bounds != null && !bounds.isInside(target)) {
                continue;
            }
            if (accessor instanceof ServerLevel level) {
                cell.place(level, target, settings.getMirror(), settings.getRotation());
                continue;
            }
            if (!(accessor instanceof WorldGenRegion)) {
                OffgridIndex index = OffgridIndex.of(accessor);
                if (index != null) {
                    cell.write(index.detachedChunk(ChunkPos.asLong(SectionPos.blockToSectionCoord(target.getX()), SectionPos.blockToSectionCoord(target.getZ()))), target, settings.getMirror(), settings.getRotation());
                }
                continue;
            }
            ChunkAccess chunk = accessor.getChunk(SectionPos.blockToSectionCoord(target.getX()), SectionPos.blockToSectionCoord(target.getZ()), ChunkStatus.EMPTY, false);
            OffgridChunk data = chunk == null ? null : Services.PLATFORM.getOrCreateGeneratingChunkData(accessor.getLevel(), chunk);
            if (data != null) {
                cell.write(data, target, settings.getMirror(), settings.getRotation());
            }
        }
    }
}
