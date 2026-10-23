package dev.tazer.offgrid.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.world.EffectOffset;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class BlockMixin {

    @WrapOperation(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean offgrid$shiftDrop(Level level, Entity entity, Operation<Boolean> original) {
        Vec3 shift = EffectOffset.at(level, entity.getX(), entity.getY(), entity.getZ());
        if (shift != Vec3.ZERO) {
            entity.setPos(entity.getX() + shift.x, entity.getY() + shift.y, entity.getZ() + shift.z);
        }
        return original.call(level, entity);
    }
}
