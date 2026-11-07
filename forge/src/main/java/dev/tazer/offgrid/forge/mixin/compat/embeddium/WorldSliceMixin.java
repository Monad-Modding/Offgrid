package dev.tazer.offgrid.forge.mixin.compat.embeddium;

import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.BlockEntities;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.OffgridIndexHolder;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@IfModPresent("embeddium")
@Mixin(value = WorldSlice.class, remap = false)
public abstract class WorldSliceMixin implements OffgridIndexHolder {

    @Shadow
    @Final
    public ClientLevel world;

    @Override
    @Nullable
    public OffgridIndex offgrid$getIndex() {
        return OffgridIndex.of(this.world);
    }

    @Inject(method = "getBlockEntity(III)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("HEAD"), cancellable = true)
    private void offgrid$viewOverlayBlockEntity(int x, int y, int z, CallbackInfoReturnable<BlockEntity> cir) {
        BlockEntity viewed = BlockEntities.viewedAt(x, y, z);
        if (viewed != null) {
            cir.setReturnValue(viewed);
        }
    }
}
