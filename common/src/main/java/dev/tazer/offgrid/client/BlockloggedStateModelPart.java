//? if >=26 {
/*package dev.tazer.offgrid.client;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockloggedStateModelPart implements BlockStateModelPart {

    protected final BlockStateModelPart part;
    private final List<AABB> open;

    public BlockloggedStateModelPart(BlockStateModelPart part, List<AABB> open) {
        this.part = part;
        this.open = open;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return QuadClipper.clip(this.part.getQuads(direction), this.open);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useAmbientOcclusion() {
        return this.part.useAmbientOcclusion();
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.part.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.part.materialFlags();
    }
}
*///?}
