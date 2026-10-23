package dev.tazer.offgrid.mixin.access;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public interface BlockItemAccessor {

    @Nullable
    @Invoker("getPlacementState")
    BlockState offgrid$getPlacementState(BlockPlaceContext context);
}
