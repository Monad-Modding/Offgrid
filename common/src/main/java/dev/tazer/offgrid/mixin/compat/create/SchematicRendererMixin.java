//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import dev.tazer.offgrid.client.AssembledOverlayRender;
import dev.tazer.offgrid.compat.create.PrintedOverlay;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Pseudo
@IfModPresent("create")
@Mixin(SchematicRenderer.class)
public abstract class SchematicRendererMixin {

    @Shadow(remap = false)
    @Final
    private BlockPos anchor;

    @Unique
    private final Map<PrintedOverlay, BlockEntity> offgrid$blockEntities = new HashMap<>();

    @Unique
    private Level offgrid$schematic;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void offgrid$captureSchematic(@Coerce Level schematic, CallbackInfo ci) {
        this.offgrid$schematic = schematic;
    }

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void offgrid$renderOverlays(PoseStack poseStack, @Coerce MultiBufferSource buffers, CallbackInfo ci) {
        if (this.offgrid$schematic == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        //? if >=1.21 {
        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        //?} else {
        /*float partialTicks = minecraft.getFrameTime();
        *///?}
        for (PrintedOverlay overlay : PrintedOverlay.collect(this.offgrid$schematic)) {
            BlockPos pos = overlay.pos();
            BlockEntity blockEntity = this.offgrid$blockEntities.computeIfAbsent(overlay, key -> AssembledOverlayRender.createBlockEntity(this.offgrid$schematic, pos, overlay.state(), overlay.tag()));
            poseStack.pushPose();
            poseStack.translate(pos.getX() - this.anchor.getX(), pos.getY() - this.anchor.getY(), pos.getZ() - this.anchor.getZ());
            AssembledOverlayRender.render(this.offgrid$schematic.getBlockState(pos), overlay.state(), overlay.offset(), blockEntity, poseStack, buffers, LightTexture.FULL_BRIGHT, partialTicks);
            poseStack.popPose();
        }
    }
}
//?}
