package dev.tazer.offgrid.mixin.placement;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.OffgridSettings;
import dev.tazer.offgrid.world.SurfaceMounts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HangingEntityItem.class)
public abstract class HangingEntityItemMixin {

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;survives()Z"))
    private boolean offgrid$mountOnSurface(HangingEntity entity, Operation<Boolean> original, @Local(argsOnly = true) UseOnContext context) {
        SurfaceMounts.mount(entity, context);
        return original.call(entity);
    }

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean offgrid$attachToAnyFace(boolean original, @Local(argsOnly = true) Player placer, @Local(argsOnly = true) Direction direction, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) BlockPos pos) {
        if (original || !direction.getAxis().isVertical() || !OffgridSettings.of(placer.level()).anyFacePaintings()) return original;
        return placer.mayUseItemAt(pos, direction, stack);
    }
}
