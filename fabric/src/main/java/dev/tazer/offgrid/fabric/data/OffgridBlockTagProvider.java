package dev.tazer.offgrid.fabric.data;

import dev.tazer.offgrid.OffgridTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
//? if >=26 {
/*import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.data.tags.TagAppender;
*///?} else {
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
//?}

import java.util.concurrent.CompletableFuture;

//? if >=26 {
/*public final class OffgridBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
*///?} else {
public final class OffgridBlockTagProvider extends FabricTagProvider.BlockTagProvider {
//?}

    private static final String[] DYES = {"white", "light_gray", "gray", "black", "brown", "yellow", "lime", "green", "cyan", "blue", "light_blue", "magenta", "pink", "purple", "red", "orange"};
    private static final String[] SCONCE_WALLS = {"supplementaries:sconce_wall", "supplementaries:sconce_wall_soul", "supplementaries:sconce_wall_green", "supplementaries:sconce_wall_ender", "supplementaries:sconce_wall_glow", "supplementaries:sconce_wall_nether_brass"};
    private static final String[] WEATHERING = {"", "exposed_", "weathered_", "oxidized_", "waxed_", "waxed_exposed_", "waxed_weathered_", "waxed_oxidized_"};
    private static final String[] HEADS = {"supplementaries:spider_head", "supplementaries:enderman_head", "caverns_and_chasms:deeper_head", "caverns_and_chasms:evendeeper_head", "caverns_and_chasms:peeper_head", "caverns_and_chasms:mime_head", "piglinproliferation:piglin_alchemist_head", "piglinproliferation:piglin_traveler_head", "piglinproliferation:piglin_brute_head", "piglinproliferation:zombified_piglin_head", "nirvana:reefer_head"};
    private static final String[] METAL = {"andesite", "copper", "brass", "zinc", "industrial_iron"};
    public OffgridBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH);
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.LANTERN, Blocks.SOUL_LANTERN, Blocks.END_ROD, Blocks.MOSS_CARPET, Blocks.SNOW);
        //? if >=1.21 {
        this.tags(OffgridTags.CAN_OVERLAY, conventional("skulls"), conventional("buds"), conventional("clusters"));
        //?} else {
        /*this.blocks(OffgridTags.CAN_OVERLAY, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD);
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER);
        this.optionalTags(OffgridTags.CAN_OVERLAY, "c:buds", "c:clusters");
        *///?}
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.SEA_PICKLE, Blocks.TURTLE_EGG, Blocks.SNIFFER_EGG, Blocks.LEVER);
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.CAKE);
        this.tags(OffgridTags.CAN_OVERLAY, BlockTags.CANDLE_CAKES);
        this.tags(OffgridTags.CAN_OVERLAY, BlockTags.CANDLES, BlockTags.BUTTONS, BlockTags.PRESSURE_PLATES, BlockTags.WOOL_CARPETS, BlockTags.STANDING_SIGNS, BlockTags.WALL_SIGNS, BlockTags.FLOWER_POTS, BlockTags.BANNERS);
        //? if >=1.21.4 {
        /*this.blocks(OffgridTags.CAN_OVERLAY, Blocks.PALE_MOSS_CARPET);
        *///?}
        //? if >=1.21.9 {
        /*this.blocks(OffgridTags.CAN_OVERLAY, Blocks.COPPER_TORCH, Blocks.COPPER_WALL_TORCH);
        this.tags(OffgridTags.CAN_OVERLAY, BlockTags.LIGHTNING_RODS);
        *///?} else {
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.LIGHTNING_ROD);
        //?}
        //? if >=26.2 {
        /*this.blocks(OffgridTags.CAN_OVERLAY, Blocks.COPPER_LANTERN.asList().toArray(Block[]::new));
        *///?} else if >=1.21.9 {
        /*this.blocks(OffgridTags.CAN_OVERLAY, Blocks.COPPER_LANTERN.unaffected(), Blocks.COPPER_LANTERN.exposed(), Blocks.COPPER_LANTERN.weathered(), Blocks.COPPER_LANTERN.oxidized());
        this.blocks(OffgridTags.CAN_OVERLAY, Blocks.COPPER_LANTERN.waxed(), Blocks.COPPER_LANTERN.waxedExposed(), Blocks.COPPER_LANTERN.waxedWeathered(), Blocks.COPPER_LANTERN.waxedOxidized());
        *///?}
        this.tags(OffgridTags.CAN_OVERLAY, OffgridTags.CAN_BLOCKLOG);
        this.tags(OffgridTags.CAN_OFFSET, OffgridTags.CAN_OVERLAY);
        this.blocks(OffgridTags.CAN_OFFSET, Blocks.TRIPWIRE_HOOK, Blocks.DRAGON_EGG, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD);
        this.tags(OffgridTags.FACING_ATTACHING, BlockTags.WALL_SIGNS);
        this.blocks(OffgridTags.FACING_ATTACHING, Blocks.WALL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.REDSTONE_WALL_TORCH);
        //? if >=26.2 {
        /*this.blocks(OffgridTags.FACING_ATTACHING, Blocks.WALL_BANNER.asList().toArray(Block[]::new));
        *///?} else {
        this.blocks(OffgridTags.FACING_ATTACHING, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER);
        this.blocks(OffgridTags.FACING_ATTACHING, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER);
        //?}
        this.blocks(OffgridTags.FACING_ATTACHING, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.ZOMBIE_WALL_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.PIGLIN_WALL_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.PLAYER_WALL_HEAD);
        //? if >=1.21 {
        this.tags(OffgridTags.FACING_ATTACHING, conventional("buds"), conventional("clusters"));
        //?} else {
        /*this.blocks(OffgridTags.FACING_ATTACHING, Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER);
        this.optionalTags(OffgridTags.FACING_ATTACHING, "c:buds", "c:clusters");
        *///?}
        this.blocks(OffgridTags.FACING_ATTACHING, Blocks.END_ROD, Blocks.TRIPWIRE_HOOK);
        //? if >=1.21.9 {
        /*this.blocks(OffgridTags.FACING_ATTACHING, Blocks.COPPER_WALL_TORCH);
        this.tags(OffgridTags.FACING_ATTACHING, BlockTags.LIGHTNING_RODS);
        *///?} else {
        this.blocks(OffgridTags.FACING_ATTACHING, Blocks.LIGHTNING_ROD);
        //?}
        //? if >=1.21 {
        this.tags(OffgridTags.CAN_BLOCKLOG, conventional("glass_panes"), conventional("chains"), conventional("fences"), BlockTags.FENCES, BlockTags.WALLS);
        //?} else {
        /*this.optionalTags(OffgridTags.CAN_BLOCKLOG, "c:glass_panes", "forge:glass_panes", "forge:fences");
        this.tags(OffgridTags.CAN_BLOCKLOG, BlockTags.FENCES, BlockTags.WALLS);
        this.blocks(OffgridTags.CAN_BLOCKLOG, Blocks.CHAIN);
        *///?}
        //? if >=1.21.9 {
        /*this.tags(OffgridTags.CAN_BLOCKLOG, conventional("bars"), BlockTags.BARS, BlockTags.CHAINS);
        *///?} else {
        this.blocks(OffgridTags.CAN_BLOCKLOG, Blocks.IRON_BARS);
        this.optional(OffgridTags.CAN_BLOCKLOG, metal("create:%_bars"));
        this.optional(OffgridTags.CAN_BLOCKLOG, metal("createdeco:%_bars"));
        this.optional(OffgridTags.CAN_BLOCKLOG, metal("createdeco:%_bars_overlay"));
        this.optional(OffgridTags.CAN_BLOCKLOG, "createdeco:iron_bars_overlay");
        //?}
        this.tags(OffgridTags.BLOCKLOGGABLE, BlockTags.SLABS, BlockTags.STAIRS);
        this.blocks(OffgridTags.BLOCKLOGGABLE, Blocks.SNOW);
        this.tags(OffgridTags.LAYERS, BlockTags.WOOL_CARPETS);
        this.blocks(OffgridTags.LAYERS, Blocks.MOSS_CARPET, Blocks.SNOW);
        //? if >=1.21.4 {
        /*this.blocks(OffgridTags.LAYERS, Blocks.PALE_MOSS_CARPET);
        *///?}
        this.addIntegrationTags();
    }

    private void addIntegrationTags() {
        this.optional(OffgridTags.CAN_OFFSET, "create:analog_lever", "supplementaries:crank", "supplementaries:wind_vane", "abundant_atmosphere:mud_lamp", "caverns_and_chasms:dimmer");
        this.optional(OffgridTags.CAN_OFFSET, "supplementaries:item_shelf", "supplementaries:flower_box", "simulated:iron_handle", "simulated:copper_handle");
        this.optional(OffgridTags.CAN_OFFSET, "supplementaries:jar", "supplementaries:jar_boat", "supplementaries:cage", "supplementaries:statue", "supplementaries:lunch_basket");
        this.optional(OffgridTags.CAN_OFFSET, "farmersdelight:cutting_board", "nomansland:tap", "etched:radio", "caverns_and_chasms:toolbox");
        this.optional(OffgridTags.CAN_OFFSET, "envelope:paper_box", "envelope:letter", "envelope:package", "envelope:sealed_package");
        this.optional(OffgridTags.CAN_OFFSET, dyed("another_furniture:%_lamp"));
        this.optional(OffgridTags.CAN_OFFSET, weathered("caverns_and_chasms:%floodlight"));
        this.optional(OffgridTags.CAN_OFFSET, dyed("simulated:%_handle"));
        this.optional(OffgridTags.CAN_OFFSET, dyed("simulated:%_nameplate"));

        this.optional(OffgridTags.CAN_OVERLAY, "simulated:linked_typewriter");
        this.optional(OffgridTags.CAN_OVERLAY, "another_furniture:service_bell");
        this.optional(OffgridTags.CAN_OVERLAY, dyed("create:%_seat"));
        this.optional(OffgridTags.CAN_OVERLAY, dyed("another_furniture:%_lamp"));
        this.optional(OffgridTags.CAN_OVERLAY, withWallTorches("caverns_and_chasms:cupric_torch", "nomansland:sconce_torch", "nomansland:sconce_soul_torch"));
        this.optional(OffgridTags.CAN_OVERLAY, withWallTorches("nomansland:extinguished_torch", "nomansland:extinguished_soul_torch", "nomansland:extinguished_sconce_torch", "nomansland:extinguished_sconce_soul_torch"));
        this.optional(OffgridTags.CAN_OVERLAY, "caverns_and_chasms:cupric_lantern", "upgrade_aquatic:tooth_lantern");
        this.optional(OffgridTags.CAN_OVERLAY, weathered("caverns_and_chasms:%copper_lantern"));
        this.optional(OffgridTags.CAN_OVERLAY, "blocksabound:light_bulb", "blocksabound:soul_light_bulb", "blocksabound:redstone_light_bulb", "blocksabound:copper_light_bulb");
        this.optional(OffgridTags.CAN_OVERLAY, "blocksabound:cage_light_bulb", "blocksabound:soul_cage_light_bulb", "blocksabound:redstone_cage_light_bulb", "blocksabound:copper_cage_light_bulb");
        this.optional(OffgridTags.CAN_OVERLAY, "supplementaries:candle_holder");
        this.optional(OffgridTags.CAN_OVERLAY, dyed("supplementaries:candle_holder_%"));
        this.optional(OffgridTags.CAN_OVERLAY, "abundant_atmosphere:rust_moss_carpet", "farmersdelight:full_tatami_mat", "farmersdelight:half_tatami_mat", "farmersdelight:canvas_rug", "supplementaries:doormat");
        this.optional(OffgridTags.CAN_OVERLAY, dyed("supplementaries:flag_%"));
        this.optional(OffgridTags.CAN_OVERLAY, withWallHeads(HEADS));
        this.optional(OffgridTags.CAN_OVERLAY, "nomansland:small_quartzite_bud", "nomansland:medium_quartzite_bud", "nomansland:large_quartzite_bud", "nomansland:quartzite_cluster");
        this.optional(OffgridTags.CAN_OVERLAY, "nomansland:pebbles", "nomansland:seashells", "nomansland:shelf_mushroom", "nomansland:tortoise_egg", "nomansland:warding_effigy");
        this.optional(OffgridTags.CAN_OVERLAY, "abundant_atmosphere:moss_clump", "abundant_atmosphere:rust_moss_clump", "abundant_atmosphere:gourdnut", "abundant_atmosphere:carved_gourdnut", "abundant_atmosphere:lamp_o_gourd");
        this.optional(OffgridTags.CAN_OVERLAY, "incubation:twig_nest", "incubation:hay_nest");
        this.optional(OffgridTags.CAN_OVERLAY, "caverns_and_chasms:exposed_lightning_rod", "caverns_and_chasms:weathered_lightning_rod", "caverns_and_chasms:oxidized_lightning_rod");
        this.optional(OffgridTags.CAN_OVERLAY, "caverns_and_chasms:waxed_lightning_rod", "caverns_and_chasms:waxed_exposed_lightning_rod", "caverns_and_chasms:waxed_weathered_lightning_rod", "caverns_and_chasms:waxed_oxidized_lightning_rod");
        this.optional(OffgridTags.CAN_OVERLAY, "create:desk_bell", "caverns_and_chasms:winch", "upgrade_aquatic:elder_eye", "brewinandchewin:coaster");
        this.optional(OffgridTags.CAN_BLOCKLOG, "farmersdelight:rope");
        this.optional(OffgridTags.CAN_OVERLAY, "supplementaries:hourglass", "supplementaries:goblet", "supplementaries:globe", "supplementaries:globe_sepia");
        this.optional(OffgridTags.CAN_OVERLAY, "amendments:directional_cake", "amendments:double_cake", "amendments:nomansland/double_fruit_cake", "nomansland:fruit_cake", "nomansland:pear_cobbler", "nomansland:stallion_strips");
        this.optional(OffgridTags.CAN_OVERLAY, "farmersdelight:apple_pie", "farmersdelight:sweet_berry_cheesecake", "farmersdelight:chocolate_pie", "farmersdelight:pumpkin_pie");
        this.optional(OffgridTags.CAN_OVERLAY, "farmersdelight:roast_chicken_block", "farmersdelight:honey_glazed_ham_block", "farmersdelight:shepherds_pie_block", "farmersdelight:stuffed_pumpkin_block", "farmersdelight:gleaming_salad_block", "farmersdelight:rice_roll_medley_block");
        this.optional(OffgridTags.CAN_OVERLAY, "brewinandchewin:unripe_flaxen_cheese_wheel", "brewinandchewin:unripe_scarlet_cheese_wheel", "brewinandchewin:flaxen_cheese_wheel", "brewinandchewin:scarlet_cheese_wheel");
        this.optional(OffgridTags.CAN_OVERLAY, "brewinandchewin:pizza", "brewinandchewin:rich_chocolate_cake", "brewinandchewin:pumpkin_roll", "brewinandchewin:quiche", "brewinandchewin:glow_berry_meringue_pie", "onion_onion:motley_grill_block");

        this.optional(OffgridTags.CAN_OFFSET, "passager:pigeon_nest", "farmersdelight:skillet", "farmersdelight:cooking_pot");
        this.optional(OffgridTags.CAN_OVERLAY, "supplementaries:sconce", "supplementaries:sconce_soul", "supplementaries:sconce_green", "supplementaries:sconce_ender", "supplementaries:sconce_glow", "supplementaries:sconce_nether_brass", "supplementaries:sconce_lever");
        this.optional(OffgridTags.CAN_OVERLAY, SCONCE_WALLS);

        this.optional(OffgridTags.FACING_ATTACHING, "nomansland:shelf_mushroom");
        this.optional(OffgridTags.FACING_ATTACHING, SCONCE_WALLS);
        this.optional(OffgridTags.FACING_ATTACHING, "caverns_and_chasms:cupric_wall_torch", "nomansland:sconce_wall_torch", "nomansland:sconce_soul_wall_torch");
        this.optional(OffgridTags.FACING_ATTACHING, "nomansland:extinguished_wall_torch", "nomansland:extinguished_soul_wall_torch", "nomansland:extinguished_sconce_wall_torch", "nomansland:extinguished_sconce_soul_wall_torch");
        this.optional(OffgridTags.FACING_ATTACHING, "nomansland:small_quartzite_bud", "nomansland:medium_quartzite_bud", "nomansland:large_quartzite_bud", "nomansland:quartzite_cluster", "nomansland:tap");
        this.optional(OffgridTags.FACING_ATTACHING, "caverns_and_chasms:exposed_lightning_rod", "caverns_and_chasms:weathered_lightning_rod", "caverns_and_chasms:oxidized_lightning_rod");
        this.optional(OffgridTags.FACING_ATTACHING, "caverns_and_chasms:waxed_lightning_rod", "caverns_and_chasms:waxed_exposed_lightning_rod", "caverns_and_chasms:waxed_weathered_lightning_rod", "caverns_and_chasms:waxed_oxidized_lightning_rod");
        this.optional(OffgridTags.FACING_ATTACHING, weathered("caverns_and_chasms:%floodlight"));
        this.optional(OffgridTags.FACING_ATTACHING, "simulated:iron_handle", "simulated:copper_handle");
        this.optional(OffgridTags.FACING_ATTACHING, dyed("simulated:%_handle"));
        this.optional(OffgridTags.FACING_ATTACHING, dyed("simulated:%_nameplate"));
        this.optional(OffgridTags.FACING_ATTACHING, "supplementaries:crank", "supplementaries:item_shelf", "create:desk_bell", "upgrade_aquatic:elder_eye", "upgrade_aquatic:tooth_lantern");
        this.optional(OffgridTags.FACING_ATTACHING, wallHeads(HEADS));
    }

    private static String[] dyed(String format) {
        String[] ids = new String[DYES.length];
        for (int i = 0; i < DYES.length; i++) {
            ids[i] = format.replace("%", DYES[i]);
        }
        return ids;
    }

    private static String[] weathered(String format) {
        String[] ids = new String[WEATHERING.length];
        for (int i = 0; i < WEATHERING.length; i++) {
            ids[i] = format.replace("%", WEATHERING[i]);
        }
        return ids;
    }

    private static String[] metal(String format) {
        String[] ids = new String[METAL.length];
        for (int i = 0; i < METAL.length; i++) {
            ids[i] = format.replace("%", METAL[i]);
        }
        return ids;
    }

    private static String[] withWallTorches(String... torches) {
        String[] ids = new String[torches.length * 2];
        for (int i = 0; i < torches.length; i++) {
            ids[i * 2] = torches[i];
            ids[i * 2 + 1] = torches[i].substring(0, torches[i].length() - "torch".length()) + "wall_torch";
        }
        return ids;
    }

    private static String[] withWallHeads(String... heads) {
        String[] ids = new String[heads.length * 2];
        for (int i = 0; i < heads.length; i++) {
            ids[i * 2] = heads[i];
            ids[i * 2 + 1] = wallHead(heads[i]);
        }
        return ids;
    }

    private static String[] wallHeads(String... heads) {
        String[] ids = new String[heads.length];
        for (int i = 0; i < heads.length; i++) {
            ids[i] = wallHead(heads[i]);
        }
        return ids;
    }

    private static String wallHead(String head) {
        return head.replaceAll("_(head|skull)$", "_wall_$1");
    }

    private void blocks(TagKey<Block> tag, Block... blocks) {
        for (Block block : blocks) {
            this.entries(tag).add(key(block));
        }
    }

    @SafeVarargs
    private void tags(TagKey<Block> tag, TagKey<Block>... tags) {
        for (TagKey<Block> other : tags) {
            this.entries(tag).forceAddTag(other);
        }
    }

    //? if >=26.2 {
    /*private TagAppender<Block> entries(TagKey<Block> tag) {
        return this.builder(tag);
    }
    *///?} else if >=26 {
    /*private TagAppender<ResourceKey<Block>, Block> entries(TagKey<Block> tag) {
        return this.builder(tag);
    }
    *///?} else {
    private FabricTagProvider<Block>.FabricTagBuilder entries(TagKey<Block> tag) {
        return this.getOrCreateTagBuilder(tag);
    }
    //?}

    private void optional(TagKey<Block> tag, String... ids) {
        for (String id : ids) {
            //? if >=26 {
            /*this.entries(tag).addOptional(ResourceKey.create(Registries.BLOCK, Identifier.parse(id)));
            *///?} else if >=1.21 {
            this.entries(tag).addOptional(ResourceLocation.parse(id));
            //?} else {
            /*this.entries(tag).addOptional(new ResourceLocation(id));
            *///?}
        }
    }

    private static TagKey<Block> conventional(String path) {
        //? if >=1.21 {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
        //?} else {
        /*return TagKey.create(Registries.BLOCK, new ResourceLocation("c", path));
        *///?}
    }

    //? if <1.21 {
    /*private void optionalTags(TagKey<Block> tag, String... ids) {
        for (String id : ids) {
            this.entries(tag).addOptionalTag(new ResourceLocation(id));
        }
    }
    *///?}

    private static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }
}
