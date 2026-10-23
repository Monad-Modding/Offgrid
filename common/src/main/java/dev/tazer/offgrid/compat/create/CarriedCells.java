//? if <26 {
package dev.tazer.offgrid.compat.create;

import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

public interface CarriedCells {

    Map<BlockPos, AssembledCell> offgrid$getCarriedCells();

    Map<AssembledCell.AssembledOverlay, BlockEntity> offgrid$getCarriedBlockEntities();
}
//?}
