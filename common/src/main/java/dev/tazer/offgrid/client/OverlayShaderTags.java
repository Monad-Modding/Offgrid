package dev.tazer.offgrid.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.offgrid.compat.Integrations;
//? if >=1.21 {
import dev.tazer.offgrid.compat.iris.IrisOverlayTags;
//?}

public class OverlayShaderTags {

    private static final OverlayShaderTags NONE = new OverlayShaderTags();

    protected OverlayShaderTags() {
    }

    public static OverlayShaderTags create() {
        //? if >=1.21 {
        if (Integrations.isLoaded(Integrations.IRIS)) return new IrisOverlayTags();
        //?}
        return NONE;
    }

    public void enter(OverlayGeometry.Entry entry) {
    }

    public VertexConsumer tag(VertexConsumer consumer) {
        return consumer;
    }

    public void exit() {
    }
}
