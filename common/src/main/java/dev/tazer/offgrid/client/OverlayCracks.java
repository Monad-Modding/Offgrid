package dev.tazer.offgrid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.network.OverlayDestruction;
import dev.tazer.offgrid.world.Cells;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayHitResult;
import dev.tazer.offgrid.world.SubLevelSpace;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
//? if >=26 {
/*import dev.tazer.offgrid.world.Offsets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
*///?} else {
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.world.OverlayContext;
import net.minecraft.client.resources.model.ModelBakery;
//?}
//? if >=26.2 {
/*import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.util.RandomSource;
*///?}

import java.util.ArrayList;
import java.util.List;

public final class OverlayCracks {

    private static final double RANGE_SQR = 1024.0;
    private static final int EXPIRY_TICKS = 400;
    private static final Int2ObjectMap<Destruction> DESTRUCTIONS = new Int2ObjectOpenHashMap<>();

    @Nullable
    private static ClientLevel destructionLevel;

    private OverlayCracks() {
    }

    public static void apply(OverlayDestruction packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level != destructionLevel) {
            DESTRUCTIONS.clear();
            destructionLevel = level;
        }
        if (packet.stage() < 0 || packet.stage() > 9) {
            DESTRUCTIONS.remove(packet.breaker());
        } else {
            DESTRUCTIONS.put(packet.breaker(), new Destruction(BlockPos.of(packet.pos()), packet.id(), packet.stage(), level.getGameTime()));
        }
    }

    //? if >=26 {
    /*public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 camera) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        for (Crack crack : collect(level, camera)) {
            BlockState state = crack.overlay().getState();
            if (state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }
            BlockPos host = crack.host();
            Vec3 offset = crack.overlay().getOffset();
            poseStack.pushPose();
            poseStack.translate(host.getX() - camera.x + offset.x, host.getY() - camera.y + offset.y, host.getZ() - camera.z + offset.z);
            submitModel(poseStack, collector, minecraft.getModelManager().getBlockStateModelSet().get(state), state, host, crack.stage());
            poseStack.popPose();
        }
    }

    public static Vec3 shift(BlockPos pos, BlockState state) {
        Vec3 offset = Offsets.of(Minecraft.getInstance().level, pos, state);
        return offset == null ? Vec3.ZERO : offset;
    }
    *///?} else {
    public static void render(Minecraft minecraft, ClientLevel level, PoseStack poseStack, Vec3 camera, float partialTick) {
        for (Crack crack : collect(level, camera)) {
            Overlay overlay = crack.overlay();
            BlockPos host = crack.host();
            poseStack.pushPose();
            SubLevelView.enter(poseStack, camera, host, overlay.getOffset(), partialTick);
            //? if >=1.21 {
            VertexConsumer consumer = new SheetedDecalTextureGenerator(minecraft.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(crack.stage())), poseStack.last(), 1.0F);
            //?} else {
            /*VertexConsumer consumer = new SheetedDecalTextureGenerator(minecraft.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(crack.stage())), poseStack.last().pose(), poseStack.last().normal(), 1.0F);
            *///?}
            OverlayContext.run(level, host, overlay, () -> minecraft.getBlockRenderer().renderBreakingTexture(overlay.getState(), host, level, poseStack, consumer));
            poseStack.popPose();
        }
    }
    //?}

    //? if >=26.3 {
    /*private static void submitModel(PoseStack poseStack, SubmitNodeCollector collector, BlockStateModel model, BlockState state, BlockPos host, int stage) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(state.getSeed(host)), parts);
        collector.submitBreakingBlockModel(poseStack, List.copyOf(parts), stage, model.hasMaterialFlag(1));
    }
    *///?} else if >=26.2 {
    /*private static void submitModel(PoseStack poseStack, SubmitNodeCollector collector, BlockStateModel model, BlockState state, BlockPos host, int stage) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(state.getSeed(host)), parts);
        collector.submitBreakingBlockModel(poseStack, List.copyOf(parts), stage);
    }
    *///?} else if >=26 {
    /*private static void submitModel(PoseStack poseStack, SubmitNodeCollector collector, BlockStateModel model, BlockState state, BlockPos host, int stage) {
        collector.submitBreakingBlockModel(poseStack, model, state.getSeed(host), stage);
    }
    *///?}

    private static List<Crack> collect(ClientLevel level, Vec3 camera) {
        if (level != destructionLevel) {
            DESTRUCTIONS.clear();
            destructionLevel = level;
        }
        OverlayHitResult target = OverlayBreaking.getTarget();
        int stage = OverlayBreaking.getStage();
        boolean breaking = target != null && stage >= 0;
        if (!breaking && DESTRUCTIONS.isEmpty()) return List.of();
        List<Crack> cracks = new ArrayList<>();
        if (breaking) {
            add(cracks, level, target.getBlockPos(), target.getOverlayId(), stage);
        }
        long now = level.getGameTime();
        ObjectIterator<Destruction> iterator = DESTRUCTIONS.values().iterator();
        while (iterator.hasNext()) {
            Destruction destruction = iterator.next();
            if (now - destruction.updated() > EXPIRY_TICKS) {
                iterator.remove();
            } else if (SubLevelSpace.distanceSqr(level, destruction.host(), camera) <= RANGE_SQR) {
                add(cracks, level, destruction.host(), destruction.id(), destruction.stage());
            }
        }
        return cracks;
    }

    private static void add(List<Crack> cracks, ClientLevel level, BlockPos host, int id, int stage) {
        Overlay overlay = Cells.find(level, host, id);
        if (overlay == null) return;
        for (int i = 0; i < cracks.size(); i++) {
            Crack existing = cracks.get(i);
            if (existing.overlay() == overlay) {
                if (stage > existing.stage()) {
                    cracks.set(i, new Crack(host, overlay, stage));
                }
                return;
            }
        }
        cracks.add(new Crack(host, overlay, stage));
    }

    private record Destruction(BlockPos host, int id, int stage, long updated) {
    }

    private record Crack(BlockPos host, Overlay overlay, int stage) {
    }
}
