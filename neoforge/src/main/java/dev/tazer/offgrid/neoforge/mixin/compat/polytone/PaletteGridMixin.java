//? if <26 {
package dev.tazer.offgrid.neoforge.mixin.compat.polytone;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.tazer.offgrid.neoforge.compat.polytone.PolytoneOverlayLights;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo // pretty sure Pseudo isn't necessary here, but all the other compat mixins have it, so
@IfModPresent("polytone")
@Mixin(targets = "net.mehvahdjukaar.polytone.content.shaders.voxel.PaletteGrid", remap = false)
public abstract class PaletteGridMixin {

    @Unique
    private static final String FILL_SECTION = "fillSection(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/core/SectionPos;Lnet/mehvahdjukaar/polytone/content/shaders/voxel/PaletteGrid$BlockEntitySlot;)V";

    @Unique
    private boolean offgrid$sectionHasCells;

    @WrapOperation(method = FILL_SECTION, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
    private boolean offgrid$keepOverlaySections(LevelChunkSection section, Operation<Boolean> original, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) SectionPos pos) {
        this.offgrid$sectionHasCells = PolytoneOverlayLights.hasCells(level, pos);
        return original.call(section) && !this.offgrid$sectionHasCells;
    }

    @WrapOperation(method = FILL_SECTION, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState offgrid$overlayInSection(LevelChunkSection section, int x, int y, int z, Operation<BlockState> original, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) SectionPos pos) {
        BlockState state = original.call(section, x, y, z);
        if (!this.offgrid$sectionHasCells) return state;
        return PolytoneOverlayLights.brightest(level, pos.minBlockX() + x, pos.minBlockY() + y, pos.minBlockZ() + z, state);
    }

    @WrapOperation(method = "updateBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState offgrid$overlayAtBlock(Level level, BlockPos pos, Operation<BlockState> original) {
        return PolytoneOverlayLights.brightest(level, pos.getX(), pos.getY(), pos.getZ(), original.call(level, pos));
    }
}
//?}
