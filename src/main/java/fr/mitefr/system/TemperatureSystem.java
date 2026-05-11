package fr.mitefr.system;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Gestion de la température corporelle du joueur.
 *
 * Sources de chaleur / froid prises en compte :
 *  - Biome (température intrinsèque)
 *  - Heure du jour (nuit = -5°C)
 *  - Météo (pluie = -4°C, neige = -8°C)
 *  - Profondeur (sous y=30 = -3°C)
 *  - Proximité de lave/feu (+10 à +20°C)
 *  - Armure portée (insulation légère)
 */
public final class TemperatureSystem {

    private static final int TICK_INTERVAL = 20; // Calcul toutes les secondes

    private TemperatureSystem() {}

    public static void tick(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_TEMPERATURE)) return;

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.setTemperatureTimer(data.getTemperatureTimer() + 1);
        if (data.getTemperatureTimer() < TICK_INTERVAL) return;
        data.setTemperatureTimer(0);

        float targetTemp = computeTargetTemperature(player);
        // Transition progressive vers la température cible
        float current = data.getTemperature();
        float delta   = (targetTemp - current) * 0.05f;
        data.setTemperature(current + delta);

        applyTemperatureEffects(player, data);
    }

    private static float computeTargetTemperature(ServerPlayerEntity player) {
        World world   = player.getWorld();
        BlockPos pos  = player.getBlockPos();

        // Base biome
        float biomeTemp = world.getBiome(pos).value().getTemperature();
        // Biome temp vanilla : 0.0 = neige, 0.5 = normal, 2.0 = désert
        float celsius = (biomeTemp * 30f) - 5f; // Conversion approximative

        // Nuit : -5°C
        long timeOfDay = world.getTimeOfDay() % 24000;
        if (timeOfDay > 13000 && timeOfDay < 23000) {
            celsius -= 5f;
        }

        // Pluie
        if (world.isRaining() && world.isSkyVisible(pos)) {
            celsius -= 4f;
        }

        // Neige (biome température < 0.15)
        if (biomeTemp < 0.15f) {
            celsius -= 8f;
        }

        // Sous la terre (y < 30)
        if (pos.getY() < 30) {
            celsius -= 3f;
        }

        // Proximité feu/lave (vérification dans un rayon de 4 blocs)
        if (hasHeatSource(player)) {
            celsius += 15f;
        }

        // Armure = insulation (+2°C par pièce)
        int armorCount = 0;
        for (var stack : player.getArmorItems()) {
            if (!stack.isEmpty()) armorCount++;
        }
        celsius += armorCount * 2f;

        return celsius;
    }

    private static boolean hasHeatSource(ServerPlayerEntity player) {
        World world  = player.getWorld();
        BlockPos pos = player.getBlockPos();
        int radius   = 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos check = pos.add(dx, dy, dz);
                    var block = world.getBlockState(check).getBlock();
                    if (block == net.minecraft.block.Blocks.LAVA
                     || block == net.minecraft.block.Blocks.FIRE
                     || block == net.minecraft.block.Blocks.CAMPFIRE
                     || block == net.minecraft.block.Blocks.SOUL_CAMPFIRE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void applyTemperatureEffects(ServerPlayerEntity player, PlayerMiteData data) {
        float temp = data.getTemperature();

        // Supprimer les maladies de température qui ne correspondent plus
        if (temp >= MiteFRConstants.TEMP_COLD && data.hasDisease(Disease.HYPOTHERMIE)) {
            data.removeDisease(Disease.HYPOTHERMIE);
        }
        if (temp <= MiteFRConstants.TEMP_HOT && data.hasDisease(Disease.HYPERTHERMIE)) {
            data.removeDisease(Disease.HYPERTHERMIE);
        }

        if (temp <= MiteFRConstants.TEMP_FREEZING) {
            // Gel : dégâts directs
            player.damage(player.getDamageSources().freeze(), 1.0f);
            data.modifyCondition(-3);
            if (!data.hasDisease(Disease.HYPOTHERMIE)) data.addDisease(Disease.HYPOTHERMIE);
        } else if (temp <= MiteFRConstants.TEMP_COLD) {
            // Froid : ralentissement et condition qui baisse
            data.modifyCondition(-1);
            if (!data.hasDisease(Disease.HYPOTHERMIE)) data.addDisease(Disease.HYPOTHERMIE);
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 100, 0, false, false, true));
        } else if (temp >= MiteFRConstants.TEMP_BURNING) {
            // Surchauffe : dégâts
            player.damage(player.getDamageSources().onFire(), 1.0f);
            data.modifyCondition(-2);
            if (!data.hasDisease(Disease.HYPERTHERMIE)) data.addDisease(Disease.HYPERTHERMIE);
        } else if (temp >= MiteFRConstants.TEMP_HOT) {
            // Chaud : faim doublée (faim += 1 bonus)
            data.modifyCondition(-1);
            if (!data.hasDisease(Disease.HYPERTHERMIE)) data.addDisease(Disease.HYPERTHERMIE);
        }
        // Zone normale : pas d'effet
    }
}
