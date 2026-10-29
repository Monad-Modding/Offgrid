package dev.tazer.offgrid.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
//? if >=26 {
/*import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
*///?} else {
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
//?}

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public final class HostFaces extends AbstractList<AABB> {

    private static final Direction[] SIDES = {null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private final List<Face> faces;
    private final List<AABB> bounds;

    private HostFaces(List<Face> faces) {
        this.faces = List.copyOf(faces);
        List<AABB> bounds = new ArrayList<>(faces.size());
        for (Face face : faces) {
            bounds.add(face.bounds());
        }
        this.bounds = List.copyOf(bounds);
    }

    public static List<AABB> of(BlockState host, BlockPos pos, List<AABB> fallback) {
        if (host.getRenderShape() != RenderShape.MODEL) return fallback;
        List<Face> faces = new ArrayList<>();
        //? if >=26 {
        /*BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(host);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(host.getSeed(pos)), parts);
        for (BlockStateModelPart part : parts) {
            for (Direction side : SIDES) {
                add(faces, part.getQuads(side));
            }
        }
        *///?} else {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(host);
        RandomSource random = RandomSource.create();
        long seed = host.getSeed(pos);
        for (Direction side : SIDES) {
            random.setSeed(seed);
            add(faces, model.getQuads(host, side, random));
        }
        //?}
        return faces.isEmpty() ? fallback : new HostFaces(faces);
    }

    private static void add(List<Face> faces, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            Face face = QuadClipper.face(quad);
            if (face != null) {
                faces.add(face);
            }
        }
    }

    public List<Face> getFaces() {
        return this.faces;
    }

    @Override
    public AABB get(int index) {
        return this.bounds.get(index);
    }

    @Override
    public int size() {
        return this.bounds.size();
    }

    public record Face(int axis, float plane, float[] points, float winding) {

        public AABB bounds() {
            double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
            double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
            for (int vertex = 0; vertex < 4; vertex++) {
                for (int component = 0; component < 3; component++) {
                    min[component] = Math.min(min[component], this.points[vertex * 3 + component]);
                    max[component] = Math.max(max[component], this.points[vertex * 3 + component]);
                }
            }
            return new AABB(min[0], min[1], min[2], max[0], max[1], max[2]);
        }
    }
}
