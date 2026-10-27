package dev.tazer.offgrid.mixin.slab;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SlabBlock.class)
public abstract class SlabBlockMixin {

    @WrapMethod(method = "canBeReplaced")
    private boolean offgrid$mergeDifferentSlab(BlockState state, BlockPlaceContext context, Operation<Boolean> original) {
        return original.call(state, context) || MixedSlabs.canMergeInto(state, context);
    }
}
