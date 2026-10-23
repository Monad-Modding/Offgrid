//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import dev.tazer.offgrid.compat.create.CarriedCells;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Pseudo
@IfModPresent("create")
@Mixin(Contraption.class)
public abstract class ContraptionMixin implements CarriedCells {

    @Unique
    private static final String offgrid$CELLS = "offgrid:cells";

    @Shadow(remap = false)
    public BlockPos anchor;

    @Shadow(remap = false)
    protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;

    @Unique
    private final Map<BlockPos, AssembledCell> offgrid$cells = new HashMap<>();

    @Unique
    private final Map<AssembledCell.AssembledOverlay, BlockEntity> offgrid$blockEntities = new IdentityHashMap<>();

    @Override
    public Map<BlockPos, AssembledCell> offgrid$getCarriedCells() {
        return this.offgrid$cells;
    }

    @Override
    public Map<AssembledCell.AssembledOverlay, BlockEntity> offgrid$getCarriedBlockEntities() {
        return this.offgrid$blockEntities;
    }

    @Shadow(remap = false)
    protected abstract Pair<StructureTemplate.StructureBlockInfo, BlockEntity> capture(Level level, BlockPos pos);

    @Shadow(remap = false)
    protected abstract void addBlock(Level level, BlockPos pos, Pair<StructureTemplate.StructureBlockInfo, BlockEntity> pair);

    @Inject(method = "removeBlocksFromWorld", at = @At("HEAD"), remap = false)
    private void offgrid$assembleCells(Level level, BlockPos offset, CallbackInfo ci) {
        if (!(level instanceof ServerLevel server)) return;
        BlockPos origin = this.anchor.offset(offset);
        List<BlockPos> moved = new ArrayList<>(this.blocks.size());
        for (BlockPos local : this.blocks.keySet()) {
            moved.add(local.offset(origin));
        }
        AssembledCell.Held held = AssembledCell.held(server, moved);
        if (offset.equals(BlockPos.ZERO)) {
            for (BlockPos extra : held.blocks()) {
                this.addBlock(level, extra, this.capture(level, extra));
                moved.add(extra);
            }
        }
        this.offgrid$cells.putAll(AssembledCell.assembleLocal(server, origin, moved, held));
    }

    @Inject(method = "addBlocksToWorld", at = @At("TAIL"), remap = false)
    private void offgrid$landCells(Level level, StructureTransform transform, CallbackInfo ci) {
        if (!(level instanceof ServerLevel server)) return;
        AssembledCell.landAll(server, this.offgrid$cells, this.blocks.keySet(), transform::apply, transform::apply, transform::applyWithoutOffsetUncentered);
        this.offgrid$cells.clear();
        this.offgrid$blockEntities.clear();
    }

    //? if >=1.21 {
    @ModifyReturnValue(method = "writeNBT", at = @At("RETURN"), remap = false)
    private CompoundTag offgrid$saveCells(CompoundTag tag, HolderLookup.Provider registries, boolean spawnPacket) {
    //?} else {
    /*@ModifyReturnValue(method = "writeNBT", at = @At("RETURN"), remap = false)
    private CompoundTag offgrid$saveCells(CompoundTag tag, boolean spawnPacket) {
    *///?}
        if (!this.offgrid$cells.isEmpty()) {
            tag.put(offgrid$CELLS, AssembledCell.writeAll(this.offgrid$cells));
        }
        return tag;
    }

    @Inject(method = "readNBT", at = @At("TAIL"), remap = false)
    private void offgrid$loadCells(Level level, CompoundTag tag, boolean spawnData, CallbackInfo ci) {
        AssembledCell.readAll(tag.get(offgrid$CELLS), this.offgrid$cells);
        this.offgrid$blockEntities.clear();
    }
}
//?}
