//? if <26 {
package dev.tazer.offgrid.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.world.Connections;
import dev.tazer.offgrid.world.Offsets;
import dev.tazer.offgrid.world.OverlayContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

public final class NeoForgeOverlayGeometry {

    private NeoForgeOverlayGeometry() {
    }

    public static List<Prepared> prepare(Level level, List<OverlayGeometry.Entry> entries) {
        if (entries.isEmpty()) return List.of();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        List<Prepared> prepared = new ArrayList<>(entries.size());
        for (OverlayGeometry.Entry entry : entries) {
            BlockState state = entry.state();
            BakedModel original = dispatcher.getBlockModel(state);
            BakedModel model = entry.open() == null ? original : new NeoForgeBlockloggedModel(original, entry.open());
            BlockEntity blockEntity = state.hasBlockEntity() ? entry.overlay().materialize(level, entry.host()) : null;
            ModelData initial = blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData();
            ModelData data = OverlayContext.call(level, entry.host(), entry.overlay(), () -> Connections.viewing(state, entry.host(), () -> model.getModelData(level, entry.host(), state, initial)));
            prepared.add(new Prepared(entry, model, data));
        }
        return prepared;
    }

    public static void render(AddSectionGeometryEvent.SectionRenderingContext context, BlockPos origin, List<Prepared> prepared) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = context.getPoseStack();
        RandomSource random = RandomSource.create();
        Offsets.suppress(() -> {
            for (Prepared item : prepared) {
                OverlayGeometry.Entry entry = item.entry();
                BlockState state = entry.state();
                long seed = state.getSeed(entry.host());
                random.setSeed(seed);
                for (RenderType type : item.model().getRenderTypes(state, random, item.data())) {
                    poseStack.pushPose();
                    poseStack.translate(entry.x(origin), entry.y(origin), entry.z(origin));
                    dispatcher.getModelRenderer().tesselateBlock(context.getRegion(), item.model(), state, entry.host(), poseStack, context.getOrCreateChunkBuffer(type), entry.culls(), random, seed, OverlayTexture.NO_OVERLAY, item.data(), type);
                    poseStack.popPose();
                }
            }
        });
    }

    public record Prepared(OverlayGeometry.Entry entry, BakedModel model, ModelData data) {
    }
}
//?}
