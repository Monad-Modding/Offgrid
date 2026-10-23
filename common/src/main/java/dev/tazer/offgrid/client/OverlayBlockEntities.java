package dev.tazer.offgrid.client;

import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayContext;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
//? if >=26 {
/*import dev.tazer.offgrid.mixin.client.render.BlockEntityRenderStateAccessor;
import dev.tazer.offgrid.world.Offsets;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
//?}

public final class OverlayBlockEntities {

    private static final double MAX_DISTANCE = 128.0;
    //? if >=26 {
    /*private static final Map<BlockEntityRenderState, Vec3> OFFSETS = new IdentityHashMap<>();
    *///?}

    private OverlayBlockEntities() {
    }

    //? if >=26 {
    /*public static void extract(float partialTick, List<BlockEntityRenderState> output) {
        OFFSETS.clear();
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        OffgridIndex index = OffgridIndex.of(level);
        if (level == null || index == null || index.isEmpty()) return;
        BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        for (Long2ObjectMap.Entry<OffgridChunk> chunkEntry : index.getChunks().long2ObjectEntrySet()) {
            if (!withinRange(minecraft, camera, chunkEntry.getLongKey())) {
                continue;
            }
            for (Long2ObjectMap.Entry<Cell> cellEntry : chunkEntry.getValue().getCells().long2ObjectEntrySet()) {
                Cell cell = cellEntry.getValue();
                if (!cell.hasOverlays()) {
                    continue;
                }
                BlockPos host = BlockPos.of(cellEntry.getLongKey());
                for (Overlay overlay : cell.overlays()) {
                    if (!overlay.getState().hasBlockEntity()) {
                        continue;
                    }
                    BlockEntity blockEntity = overlay.materialize(level, host);
                    if (blockEntity == null || dispatcher.getRenderer(blockEntity) == null) {
                        continue;
                    }
                    //? if >=26.2 {
                    /^BlockEntityRenderState state = OverlayContext.call(level, host, overlay, () -> dispatcher.tryExtractRenderState(blockEntity, partialTick, null, false));
                    ^///?} else {
                    BlockEntityRenderState state = OverlayContext.call(level, host, overlay, () -> dispatcher.tryExtractRenderState(blockEntity, partialTick, null));
                    //?}
                    if (state != null) {
                        output.add(state);
                        OFFSETS.put(state, overlay.getOffset());
                    }
                }
            }
        }
    }

    @Nullable
    public static Vec3 offsetOf(BlockEntityRenderState state) {
        Vec3 overlay = OFFSETS.get(state);
        if (overlay != null) return overlay;
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : Offsets.of(level, state.blockPos, ((BlockEntityRenderStateAccessor) state).offgrid$getBlockState());
    }
    *///?} else {
    public static void render(PoseStack poseStack, Vec3 camera, MultiBufferSource buffers, @Nullable Frustum frustum, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        OffgridIndex index = OffgridIndex.of(level);
        if (level == null || index == null || index.isEmpty()) return;
        BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        for (Long2ObjectMap.Entry<OffgridChunk> chunkEntry : index.getChunks().long2ObjectEntrySet()) {
            long chunk = chunkEntry.getLongKey();
            boolean subLevel = SubLevelView.contains(new BlockPos(SectionPos.sectionToBlockCoord(ChunkPos.getX(chunk), 8), 0, SectionPos.sectionToBlockCoord(ChunkPos.getZ(chunk), 8)));
            if (!subLevel && !withinRange(minecraft, camera, chunk)) {
                continue;
            }
            for (Long2ObjectMap.Entry<Cell> cellEntry : chunkEntry.getValue().getCells().long2ObjectEntrySet()) {
                Cell cell = cellEntry.getValue();
                if (!cell.hasOverlays()) {
                    continue;
                }
                BlockPos host = BlockPos.of(cellEntry.getLongKey());
                if (!subLevel && frustum != null && !frustum.isVisible(new AABB(host).inflate(1.0))) {
                    continue;
                }
                for (Overlay overlay : cell.overlays()) {
                    if (!overlay.getState().hasBlockEntity()) {
                        continue;
                    }
                    BlockEntity blockEntity = overlay.materialize(level, host);
                    BlockEntityRenderer<BlockEntity> renderer = blockEntity == null ? null : dispatcher.getRenderer(blockEntity);
                    if (renderer == null) {
                        continue;
                    }
                    poseStack.pushPose();
                    SubLevelView.enter(poseStack, camera, host, overlay.getOffset(), partialTick);
                    if (subLevel) {
                        int light = LevelRenderer.getLightColor(level, host);
                        OverlayContext.run(level, host, overlay, () -> renderer.render(blockEntity, partialTick, poseStack, buffers, light, OverlayTexture.NO_OVERLAY));
                    } else {
                        OverlayContext.run(level, host, overlay, () -> dispatcher.render(blockEntity, partialTick, poseStack, buffers));
                    }
                    poseStack.popPose();
                }
            }
        }
        OverlayCracks.render(minecraft, level, poseStack, camera, partialTick);
    }
    //?}

    private static boolean withinRange(Minecraft minecraft, Vec3 camera, long chunk) {
        double range = Math.min(MAX_DISTANCE, minecraft.options.getEffectiveRenderDistance() * 16.0) + 16.0;
        double dx = SectionPos.sectionToBlockCoord(ChunkPos.getX(chunk), 8) - camera.x;
        double dz = SectionPos.sectionToBlockCoord(ChunkPos.getZ(chunk), 8) - camera.z;
        return dx * dx + dz * dz <= range * range;
    }
}
