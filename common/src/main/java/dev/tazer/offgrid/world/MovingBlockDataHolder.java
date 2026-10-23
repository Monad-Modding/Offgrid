package dev.tazer.offgrid.world;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface MovingBlockDataHolder {

    @Nullable
    MovingBlockData offgrid$getMovingBlockData();

    void offgrid$setMovingBlockData(@Nullable MovingBlockData data);

    Map<AssembledCell.AssembledOverlay, BlockEntity> offgrid$getRenderedEntities();
}
