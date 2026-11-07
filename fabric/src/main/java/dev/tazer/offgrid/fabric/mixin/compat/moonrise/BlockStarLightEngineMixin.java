//? if >=1.21 {
package dev.tazer.offgrid.fabric.mixin.compat.moonrise;

import ca.spottedleaf.moonrise.patches.starlight.light.BlockStarLightEngine;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.compat.light.OverlayLight;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Pseudo
@IfModPresent("moonrise")
@Mixin(BlockStarLightEngine.class)
public abstract class BlockStarLightEngineMixin {

    @ModifyReturnValue(method = "getSources", at = @At("RETURN"))
    private List<BlockPos> offgrid$overlaySources(List<BlockPos> sources, LightChunkGetter lightAccess, ChunkAccess chunk) {
        return OverlayLight.withSources(sources, chunk);
    }
}
//?}
