package dev.tazer.offgrid.mixin.light;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridIndex;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {

    @ModifyReturnValue(method = "getEmission", at = @At("RETURN"))
    private int offgrid$overlayEmission(int original, long packedPos, BlockState state) {
        if (original >= 15) return original;
        LightEngineAccessor engine = (LightEngineAccessor) this;
        OffgridIndex index = OffgridIndex.of(engine.offgrid$getChunkSource().getLevel());
        if (index == null || index.isEmpty()) return original;
        Cell cell = index.getCell(packedPos);
        if (cell == null || cell.light() <= original) return original;
        return ((LayerLightSectionStorageAccessor) engine.offgrid$getStorage()).offgrid$lightOnInSection(SectionPos.blockToSection(packedPos)) ? cell.light() : original;
    }
}
