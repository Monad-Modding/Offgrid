package dev.tazer.offgrid.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.world.Connections;
import dev.tazer.offgrid.world.Offsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;
import java.util.function.Function;

public final class ForgeOverlayGeometry {

    private ForgeOverlayGeometry() {
    }

    public static void render(BlockAndTintGetter region, BlockPos origin, Function<RenderType, VertexConsumer> buffers) {
        List<OverlayGeometry.Entry> entries = OverlayGeometry.collect(region, origin);
        if (entries.isEmpty()) return;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = new PoseStack();
        RandomSource random = RandomSource.create();
        Offsets.suppress(() -> {
            for (OverlayGeometry.Entry entry : entries) {
                BlockState state = entry.state();
                BakedModel original = dispatcher.getBlockModel(state);
                BakedModel model = entry.open() == null ? original : new ForgeBlockloggedModel(original, entry.open());
                long seed = state.getSeed(entry.host());
                random.setSeed(seed);
                ModelData data = Connections.viewing(state, entry.host(), () -> model.getModelData(region, entry.host(), state, ModelData.EMPTY));
                for (RenderType type : model.getRenderTypes(state, random, data)) {
                    poseStack.pushPose();
                    poseStack.translate(entry.x(origin), entry.y(origin), entry.z(origin));
                    if (entry.open() == null) {
                        dispatcher.renderBatched(state, entry.host(), region, poseStack, buffers.apply(type), entry.culls(), random, data, type);
                    } else {
                        dispatcher.getModelRenderer().tesselateBlock(region, model, state, entry.host(), poseStack, buffers.apply(type), entry.culls(), random, seed, OverlayTexture.NO_OVERLAY, data, type);
                    }
                    poseStack.popPose();
                }
            }
        });
    }
}
