package dev.tazer.offgrid.mixin.placement;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.world.Layers;
import dev.tazer.offgrid.world.Placement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @WrapMethod(method = "place")
    private InteractionResult offgrid$placeOffgrid(BlockPlaceContext context, Operation<InteractionResult> original) {
        BlockItem item = (BlockItem) (Object) this;
        return Layers.preserving(item, context, () -> Placement.place(item, context, original::call));
    }
}
