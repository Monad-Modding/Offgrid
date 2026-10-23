package dev.tazer.offgrid;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Offgrid {

    public static final String MOD_ID = "offgrid";
    public static final Logger LOGGER = LoggerFactory.getLogger("Offgrid");

    private Offgrid() {
    }

    public static ResourceLocation id(String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
        //?} else {
        /*return new ResourceLocation(MOD_ID, path);
        *///?}
    }
}
