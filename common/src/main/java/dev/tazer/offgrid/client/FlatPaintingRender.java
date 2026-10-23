package dev.tazer.offgrid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
//? if <26 {
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.world.FlatPaintings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.world.entity.decoration.Painting;
//?}

public final class FlatPaintingRender {

    //? if <26 {
    //? if >=1.21 {
    private static final float UNIT = 1.0F;
    //?} else {
    /*private static final float UNIT = 16.0F;
    *///?}
    private static final float DEPTH = 0.03125F * UNIT;
    private static final float EDGE = 1.0F / 16.0F;
    //?}

    private FlatPaintingRender() {
    }

    public static void rotate(PoseStack poseStack, Direction face, Direction top) {
        boolean floor = face == Direction.UP;
        float yaw = 180.0F - (floor ? top.getOpposite() : top).toYRot();
        float pitch = floor ? 90.0F : -90.0F;
        //? if >=26.3 {
        /*poseStack.rotateDegrees(Axis.YP, yaw);
        poseStack.rotateDegrees(Axis.XP, pitch);
        *///?} else {
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        //?}
    }

    //? if <26 {
    public static void render(Painting painting, PoseStack poseStack, MultiBufferSource buffers) {
        PaintingTextureManager textures = Minecraft.getInstance().getPaintingTextures();
        TextureAtlasSprite back = textures.getBackSprite();
        TextureAtlasSprite front = textures.get(painting.getVariant().value());
        VertexConsumer consumer = buffers.getBuffer(RenderType.entitySolid(back.atlasLocation()));
        int width = FlatPaintings.width(painting);
        int height = FlatPaintings.height(painting);
        poseStack.pushPose();
        rotate(poseStack, painting.getDirection(), FlatPaintings.top(painting));
        //? if <1.21 {
        /*poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        *///?}
        PoseStack.Pose pose = poseStack.last();
        float left = -width * UNIT / 2.0F;
        float bottom = -height * UNIT / 2.0F;
        float backU0 = back.getU0();
        float backU1 = back.getU1();
        float backV0 = back.getV0();
        float backV1 = back.getV1();
        float edgeV = v(back, EDGE);
        float edgeU = u(back, EDGE);
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                float x1 = left + (column + 1) * UNIT;
                float x0 = left + column * UNIT;
                float y1 = bottom + (row + 1) * UNIT;
                float y0 = bottom + row * UNIT;
                int light = LevelRenderer.getLightColor(painting.level(), FlatPaintings.lightPos(painting, width, height, column, row));
                float frontU0 = u(front, (float) (width - column) / width);
                float frontU1 = u(front, (float) (width - (column + 1)) / width);
                float frontV0 = v(front, (float) (height - row) / height);
                float frontV1 = v(front, (float) (height - (row + 1)) / height);
                vertex(pose, consumer, x1, y0, frontU1, frontV0, -DEPTH, 0, 0, -1, light);
                vertex(pose, consumer, x0, y0, frontU0, frontV0, -DEPTH, 0, 0, -1, light);
                vertex(pose, consumer, x0, y1, frontU0, frontV1, -DEPTH, 0, 0, -1, light);
                vertex(pose, consumer, x1, y1, frontU1, frontV1, -DEPTH, 0, 0, -1, light);
                vertex(pose, consumer, x1, y1, backU1, backV0, DEPTH, 0, 0, 1, light);
                vertex(pose, consumer, x0, y1, backU0, backV0, DEPTH, 0, 0, 1, light);
                vertex(pose, consumer, x0, y0, backU0, backV1, DEPTH, 0, 0, 1, light);
                vertex(pose, consumer, x1, y0, backU1, backV1, DEPTH, 0, 0, 1, light);
                vertex(pose, consumer, x1, y1, backU0, backV0, -DEPTH, 0, 1, 0, light);
                vertex(pose, consumer, x0, y1, backU1, backV0, -DEPTH, 0, 1, 0, light);
                vertex(pose, consumer, x0, y1, backU1, edgeV, DEPTH, 0, 1, 0, light);
                vertex(pose, consumer, x1, y1, backU0, edgeV, DEPTH, 0, 1, 0, light);
                vertex(pose, consumer, x1, y0, backU0, backV0, DEPTH, 0, -1, 0, light);
                vertex(pose, consumer, x0, y0, backU1, backV0, DEPTH, 0, -1, 0, light);
                vertex(pose, consumer, x0, y0, backU1, edgeV, -DEPTH, 0, -1, 0, light);
                vertex(pose, consumer, x1, y0, backU0, edgeV, -DEPTH, 0, -1, 0, light);
                vertex(pose, consumer, x1, y1, edgeU, backV0, DEPTH, -1, 0, 0, light);
                vertex(pose, consumer, x1, y0, edgeU, backV1, DEPTH, -1, 0, 0, light);
                vertex(pose, consumer, x1, y0, backU0, backV1, -DEPTH, -1, 0, 0, light);
                vertex(pose, consumer, x1, y1, backU0, backV0, -DEPTH, -1, 0, 0, light);
                vertex(pose, consumer, x0, y1, edgeU, backV0, -DEPTH, 1, 0, 0, light);
                vertex(pose, consumer, x0, y0, edgeU, backV1, -DEPTH, 1, 0, 0, light);
                vertex(pose, consumer, x0, y0, backU0, backV1, DEPTH, 1, 0, 0, light);
                vertex(pose, consumer, x0, y1, backU0, backV0, DEPTH, 1, 0, 0, light);
            }
        }
        poseStack.popPose();
    }

    private static float u(TextureAtlasSprite sprite, float fraction) {
        //? if >=1.21 {
        return sprite.getU(fraction);
        //?} else {
        /*return sprite.getU(fraction * 16.0);
        *///?}
    }

    private static float v(TextureAtlasSprite sprite, float fraction) {
        //? if >=1.21 {
        return sprite.getV(fraction);
        //?} else {
        /*return sprite.getV(fraction * 16.0);
        *///?}
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light) {
        //? if >=1.21 {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, normalX, normalY, normalZ);
        //?} else {
        /*consumer.vertex(pose.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
        *///?}
    }
    //?}
}
