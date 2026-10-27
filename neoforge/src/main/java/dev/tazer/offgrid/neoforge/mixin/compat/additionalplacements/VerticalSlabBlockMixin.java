//? if <26.2 {
package dev.tazer.offgrid.neoforge.mixin.compat.additionalplacements;

import com.firemerald.additionalplacements.block.VerticalSlabBlock;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@IfModPresent("additionalplacements")
@Mixin(VerticalSlabBlock.class)
public abstract class VerticalSlabBlockMixin {

    @WrapMethod(method = "canBeReplaced")
    private boolean offgrid$mergeDifferentVerticalSlab(BlockState state, BlockPlaceContext context, Operation<Boolean> original) {
        return original.call(state, context) || MixedSlabs.canMergeInto(state, context);
    }
}
//?}
