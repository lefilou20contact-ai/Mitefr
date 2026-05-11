package fr.mitefr.block;

import fr.mitefr.MiteFRMod;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * Tous les blocs custom de MITE-FR.
 * Principalement les stations d'artisanat par ère.
 */
public final class MiteFRBlocks {

    // ── Stations d'artisanat ─────────────────────────────────────

    /** Foyer de Forge — Âge du Cuivre */
    public static final Block CLAY_FORGE = register("clay_forge",
        new ClayForgeBlock(FabricBlockSettings.create()
            .strength(1.5f, 3.0f)
            .sounds(BlockSoundGroup.STONE)
            .luminance(state -> 7)
            .requiresTool()));

    /** Forge du Bronzier — Âge du Bronze */
    public static final Block BRONZE_FORGE = register("bronze_forge",
        new BronzeForgeBlock(FabricBlockSettings.create()
            .strength(2.5f, 6.0f)
            .sounds(BlockSoundGroup.METAL)
            .luminance(state -> 9)
            .requiresTool()));

    /** Enclume de Maître — Âge du Fer */
    public static final Block MASTER_ANVIL = register("master_anvil",
        new MasterAnvilBlock(FabricBlockSettings.create()
            .strength(5.0f, 1200.0f)
            .sounds(BlockSoundGroup.ANVIL)
            .requiresTool()));

    /** Atelier du Néant — Âge du Néant */
    public static final Block VOID_WORKSHOP = register("void_workshop",
        new VoidWorkshopBlock(FabricBlockSettings.create()
            .strength(50.0f, 3600.0f)
            .sounds(BlockSoundGroup.ANCIENT_DEBRIS)
            .luminance(state -> 4)
            .requiresTool()));

    public static void register() {
        // Les blocs sont enregistrés au chargement de la classe
    }

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, new Identifier(MiteFRMod.MOD_ID, name), block);
    }
}
