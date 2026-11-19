//? if <26 {
package dev.tazer.offgrid.neoforge.compat.moonlight;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MoonlightPlacements {

    private MoonlightPlacements() {
    }

    @Nullable
    public static BlockState placementState(BlockItem item, BlockPlaceContext context) {
        AdditionalItemPlacement placement = AdditionalItemPlacementsAPI.getBehavior(item);
        return placement == null ? null : placement.overrideGetPlacementState(context);
    }
}
//?}
