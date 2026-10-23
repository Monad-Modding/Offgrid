package dev.tazer.offgrid.client;

import dev.tazer.offgrid.slab.MixedSlabBlock;
import dev.tazer.offgrid.slab.MixedSlabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class MixedSlabParticles {

    private MixedSlabParticles() {
    }

    public static boolean crack(ClientLevel level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        Minecraft minecraft = Minecraft.getInstance();
        if (!(state.getBlock() instanceof MixedSlabBlock) || minecraft.player == null) return false;
        Direction.Axis axis = state.getValue(MixedSlabBlock.AXIS);
        boolean second = MixedSlabs.targetsSecond(minecraft.player, pos, axis);
        BlockState half = MixedSlabs.half(level, pos, second);
        if (half == null) return false;
        AABB box = MixedSlabs.halfShape(axis, second).bounds();
        RandomSource random = level.getRandom();
        double x = pos.getX() + random.nextDouble() * (box.maxX - box.minX - 0.2) + 0.1 + box.minX;
        double y = pos.getY() + random.nextDouble() * (box.maxY - box.minY - 0.2) + 0.1 + box.minY;
        double z = pos.getZ() + random.nextDouble() * (box.maxZ - box.minZ - 0.2) + 0.1 + box.minZ;
        switch (side) {
            case DOWN -> y = pos.getY() + box.minY - 0.1;
            case UP -> y = pos.getY() + box.maxY + 0.1;
            case NORTH -> z = pos.getZ() + box.minZ - 0.1;
            case SOUTH -> z = pos.getZ() + box.maxZ + 0.1;
            case WEST -> x = pos.getX() + box.minX - 0.1;
            case EAST -> x = pos.getX() + box.maxX + 0.1;
        }
        minecraft.particleEngine.add(new TerrainParticle(level, x, y, z, 0.0, 0.0, 0.0, half, pos).setPower(0.2F).scale(0.6F));
        return true;
    }
}
