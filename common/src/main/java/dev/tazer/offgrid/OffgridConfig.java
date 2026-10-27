package dev.tazer.offgrid;

import dev.tazer.offgrid.platform.Services;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedName;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public final class OffgridConfig extends ReflectiveConfig {

    public static final OffgridConfig INSTANCE = createToml(Services.PLATFORM.getConfigDirectory(), Offgrid.MOD_ID, "common", OffgridConfig.class);

    @SerializedName("offsetting")
    public final TrackedValue<Boolean> offsetting = this.value(true);

    @SerializedName("overlaying")
    public final TrackedValue<Boolean> overlaying = this.value(true);

    @SerializedName("blocklogging")
    public final TrackedValue<Boolean> blocklogging = this.value(true);

    @SerializedName("layering")
    public final TrackedValue<Boolean> layering = this.value(true);

    @SerializedName("surface_attaching")
    public final TrackedValue<Boolean> surfaceAttaching = this.value(true);

    @SerializedName("any_face_paintings")
    public final TrackedValue<Boolean> anyFacePaintings = this.value(true);

    public static OffgridConfig get() {
        return INSTANCE;
    }
}
