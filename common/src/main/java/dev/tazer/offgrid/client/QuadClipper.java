package dev.tazer.offgrid.client;

import dev.tazer.offgrid.world.Layers;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
//? if >=26 {
/*import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector3fc;
*///?} else {
import net.minecraft.client.renderer.block.model.BakedQuad;
//?}

public final class QuadClipper {

    private static final int FLOATS = 5;
    private static final float EPSILON = 1.0E-5F;
    private static final double MIN_AREA = 1.0E-8;
    private static final float FACE_EPSILON = 1.0E-3F;

    private QuadClipper() {
    }

    public static List<BakedQuad> clip(List<BakedQuad> quads, List<AABB> open) {
        if (quads.isEmpty()) return quads;
        if (open instanceof Layers.HostOutline host) return underHost(quads, host);
        List<BakedQuad> clipped = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            clip(quad, open, clipped);
        }
        return clipped;
    }

    private static List<BakedQuad> underHost(List<BakedQuad> quads, List<AABB> host) {
        List<BakedQuad> kept = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            float[] corners = read(quad);
            int axis = direction(quad).getAxis().ordinal();
            float plane = corners[axis];
            boolean flat = true;
            for (int vertex = 1; vertex < 4; vertex++) {
                flat &= Math.abs(corners[vertex * FLOATS + axis] - plane) < FACE_EPSILON;
            }
            VoxelShape covered = flat ? coveredFaces(host, axis, plane) : Shapes.empty();
            if (covered.isEmpty()) {
                kept.add(quad);
                continue;
            }
            clip(quad, Shapes.join(Shapes.block(), covered, BooleanOp.ONLY_FIRST).toAabbs(), kept);
        }
        return kept;
    }

    private static VoxelShape coveredFaces(List<AABB> host, int axis, float plane) {
        VoxelShape covered = Shapes.empty();
        for (AABB box : host) {
            if (Math.abs(min(box, axis) - plane) < FACE_EPSILON || Math.abs(max(box, axis) - plane) < FACE_EPSILON) {
                covered = Shapes.or(covered, Shapes.create(switch (axis) {
                    case 0 -> new AABB(plane - FACE_EPSILON, box.minY, box.minZ, plane + FACE_EPSILON, box.maxY, box.maxZ);
                    case 1 -> new AABB(box.minX, plane - FACE_EPSILON, box.minZ, box.maxX, plane + FACE_EPSILON, box.maxZ);
                    default -> new AABB(box.minX, box.minY, plane - FACE_EPSILON, box.maxX, box.maxY, plane + FACE_EPSILON);
                }));
            }
        }
        return covered;
    }

    private static void clip(BakedQuad quad, List<AABB> open, List<BakedQuad> out) {
        float[] corners = read(quad);
        for (AABB box : open) {
            if (inside(corners, box)) {
                out.add(quad);
                return;
            }
        }
        float[] buffer = new float[FLOATS * 16];
        float[] scratch = new float[FLOATS * 16];
        for (AABB box : open) {
            System.arraycopy(corners, 0, buffer, 0, FLOATS * 4);
            int count = 4;
            for (int axis = 0; axis < 3 && count > 0; axis++) {
                count = clipPlane(buffer, count, scratch, axis, (float) min(box, axis), true);
                count = clipPlane(scratch, count, buffer, axis, (float) max(box, axis), false);
            }
            if (count >= 3 && area(buffer, count) > MIN_AREA) {
                emit(quad, buffer, count, out);
            }
        }
    }

    private static int clipPlane(float[] in, int count, float[] out, int axis, float plane, boolean lower) {
        int written = 0;
        for (int index = 0; index < count; index++) {
            int current = index * FLOATS;
            int next = ((index + 1) % count) * FLOATS;
            float a = in[current + axis];
            float b = in[next + axis];
            boolean aInside = lower ? a >= plane - EPSILON : a <= plane + EPSILON;
            boolean bInside = lower ? b >= plane - EPSILON : b <= plane + EPSILON;
            if (aInside) {
                System.arraycopy(in, current, out, written * FLOATS, FLOATS);
                written++;
            }
            if (aInside != bInside) {
                float t = (plane - a) / (b - a);
                for (int component = 0; component < FLOATS; component++) {
                    out[written * FLOATS + component] = in[current + component] + (in[next + component] - in[current + component]) * t;
                }
                out[written * FLOATS + axis] = plane;
                written++;
            }
        }
        return written;
    }

    private static boolean inside(float[] corners, AABB box) {
        for (int vertex = 0; vertex < 4; vertex++) {
            for (int axis = 0; axis < 3; axis++) {
                float value = corners[vertex * FLOATS + axis];
                if (value < min(box, axis) - EPSILON || value > max(box, axis) + EPSILON) return false;
            }
        }
        return true;
    }

    private static double min(AABB box, int axis) {
        return switch (axis) {
            case 0 -> box.minX;
            case 1 -> box.minY;
            default -> box.minZ;
        };
    }

    private static double max(AABB box, int axis) {
        return switch (axis) {
            case 0 -> box.maxX;
            case 1 -> box.maxY;
            default -> box.maxZ;
        };
    }

    private static double area(float[] polygon, int count) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        for (int index = 1; index + 1 < count; index++) {
            int a = index * FLOATS;
            int b = (index + 1) * FLOATS;
            double ax = polygon[a] - polygon[0];
            double ay = polygon[a + 1] - polygon[1];
            double az = polygon[a + 2] - polygon[2];
            double bx = polygon[b] - polygon[0];
            double by = polygon[b + 1] - polygon[1];
            double bz = polygon[b + 2] - polygon[2];
            x += ay * bz - az * by;
            y += az * bx - ax * bz;
            z += ax * by - ay * bx;
        }
        return x * x + y * y + z * z;
    }

    private static void emit(BakedQuad quad, float[] polygon, int count, List<BakedQuad> out) {
        for (int start = 1; start + 1 < count; start += 2) {
            int third = start + 2 < count ? start + 2 : start + 1;
            out.add(build(quad, polygon, 0, start, start + 1, third));
        }
    }

    //? if >=26 {
    /*private static float[] read(BakedQuad quad) {
        float[] corners = new float[FLOATS * 4];
        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3fc position = quad.position(vertex);
            long uv = quad.packedUV(vertex);
            int offset = vertex * FLOATS;
            corners[offset] = position.x();
            corners[offset + 1] = position.y();
            corners[offset + 2] = position.z();
            corners[offset + 3] = UVPair.unpackU(uv);
            corners[offset + 4] = UVPair.unpackV(uv);
        }
        return corners;
    }

    private static Direction direction(BakedQuad quad) {
        return quad.direction();
    }

    private static BakedQuad build(BakedQuad quad, float[] polygon, int first, int second, int third, int fourth) {
        return new BakedQuad(position(polygon, first), position(polygon, second), position(polygon, third), position(polygon, fourth), uv(polygon, first), uv(polygon, second), uv(polygon, third), uv(polygon, fourth), quad.direction(), quad.materialInfo());
    }

    private static Vector3fc position(float[] polygon, int vertex) {
        int offset = vertex * FLOATS;
        return new Vector3f(polygon[offset], polygon[offset + 1], polygon[offset + 2]);
    }

    private static long uv(float[] polygon, int vertex) {
        int offset = vertex * FLOATS;
        return UVPair.pack(polygon[offset + 3], polygon[offset + 4]);
    }
    *///?} else {
    private static float[] read(BakedQuad quad) {
        int[] vertices = quad.getVertices();
        int stride = vertices.length / 4;
        float[] corners = new float[FLOATS * 4];
        for (int vertex = 0; vertex < 4; vertex++) {
            int source = vertex * stride;
            int offset = vertex * FLOATS;
            corners[offset] = Float.intBitsToFloat(vertices[source]);
            corners[offset + 1] = Float.intBitsToFloat(vertices[source + 1]);
            corners[offset + 2] = Float.intBitsToFloat(vertices[source + 2]);
            corners[offset + 3] = Float.intBitsToFloat(vertices[source + 4]);
            corners[offset + 4] = Float.intBitsToFloat(vertices[source + 5]);
        }
        return corners;
    }

    private static Direction direction(BakedQuad quad) {
        return quad.getDirection();
    }

    private static BakedQuad build(BakedQuad quad, float[] polygon, int first, int second, int third, int fourth) {
        int[] source = quad.getVertices();
        int stride = source.length / 4;
        int[] vertices = new int[source.length];
        int[] order = {first, second, third, fourth};
        for (int vertex = 0; vertex < 4; vertex++) {
            int target = vertex * stride;
            int offset = order[vertex] * FLOATS;
            System.arraycopy(source, 0, vertices, target, stride);
            vertices[target] = Float.floatToRawIntBits(polygon[offset]);
            vertices[target + 1] = Float.floatToRawIntBits(polygon[offset + 1]);
            vertices[target + 2] = Float.floatToRawIntBits(polygon[offset + 2]);
            vertices[target + 4] = Float.floatToRawIntBits(polygon[offset + 3]);
            vertices[target + 5] = Float.floatToRawIntBits(polygon[offset + 4]);
        }
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }
    //?}
}
