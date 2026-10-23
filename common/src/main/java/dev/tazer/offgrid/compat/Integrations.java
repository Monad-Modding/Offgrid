package dev.tazer.offgrid.compat;

import dev.tazer.offgrid.compat.clutternomore.Blockloggables;
import dev.tazer.offgrid.compat.clutternomore.VerticalSlabs;
import dev.tazer.offgrid.platform.Services;
import dev.tazer.offgrid.slab.MixedSlabs;
import dev.tazer.offgrid.world.Blocklogging;

public final class Integrations {

    public static final String CLUTTER_NO_MORE = "clutternomore";
    public static final String SABLE = "sable";

    private Integrations() {
    }

    public static boolean isLoaded(String id) {
        return Services.PLATFORM.isModLoaded(id);
    }

    public static void init() {
        if (Services.PLATFORM.isModLoaded(CLUTTER_NO_MORE)) {
            MixedSlabs.addFamily(new VerticalSlabs());
            Blocklogging.addBlockloggable(Blockloggables::isBlockloggable);
        }
    }
}
