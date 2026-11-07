//? if >=1.21 {
package dev.tazer.offgrid.mixin.compat.connectiblechains;

import com.evandev.connectiblechains.entity.ChainKnotEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.Connections;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("connectiblechains")
@Mixin(ChainKnotEntity.class)
public abstract class ChainKnotEntityMixin {

    @WrapOperation(method = {"survives", "getKnotScale", "computeKnotScale"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState offgrid$seeLoggedConnector(Level level, BlockPos pos, Operation<BlockState> original) {
        BlockState logged = Connections.loggedAt(level, pos);
        return logged != null ? logged : original.call(level, pos);
    }
}
//?}
