package dev.tazer.offgrid.client;

import dev.tazer.offgrid.world.Blocklogging;
import dev.tazer.offgrid.world.Cell;
import dev.tazer.offgrid.world.OffgridChunk;
import dev.tazer.offgrid.world.OffgridIndex;
import dev.tazer.offgrid.world.Offsets;
import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.Overlays;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
//? if >=26 {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import java.util.function.Function;
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.function.Function;
//?}

public final class OverlayGeometry {

    private OverlayGeometry() {
    }

    public static boolean hasOverlays(BlockGetter getter, BlockPos origin) {
        return scan(getter, origin, null);
    }

    public static List<Entry> collect(BlockGetter getter, BlockPos origin) {
        List<Entry> entries = new ArrayList<>();
        scan(getter, origin, entries);
        return entries;
    }

    private static boolean scan(BlockGetter getter, BlockPos origin, @Nullable List<Entry> into) {
        OffgridIndex index = OffgridIndex.of(getter);
        if (index == null || index.isEmpty()) return false;
        OffgridChunk chunk = index.getChunk(ChunkPos.asLong(origin.getX() >> 4, origin.getZ() >> 4));
        if (chunk == null) return false;
        boolean found = false;
        int minY = origin.getY();
        int maxY = minY + 15;
        for (Long2ObjectMap.Entry<Cell> entry : chunk.getCells().long2ObjectEntrySet()) {
            Cell cell = entry.getValue();
            if (!cell.hasOverlays()) {
                continue;
            }
            int y = BlockPos.getY(entry.getLongKey());
            if (y < minY || y > maxY) {
                continue;
            }
            for (Overlay overlay : cell.overlays()) {
                BlockState state = overlay.getState();
                if (state.getRenderShape() != RenderShape.MODEL) {
                    continue;
                }
                if (into == null) return true;
                BlockPos host = BlockPos.of(entry.getLongKey());
                Blocklogging.Blocklog cut = Blocklogging.at(getter, host, overlay);
                into.add(new Entry(host, overlay, state, overlay.getOffset(), cut == null ? null : cut.open()));
                found = true;
            }
        }
        return found;
    }

    //? if >=26 {
    /*public static void render(BlockAndTintGetter region, BlockPos origin, Function<ChunkSectionLayer, VertexConsumer> buffers) {
        render(region, origin, collect(region, origin), output(buffers));
    }

    public static void render(BlockAndTintGetter region, BlockPos origin, BlockQuadOutput output) {
        render(region, origin, collect(region, origin), output);
    }

    public static void render(BlockAndTintGetter region, BlockPos origin, List<Entry> entries, BlockQuadOutput output) {
        if (entries.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ModelBlockRenderer loose = new ModelBlockRenderer(minecraft.options.ambientOcclusion().get(), false, minecraft.getBlockColors());
        ModelBlockRenderer culled = new ModelBlockRenderer(minecraft.options.ambientOcclusion().get(), true, minecraft.getBlockColors());
        BlockStateModelSet models = minecraft.getModelManager().getBlockStateModelSet();
        Offsets.suppress(() -> {
            for (Entry entry : entries) {
                (entry.culls() ? culled : loose).tesselateBlock(output, entry.x(origin), entry.y(origin), entry.z(origin), region, entry.host(), entry.state(), entry.model(models.get(entry.state())), entry.state().getSeed(entry.host()));
            }
        });
    }

    public static BlockQuadOutput output(Function<ChunkSectionLayer, VertexConsumer> buffers) {
        return (x, y, z, quad, instance) -> buffers.apply(quad.materialInfo().layer()).putBlockBakedQuad(x, y, z, quad, instance);
    }
    *///?} else {
    public static void render(BlockAndTintGetter region, BlockPos origin, Function<RenderType, VertexConsumer> buffers) {
        List<Entry> entries = collect(region, origin);
        if (entries.isEmpty()) return;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = new PoseStack();
        RandomSource random = RandomSource.create();
        Offsets.suppress(() -> {
            for (Entry entry : entries) {
                poseStack.pushPose();
                poseStack.translate(entry.x(origin), entry.y(origin), entry.z(origin));
                VertexConsumer buffer = buffers.apply(ItemBlockRenderTypes.getChunkRenderType(entry.state()));
                if (entry.open() == null) {
                    dispatcher.renderBatched(entry.state(), entry.host(), region, poseStack, buffer, entry.culls(), random);
                } else {
                    dispatcher.getModelRenderer().tesselateBlock(region, entry.model(dispatcher.getBlockModel(entry.state())), entry.state(), entry.host(), poseStack, buffer, entry.culls(), random, entry.state().getSeed(entry.host()), OverlayTexture.NO_OVERLAY);
                }
                poseStack.popPose();
            }
        });
    }
    //?}

    public record Entry(BlockPos host, Overlay overlay, BlockState state, Vec3 offset, @Nullable List<AABB> open) {

        public boolean culls() {
            return Overlays.isZero(this.offset);
        }

        //? if >=26 {
        /*public BlockStateModel model(BlockStateModel model) {
            return this.open == null ? model : BlockloggedStateModel.wrap(model, this.open);
        }
        *///?} else {
        public BakedModel model(BakedModel model) {
            return this.open == null ? model : new BlockloggedBakedModel(model, this.open);
        }
        //?}

        public float x(BlockPos origin) {
            return (float) (this.host.getX() - origin.getX() + this.offset.x);
        }

        public float y(BlockPos origin) {
            return (float) (this.host.getY() - origin.getY() + this.offset.y);
        }

        public float z(BlockPos origin) {
            return (float) (this.host.getZ() - origin.getZ() + this.offset.z);
        }
    }
}
