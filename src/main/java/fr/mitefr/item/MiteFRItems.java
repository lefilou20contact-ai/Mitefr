package fr.mitefr.item;

import fr.mitefr.MiteFRMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Tous les items custom de MITE-FR.
 */
public final class MiteFRItems {

    // ── Outils Âge du Silex ───────────────────────────────────────
    public static final Item FLINT_KNIFE       = register("flint_knife",       new FlintKnifeItem());
    public static final Item FLINT_HATCHET     = register("flint_hatchet",     new FlintHatchetItem());
    public static final Item BONE_PICK          = register("bone_pick",         new BonePickItem());

    // ── Matières premières ────────────────────────────────────────
    public static final Item PLANT_FIBER       = register("plant_fiber",       new Item(new FabricItemSettings().maxCount(64)));
    public static final Item CRUDE_COPPER_INGOT= register("crude_copper_ingot",new Item(new FabricItemSettings().maxCount(64)));
    public static final Item BRONZE_INGOT      = register("bronze_ingot",      new Item(new FabricItemSettings().maxCount(64)));
    public static final Item CHARCOAL_FILTER   = register("charcoal_filter",   new Item(new FabricItemSettings().maxCount(16)));
    public static final Item SALT              = register("salt",               new Item(new FabricItemSettings().maxCount(64)));
    public static final Item MEDICINAL_HERB    = register("medicinal_herb",     new Item(new FabricItemSettings().maxCount(64)));
    public static final Item VOID_FRAGMENT     = register("void_fragment",      new Item(new FabricItemSettings().maxCount(16)));

    // ── Gourdes ───────────────────────────────────────────────────
    public static final Item EMPTY_CANTEEN     = register("empty_canteen",     new EmptyCanteenItem());
    public static final Item FILTERED_CANTEEN  = register("filtered_canteen",  new FilteredCanteenItem());

    // ── Remèdes ───────────────────────────────────────────────────
    public static final Item HERB_POULTICE     = register("herb_poultice",     new HerbPoulticeItem());
    public static final Item ANTI_GANGRENE     = register("anti_gangrene",     new AntiGangreneItem());
    public static final Item CITRUS_EXTRACT    = register("citrus_extract",    new CitrusExtractItem());

    // ── Équipement ────────────────────────────────────────────────
    public static final Item GAS_MASK          = register("gas_mask",          new GasMaskItem());
    public static final Item MINING_GLOVES     = register("mining_gloves",     new MiningGlovesItem());
    public static final Item CAMP_KIT          = register("camp_kit",          new CampKitItem());

    // ── Stations d'artisanat (blocs item) ────────────────────────
    public static final Item CLAY_FORGE        = register("clay_forge",        new BlockItem(fr.mitefr.block.MiteFRBlocks.CLAY_FORGE,       new FabricItemSettings()));
    public static final Item BRONZE_FORGE      = register("bronze_forge",      new BlockItem(fr.mitefr.block.MiteFRBlocks.BRONZE_FORGE,     new FabricItemSettings()));
    public static final Item MASTER_ANVIL      = register("master_anvil",      new BlockItem(fr.mitefr.block.MiteFRBlocks.MASTER_ANVIL,     new FabricItemSettings()));
    public static final Item VOID_WORKSHOP     = register("void_workshop",      new BlockItem(fr.mitefr.block.MiteFRBlocks.VOID_WORKSHOP,    new FabricItemSettings()));

    // ── Groupe créatif ────────────────────────────────────────────
    public static final RegistryKey<ItemGroup> GROUP_KEY = RegistryKey.of(
        Registries.ITEM_GROUP.getKey(),
        new Identifier(MiteFRMod.MOD_ID, "main")
    );

    public static void register() {
        // Créer le groupe créatif
        Registry.register(Registries.ITEM_GROUP, GROUP_KEY,
            FabricItemGroup.builder()
                .displayName(Text.literal("MITE-FR"))
                .icon(() -> new ItemStack(FLINT_KNIFE))
                .entries((ctx, entries) -> {
                    entries.add(FLINT_KNIFE);
                    entries.add(FLINT_HATCHET);
                    entries.add(BONE_PICK);
                    entries.add(PLANT_FIBER);
                    entries.add(CRUDE_COPPER_INGOT);
                    entries.add(BRONZE_INGOT);
                    entries.add(CHARCOAL_FILTER);
                    entries.add(SALT);
                    entries.add(MEDICINAL_HERB);
                    entries.add(VOID_FRAGMENT);
                    entries.add(EMPTY_CANTEEN);
                    entries.add(FILTERED_CANTEEN);
                    entries.add(HERB_POULTICE);
                    entries.add(ANTI_GANGRENE);
                    entries.add(CITRUS_EXTRACT);
                    entries.add(GAS_MASK);
                    entries.add(MINING_GLOVES);
                    entries.add(CAMP_KIT);
                    entries.add(CLAY_FORGE);
                    entries.add(BRONZE_FORGE);
                    entries.add(MASTER_ANVIL);
                    entries.add(VOID_WORKSHOP);
                })
                .build()
        );
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, new Identifier(MiteFRMod.MOD_ID, name), item);
    }
}
