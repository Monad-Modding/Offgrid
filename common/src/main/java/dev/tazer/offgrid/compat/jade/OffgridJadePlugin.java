package dev.tazer.offgrid.compat.jade;

import dev.tazer.offgrid.registry.OffgridBlocks;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
//? if >=26 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import snownee.jade.api.IWailaCommonRegistration;
*///?} else {
import snownee.jade.api.IWailaClientRegistration;
//?}

@WailaPlugin
public final class OffgridJadePlugin implements IWailaPlugin {

    //? if >=26 {
    /*@Override
    public void register(IWailaCommonRegistration registration) {
        registration.blockOperations().pick(ResourceKey.create(Registries.BLOCK, OffgridBlocks.MIXED_SLAB_ID));
    }
    *///?} else {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.usePickedResult(OffgridBlocks.getMixedSlab());
    }
    //?}
}
