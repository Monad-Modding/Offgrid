package dev.tazer.offgrid.mixin.server;

import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.OverlayContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundBlockUpdatePacket.class)
public abstract class ClientboundBlockUpdatePacketMixin {

    @Shadow
    @Final
    @Mutable
    private BlockState blockState;

    @Inject(method = "<init>(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void offgrid$sendRealState(BlockGetter getter, BlockPos pos, CallbackInfo ci) {
        if (getter instanceof Level level && OverlayContext.isActiveAt(level, pos)) {
            this.blockState = Cells.realState(level, pos);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void offgrid$sendRealGivenState(BlockPos pos, BlockState state, CallbackInfo ci) {
        OverlayContext.Frame frame = OverlayContext.current();
        if (frame != null && frame.getLevel() instanceof ServerLevel level && frame.getHost().equals(pos)) {
            this.blockState = Cells.realState(level, pos);
        }
    }
}
