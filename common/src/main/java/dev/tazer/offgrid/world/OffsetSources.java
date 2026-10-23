package dev.tazer.offgrid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class OffsetSources {

    private static final int MAX_LINKS = 256;

    private OffsetSources() {
    }

    public static boolean justified(Level level, BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay self, boolean real) {
        if (Overlays.isZero(offset)) return true;
        for (Direction support : Support.supports(state)) {
            if (justifiedToward(level, new Link(host, state, offset, self, real), support)) return true;
        }
        return false;
    }

    private static boolean justifiedToward(Level level, Link start, Direction support) {
        Link link = start;
        for (int step = 0; step < MAX_LINKS; step++) {
            Support.Contact contact = Support.contact(level, link.host(), link.state(), link.offset(), link.self(), support);
            if (contact == null || !level.hasChunkAt(contact.cell())) return true;
            if (!reproduces(level, link, contact, support)) return false;
            Link next = continuation(level, link, contact.cell());
            if (next == null) return true;
            link = next;
        }
        return false;
    }

    private static boolean reproduces(Level level, Link link, Support.Contact contact, Direction support) {
        Direction face = support.getOpposite();
        BlockPos source = contact.cell();
        boolean ownCell = link.real() && source.equals(link.host());
        Surfaces.Surface surface = Surfaces.surfaceAt(level, source, link.self(), ownCell);
        BlockPos placed = source.relative(face);
        BlockState sourceState = ownCell ? Blocks.AIR.defaultBlockState() : Cells.realState(level, source);
        for (Vec3 candidate : Surfaces.candidatesFor(level, source, face, contact.location(), link.state(), surface)) {
            if (matches(link, placed, candidate, contact, support)) return true;
            Vec3 expected = Support.snap(level, placed, link.state(), candidate, source, sourceState, support, link.self());
            if (matches(link, placed, expected, contact, support)) return true;
        }
        return false;
    }

    private static boolean matches(Link link, BlockPos placed, Vec3 expected, Support.Contact contact, Direction support) {
        double[] wanted = {placed.getX() + expected.x, placed.getY() + expected.y, placed.getZ() + expected.z};
        double[] actual = {link.host().getX() + link.offset().x, link.host().getY() + link.offset().y, link.host().getZ() + link.offset().z};
        Direction.Axis along = Blocklogging.isAxial(link.state()) ? link.state().getValue(BlockStateProperties.AXIS) : null;
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis == along) {
                continue;
            }
            if (Math.abs(wanted[axis.ordinal()] - actual[axis.ordinal()]) > Support.EPSILON) return false;
        }
        if (along == null) return true;
        AABB footprint = OverlayShapes.of(link.state(), Vec3.ZERO).bounds();
        double end = support.getAxisDirection().getStep() < 0 ? footprint.min(along) : footprint.max(along);
        double reached = wanted[along.ordinal()] + end;
        double touching = along.choose(contact.location().x, contact.location().y, contact.location().z);
        return Math.abs(reached - touching) <= Support.EPSILON;
    }

    @Nullable
    private static Link continuation(Level level, Link link, BlockPos source) {
        if (!Blocklogging.isAxial(link.state())) return null;
        Direction.Axis axis = link.state().getValue(BlockStateProperties.AXIS);
        for (Overlay overlay : Cells.overlays(level, source)) {
            if (overlay != link.self() && links(link, source, overlay.getState(), overlay.getOffset(), axis)) return new Link(source, overlay.getState(), overlay.getOffset(), overlay, false);
        }
        if (link.real() && source.equals(link.host())) return null;
        BlockState real = Cells.realState(level, source);
        Vec3 offset = Offsets.of(level, source, real);
        if (offset != null && links(link, source, real, offset, axis)) return new Link(source, real, offset, null, true);
        return null;
    }

    private static boolean links(Link link, BlockPos pos, BlockState state, Vec3 offset, Direction.Axis axis) {
        if (!Blocklogging.isAxial(state) || state.getValue(BlockStateProperties.AXIS) != axis || Overlays.isZero(offset)) return false;
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other == axis) {
                continue;
            }
            double mine = other.choose(link.host().getX(), link.host().getY(), link.host().getZ()) + other.choose(link.offset().x, link.offset().y, link.offset().z);
            double theirs = other.choose(pos.getX(), pos.getY(), pos.getZ()) + other.choose(offset.x, offset.y, offset.z);
            if (Math.abs(mine - theirs) > Support.EPSILON) return false;
        }
        return true;
    }

    private record Link(BlockPos host, BlockState state, Vec3 offset, @Nullable Overlay self, boolean real) {
    }
}
