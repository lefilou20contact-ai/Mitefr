package fr.mitefr.world;

import fr.mitefr.MiteFRMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.Feature;

/**
 * Modifications de génération du monde pour MITE-FR :
 *
 * 1. Réduction des veines de minerai de 50 %
 *    → via data pack (ore_features override — voir resources/data/)
 *
 * 2. Sources d'eau toxique (eau sombre) — marquées dans le chunk NBT
 *    via un tag custom, détectées par ThirstSystem
 *
 * 3. Aucun village généré
 *    → via biome modifier JSON (voir resources/data/mitefr/worldgen/)
 */
public final class MiteFRWorldGen {

    private MiteFRWorldGen() {}

    public static void register() {
        MiteFRMod.LOGGER.info("MITE-FR WorldGen enregistré.");
        // Les overrides de génération sont principalement dans les data packs
        // Les modifications de structures (villages) sont dans biome_modifiers
    }
}
