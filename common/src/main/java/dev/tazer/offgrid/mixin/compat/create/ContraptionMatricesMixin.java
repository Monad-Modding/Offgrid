//? if <26 {
package dev.tazer.offgrid.mixin.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import dev.tazer.offgrid.client.AssembledOverlayRender;
import dev.tazer.offgrid.compat.create.CarriedCells;
import dev.tazer.offgrid.compat.create.ContraptionCarrier;
import dev.tazer.offgrid.platform.condition.IfModPresent;
import dev.tazer.offgrid.world.AssembledCell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@IfModPresent("create")
@Mixin(ContraptionMatrices.class)
public abstract class ContraptionMatricesMixin {

    @Inject(method = "setup", at = @At("TAIL"), remap = false)
    private void offgrid$renderCarriedOverlays(PoseStack viewProjection, AbstractContraptionEntity entity, CallbackInfo ci) {
        CarriedCells carried = ((ContraptionCarrier) (Object) entity).offgrid$getCarried();
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || carried == null || carried.offgrid$getCarriedCells().isEmpty()) return;
        Contraption contraption = (Contraption) (Object) carried;
        ContraptionMatrices matrices = (ContraptionMatrices) (Object) this;
        PoseStack pose = matrices.getModelViewProjection();
        MultiBufferSource buffers = minecraft.renderBuffers().bufferSource();
        //? if >=1.21 {
        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        //?} else {
        /*float partialTicks = minecraft.getFrameTime();
        *///?}
        for (Map.Entry<BlockPos, AssembledCell> entry : carried.offgrid$getCarriedCells().entrySet()) {
            BlockPos local = entry.getKey();
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(local);
            BlockState host = info == null ? Blocks.AIR.defaultBlockState() : info.state();
            int packedLight = offgrid$light(level, matrices.getWorld(), local);
            pose.pushPose();
            pose.translate(local.getX(), local.getY(), local.getZ());
            for (AssembledCell.AssembledOverlay overlay : entry.getValue().overlays()) {
                AssembledOverlayRender.render(level, local, host, overlay, carried.offgrid$getCarriedBlockEntities(), pose, buffers, packedLight, partialTicks);
            }
            pose.popPose();
        }
    }

    @Unique
    private static int offgrid$light(Level level, Matrix4f toWorld, BlockPos local) {
        Vector4f world = new Vector4f(local.getX() + 0.5F, local.getY() + 0.5F, local.getZ() + 0.5F, 1.0F).mul(toWorld);
        return LevelRenderer.getLightColor(level, BlockPos.containing(world.x(), world.y(), world.z()));
    }
}
//?}
