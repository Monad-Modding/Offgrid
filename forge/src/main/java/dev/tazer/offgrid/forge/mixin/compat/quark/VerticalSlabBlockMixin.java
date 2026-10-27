package dev.tazer.offgrid.forge.mixin.compat.quark;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.violetmoon.quark.content.building.block.VerticalSlabBlock;

@Pseudo
@IfModPresent("quark")
@Mixin(VerticalSlabBlock.class)
public abstract class VerticalSlabBlockMixin {

    @WrapMethod(method = "canBeReplaced")
    private boolean offgrid$mergeDifferentVerticalSlab(BlockState state, BlockPlaceContext context, Operation<Boolean> original) {
        return original.call(state, context) || MixedSlabs.canMergeInto(state, context);
    }
}
