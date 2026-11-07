package dev.tazer.offgrid.neoforge.mixin.compat.moonrise;

import ca.spottedleaf.moonrise.neoforge.NeoForgeHooks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.compat.light.OverlayLight;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("moonrise")
@Mixin(NeoForgeHooks.class)
public abstract class NeoForgeHooksMixin {

    @ModifyReturnValue(method = "getLightEmission", at = @At("RETURN"))
    private int offgrid$overlayEmission(int original, BlockState state, BlockGetter level, BlockPos pos) {
        return OverlayLight.emission(level, pos, original);
    }
}
