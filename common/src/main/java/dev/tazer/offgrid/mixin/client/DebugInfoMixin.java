package dev.tazer.offgrid.mixin.client;

import dev.tazer.offgrid.client.OffgridClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;
//? if >=26 {
/*import net.minecraft.client.gui.components.debug.DebugEntryLookingAt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Shadow;
//?}

//? if >=26 {
/*@Mixin(DebugEntryLookingAt.DebugEntryLookingAtState.class)
*///?} else {
@Mixin(DebugScreenOverlay.class)
//?}
public abstract class DebugInfoMixin {

    //? if >=26 {
    /*@Inject(method = "extractInfo", at = @At("RETURN"))
    private void offgrid$offgridInfo(List<String> lines, Level level, BlockPos pos, CallbackInfo ci) {
        if ((Object) this instanceof DebugEntryLookingAt.BlockStateInfo) {
            lines.addAll(OffgridClient.debugLines(level, pos));
        }
    }
    *///?} else {
    @Shadow
    private HitResult block;

    @ModifyReturnValue(method = "getSystemInformation", at = @At("RETURN"))
    private List<String> offgrid$offgridInfo(List<String> lines) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && this.block instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            lines.addAll(OffgridClient.debugLines(minecraft.level, hit.getBlockPos()));
        }
        return lines;
    }
    //?}
}
