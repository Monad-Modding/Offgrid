//? if >=1.21 && <1.21.2 {
package dev.tazer.offgrid.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.platform.SablePlotPlatform;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@IfModPresent("sable")
@Mixin(ServerLevelPlot.class)
public abstract class ServerLevelPlotMixin {

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/platform/SablePlotPlatform;writeChunkAttachments(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void offgrid$writeAttachmentsPerChunk(SablePlotPlatform platform, CompoundTag tag, RegistryAccess registries, LevelChunk chunk, Operation<Void> original, @Local(ordinal = 2) CompoundTag chunkTag) {
        boolean perChunk = chunkTag.contains("sections") && chunkTag.contains("block_entities");
        original.call(platform, perChunk ? chunkTag : tag, registries, chunk);
    }
}
//?}
