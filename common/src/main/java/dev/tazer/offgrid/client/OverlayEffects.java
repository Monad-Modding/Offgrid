package dev.tazer.offgrid.client;

import dev.tazer.offgrid.world.Overlay;
import dev.tazer.offgrid.world.OverlayShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class OverlayEffects {

    private OverlayEffects() {
    }

    public static void destroy(Level level, BlockPos host, Overlay overlay) {
        BlockState state = overlay.getState();
        VoxelShape shape = OverlayShapes.of(level, host, overlay);
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
        if (!shape.isEmpty()) {
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double sizeX = Math.min(1.0, maxX - minX);
                double sizeY = Math.min(1.0, maxY - minY);
                double sizeZ = Math.min(1.0, maxZ - minZ);
                int countX = Math.max(2, (int) Math.ceil(sizeX / 0.25));
                int countY = Math.max(2, (int) Math.ceil(sizeY / 0.25));
                int countZ = Math.max(2, (int) Math.ceil(sizeZ / 0.25));
                for (int x = 0; x < countX; x++) {
                    for (int y = 0; y < countY; y++) {
                        for (int z = 0; z < countZ; z++) {
                            double fractionX = (x + 0.5) / countX;
                            double fractionY = (y + 0.5) / countY;
                            double fractionZ = (z + 0.5) / countZ;
                            level.addParticle(particle, host.getX() + minX + fractionX * sizeX, host.getY() + minY + fractionY * sizeY, host.getZ() + minZ + fractionZ * sizeZ, fractionX - 0.5, fractionY - 0.5, fractionZ - 0.5);
                        }
                    }
                }
            });
        }
        Vec3 centre = OverlayShapes.centre(level, host, overlay);
        SoundType sound = state.getSoundType();
        level.playLocalSound(centre.x, centre.y, centre.z, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
    }

    public static void hit(Level level, BlockPos host, Overlay overlay, Direction face) {
        VoxelShape shape = OverlayShapes.of(level, host, overlay);
        if (shape.isEmpty()) return;
        AABB bounds = shape.bounds();
        RandomSource random = level.getRandom();
        double x = host.getX() + random.nextDouble() * (bounds.maxX - bounds.minX - 0.2) + 0.1 + bounds.minX;
        double y = host.getY() + random.nextDouble() * (bounds.maxY - bounds.minY - 0.2) + 0.1 + bounds.minY;
        double z = host.getZ() + random.nextDouble() * (bounds.maxZ - bounds.minZ - 0.2) + 0.1 + bounds.minZ;
        switch (face) {
            case DOWN -> y = host.getY() + bounds.minY - 0.1;
            case UP -> y = host.getY() + bounds.maxY + 0.1;
            case NORTH -> z = host.getZ() + bounds.minZ - 0.1;
            case SOUTH -> z = host.getZ() + bounds.maxZ + 0.1;
            case WEST -> x = host.getX() + bounds.minX - 0.1;
            case EAST -> x = host.getX() + bounds.maxX + 0.1;
        }
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, overlay.getState()), x, y, z, 0.0, 0.0, 0.0);
    }
}
