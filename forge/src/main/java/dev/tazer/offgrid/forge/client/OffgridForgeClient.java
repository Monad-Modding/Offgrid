package dev.tazer.offgrid.forge.client;

import dev.tazer.offgrid.client.MixedSlabColors;
import dev.tazer.offgrid.client.OffgridClient;
import dev.tazer.offgrid.client.OverlayBlockEntities;
import dev.tazer.offgrid.registry.OffgridBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

import java.util.Map;

public final class OffgridForgeClient {

    private OffgridForgeClient() {
    }

    public static void init(IEventBus modBus) {
        OffgridClient.init();
        modBus.addListener(OffgridForgeClient::onBlockColors);
        modBus.addListener(OffgridForgeClient::onModifyBakingResult);
        MinecraftForge.EVENT_BUS.addListener(OffgridForgeClient::onRenderStage);
        if (ModList.get().isLoaded("embeddium")) {
            EmbeddiumMeshing.register();
        }
    }

    private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(MixedSlabColors::getColor, OffgridBlocks.getMixedSlab());
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (BlockState state : OffgridBlocks.getMixedSlab().getStateDefinition().getPossibleStates()) {
            ResourceLocation location = BlockModelShaper.stateToModelLocation(state);
            BakedModel fallback = models.get(location);
            if (fallback != null) {
                models.put(location, new ForgeMixedSlabModel(fallback));
            }
        }
    }

    private static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        OverlayBlockEntities.render(event.getPoseStack(), event.getCamera().getPosition(), minecraft.renderBuffers().bufferSource(), event.getFrustum(), event.getPartialTick());
    }
}
