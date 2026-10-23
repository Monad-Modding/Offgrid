package dev.tazer.offgrid.neoforge.client;

import dev.tazer.offgrid.Offgrid;
import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.client.OffgridClient;
import dev.tazer.offgrid.client.OverlayGeometry;
import dev.tazer.offgrid.registry.OffgridBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Map;
//? if >=26 {
/*import dev.tazer.offgrid.client.BlockloggedStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
*///?} else {
import dev.tazer.offgrid.client.OverlayBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//?}

@Mod(value = Offgrid.MOD_ID, dist = Dist.CLIENT)
public final class OffgridNeoForgeClient {

    public OffgridNeoForgeClient(IEventBus modBus) {
        OffgridClient.init();
        //? if >=26 {
        /*BlockloggedStateModel.setFactory(NeoForgeBlockloggedStateModel::new);
        *///?}
        modBus.addListener(OffgridNeoForgeClient::onBlockColors);
        modBus.addListener(OffgridNeoForgeClient::onModifyBakingResult);
        NeoForge.EVENT_BUS.addListener(OffgridNeoForgeClient::onSectionGeometry);
        //? if <26 {
        NeoForge.EVENT_BUS.addListener(OffgridNeoForgeClient::onRenderStage);
        //?}
    }

    //? if >=26 {
    /*private static void onBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(MixedSlabColors.tintSources(), OffgridBlocks.getMixedSlab());
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        for (BlockState state : OffgridBlocks.getMixedSlab().getStateDefinition().getPossibleStates()) {
            BlockStateModel fallback = models.get(state);
            if (fallback != null) {
                models.put(state, new NeoForgeMixedSlabModel(fallback));
            }
        }
    }

    private static void onSectionGeometry(AddSectionGeometryEvent event) {
        BlockPos origin = event.getSectionOrigin().immutable();
        List<OverlayGeometry.Entry> entries = OverlayGeometry.collect(event.getLevel(), origin);
        if (!entries.isEmpty()) {
            event.addRenderer(context -> OverlayGeometry.render(context.getRegion(), origin, entries, OverlayGeometry.output(context::getOrCreateChunkBuffer)));
        }
    }
    *///?} else {
    private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(MixedSlabColors::getColor, OffgridBlocks.getMixedSlab());
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (BlockState state : OffgridBlocks.getMixedSlab().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
            BakedModel fallback = models.get(location);
            if (fallback != null) {
                models.put(location, new NeoForgeMixedSlabModel(fallback));
            }
        }
    }

    private static void onSectionGeometry(AddSectionGeometryEvent event) {
        BlockPos origin = event.getSectionOrigin().immutable();
        List<NeoForgeOverlayGeometry.Prepared> prepared = NeoForgeOverlayGeometry.prepare(event.getLevel(), OverlayGeometry.collect(event.getLevel(), origin));
        if (!prepared.isEmpty()) {
            event.addRenderer(context -> NeoForgeOverlayGeometry.render(context, origin, prepared));
        }
    }

    private static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        OverlayBlockEntities.render(event.getPoseStack(), event.getCamera().getPosition(), minecraft.renderBuffers().bufferSource(), event.getFrustum(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }
    //?}
}
