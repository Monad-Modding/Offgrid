package dev.tazer.offgrid.mixin.compat.clutternomore;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.clutternomore.block.VerticalSlabBlock;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModPresent("clutternomore")
@Mixin(VerticalSlabBlock.class)
public abstract class VerticalSlabBlockMixin {

    @ModifyReturnValue(method = "canBeReplaced(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/context/BlockPlaceContext;)Z", at = @At("RETURN"))
    private boolean offgrid$mergeDifferentVerticalSlab(boolean original, BlockState state, BlockPlaceContext context) {
        return original || MixedSlabs.canMergeInto(state, context);
    }
}
