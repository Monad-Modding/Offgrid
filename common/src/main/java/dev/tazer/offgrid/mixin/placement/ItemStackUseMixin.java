package dev.tazer.offgrid.mixin.placement;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackUseMixin {

    @WrapMethod(method = "useOn")
    private InteractionResult offgrid$rememberPlacement(UseOnContext context, Operation<InteractionResult> original) {
        return SurfaceMounts.placing(context, () -> original.call(context));
    }
}
