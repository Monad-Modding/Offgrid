package dev.tazer.offgrid.mixin.placement;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.OffgridSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
//? if >=26.2 {
/*import net.minecraft.world.entity.EntityTypes;
*///?}

@Mixin(HangingEntityItem.class)
public abstract class HangingEntityItemMixin {

    @Shadow
    @Final
    private EntityType<? extends HangingEntity> type;

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean offgrid$attachToAnyFace(boolean original, @Local(argsOnly = true) Player placer, @Local(argsOnly = true) Direction direction, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) BlockPos pos) {
        //? if >=26.2 {
        /*boolean painting = this.type == EntityTypes.PAINTING;
        *///?} else {
        boolean painting = this.type == EntityType.PAINTING;
        //?}
        if (original || !painting || !direction.getAxis().isVertical() || !OffgridSettings.of(placer.level()).anyFacePaintings()) return original;
        return placer.mayUseItemAt(pos, direction, stack);
    }
}
