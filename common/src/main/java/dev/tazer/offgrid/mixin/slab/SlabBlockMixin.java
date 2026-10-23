package dev.tazer.offgrid.mixin.slab;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SlabBlock.class)
public abstract class SlabBlockMixin {

    @ModifyReturnValue(method = "canBeReplaced", at = @At("RETURN"))
    private boolean offgrid$mergeDifferentSlab(boolean original, BlockState state, BlockPlaceContext context) {
        return original || MixedSlabs.canMergeInto(state, context);
    }
}
