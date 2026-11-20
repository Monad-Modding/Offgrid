//? if <26 {
package dev.tazer.offgrid.neoforge.client.compat.snowrealmagic;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.offgrid.client.OverlayShaderTags;
import dev.tazer.offgrid.neoforge.client.NeoForgeBlockloggedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import snownee.snow.block.entity.RenderData;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.RenderAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public final class SnowRealMagicOverlayRender {

    @Nullable
    private static final MethodHandle RENDER_DATA = renderDataHandle();

    private SnowRealMagicOverlayRender() {
    }

    public static boolean isSnowCovered(@Nullable BlockEntity blockEntity) {
        return renderData(blockEntity) instanceof RenderData;
    }

    public static void render(BlockEntity blockEntity, @Nullable List<AABB> open, AddSectionGeometryEvent.SectionRenderingContext context, OverlayShaderTags tags, BlockState state, BlockPos pos, PoseStack poseStack, boolean checkSides, RandomSource random, long seed, int packedOverlay) {
        if (!(renderData(blockEntity) instanceof RenderData data)) return;
        BlockAndTintGetter level = context.getRegion();
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        ClientHooks.renderHook(state, data.camo(), data.options(), null, new RenderAPI() {
            @Override
            public boolean render(BlockState part, BakedModel model, double yOffset, ModelPart kind) {
                if (kind == ModelPart.SNOW_OVERLAY) return false;
                BakedModel clipped = open == null || kind == ModelPart.DECORATION ? model : new NeoForgeBlockloggedModel(model, open.stream().map(box -> box.move(0.0, -yOffset, 0.0)).toList());
                random.setSeed(seed);
                Iterable<RenderType> types = kind == ModelPart.DECORATION ? List.of(RenderType.cutoutMipped()) : model.getRenderTypes(part, random, ModelData.EMPTY);
                for (RenderType type : types) {
                    poseStack.pushPose();
                    poseStack.translate(0.0, yOffset, 0.0);
                    renderer.tesselateBlock(level, clipped, part, pos, poseStack, tags.tag(context.getOrCreateChunkBuffer(type)), checkSides, random, seed, packedOverlay, ModelData.EMPTY, type);
                    poseStack.popPose();
                }
                return true;
            }

            @Override
            public BlockAndTintGetter level() {
                return level;
            }

            @Override
            public BlockPos pos() {
                return pos;
            }
        });
    }

    @Nullable
    private static Object renderData(@Nullable BlockEntity blockEntity) {
        if (blockEntity == null || RENDER_DATA == null) return null;
        try {
            return RENDER_DATA.invoke(blockEntity);
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Nullable
    private static MethodHandle renderDataHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(BlockEntity.class, "getRenderData", MethodType.methodType(Object.class));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
//?}
