//? if <26 {
package dev.tazer.offgrid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.world.AssembledCell;
import dev.tazer.offgrid.world.Blocklogging;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class AssembledOverlayRender {

    private AssembledOverlayRender() {
    }

    public static void render(Level level, BlockPos pos, BlockState host, AssembledCell.AssembledOverlay overlay, Map<AssembledCell.AssembledOverlay, BlockEntity> blockEntities, PoseStack pose, MultiBufferSource buffers, int light, float partialTicks) {
        BlockEntity blockEntity = blockEntities.computeIfAbsent(overlay, key -> createBlockEntity(level, pos, overlay.state(), overlay.tag()));
        render(host, overlay.state(), overlay.offset(), blockEntity, pose, buffers, light, partialTicks);
    }

    public static void render(BlockState host, BlockState state, Vec3 offset, @Nullable BlockEntity blockEntity, PoseStack pose, MultiBufferSource buffers, int light, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);
        renderModel(minecraft.getBlockRenderer(), host, state, offset, pose, buffers, light);
        BlockEntityRenderer<BlockEntity> renderer = blockEntity == null ? null : minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        if (renderer != null) {
            renderer.render(blockEntity, partialTicks, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }

    private static void renderModel(BlockRenderDispatcher dispatcher, BlockState host, BlockState state, Vec3 offset, PoseStack pose, MultiBufferSource buffers, int light) {
        if (state.getRenderShape() != RenderShape.MODEL) return;
        BakedModel model = dispatcher.getBlockModel(state);
        Blocklogging.Blocklog cut = Blocklogging.of(state, host, offset);
        if (cut != null) {
            model = new BlockloggedBakedModel(model, cut.open());
        }
        int color = Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        dispatcher.getModelRenderer().renderModel(pose.last(), buffers.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)), state, model, red, green, blue, light, OverlayTexture.NO_OVERLAY);
    }

    @Nullable
    public static BlockEntity createBlockEntity(Level level, BlockPos pos, BlockState state, @Nullable CompoundTag tag) {
        if (!state.hasBlockEntity() || !(state.getBlock() instanceof EntityBlock block)) return null;
        //? if >=1.21 {
        BlockEntity created = tag == null ? block.newBlockEntity(pos, state) : BlockEntity.loadStatic(pos, state, tag, level.registryAccess());
        //?} else {
        /*BlockEntity created = tag == null ? block.newBlockEntity(pos, state) : BlockEntity.loadStatic(pos, state, tag);
        *///?}
        if (created != null) {
            created.setLevel(level);
        }
        return created;
    }
}
//?}
