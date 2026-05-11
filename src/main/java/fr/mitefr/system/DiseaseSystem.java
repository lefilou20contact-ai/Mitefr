package fr.mitefr.system;

import fr.mitefr.data.Disease;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère les effets passifs de chaque maladie active et leur progression.
 */
public final class DiseaseSystem {

    // Tous les 2 secondes
    private static final int TICK_INTERVAL = 40;

    private DiseaseSystem() {}

    public static void tick(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_DISEASES)) return;

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();

        List<Disease> toRemove = new ArrayList<>();
        for (Disease disease : data.getDiseases()) {
            applyDiseaseEffect(player, data, disease);

            // Décompte durée naturelle
            if (!disease.isPermanent()) {
                data.tickDisease(disease);
                if (data.getDiseaseDuration(disease) <= 0) {
                    toRemove.add(disease);
                }
            }
        }

        for (Disease d : toRemove) {
            data.removeDisease(d);
            player.sendMessage(
                Text.literal("✦ Vous êtes guéri de : " + d.displayName)
                    .formatted(Formatting.GREEN),
                true
            );
        }
    }

    private static void applyDiseaseEffect(ServerPlayerEntity player, PlayerMiteData data, Disease disease) {
        long tick = player.getWorld().getTime();

        switch (disease) {
            case ROUILLE_DES_MINES -> {
                // Ralentissement de minage via effet Mining Fatigue
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.MINING_FATIGUE, 60, 1, false, false, true));
            }
            case FIEVRE_DES_MARAIS -> {
                // Nausée cyclique toutes les 10 secondes de jeu
                if (tick % 200 == 0) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA, 100, 0, false, false, true));
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, 100, 1, false, false, true));
                    data.modifyCondition(-2);
                }
            }
            case GANGRENE -> {
                // Dégâts croissants toutes les 30 secondes de jeu
                data.setGangreneTimer(data.getGangreneTimer() + 1);
                if (data.getGangreneTimer() >= 600) {
                    data.setGangreneTimer(0);
                    player.damage(player.getDamageSources().generic(), 2.0f);
                    data.modifyCondition(-5);
                    player.sendMessage(
                        Text.literal("⚠ La gangrène progresse...")
                            .formatted(Formatting.DARK_RED),
                        true
                    );
                }
            }
            case SCORBUT -> {
                // Fragilité des os — géré dans le mixin de dégâts de chute
                // Ici on s'assure juste de la persistance du debuff visuel
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 60, 0, false, false, true));
            }
            case HYPOTHERMIE -> {
                // Ralentissement sévère, condition chute
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 60, 2, false, false, true));
                if (tick % 100 == 0) data.modifyCondition(-1);
            }
            case HYPERTHERMIE -> {
                // Consommation de faim accélérée
                if (tick % 80 == 0) {
                    player.getHungerManager().addExhaustion(0.5f);
                }
            }
            case INTOXICATION_EAU -> {
                // Déjà appliqué à la consommation, ici juste la durée
            }
        }
    }

    /**
     * Vérifie les conditions d'acquisition de maladies environnementales.
     * Appelé depuis le tick serveur.
     */
    public static void checkEnvironmentalDiseases(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_DISEASES)) return;
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        long tick = player.getWorld().getTime();
        if (tick % 200 != 0) return; // Vérification toutes les 10 secondes

        var pos   = player.getBlockPos();
        var world = player.getWorld();
        var biome = world.getBiome(pos).value();

        // Fièvre des Marais — dans un marais la nuit sans torche à proximité
        boolean isSwamp = biome.hasPrecipitation()
            && biome.getTemperature() < 0.8f
            && pos.getY() < 64;
        if (isSwamp && !world.isSkyVisible(pos)) {
            if (!data.hasDisease(Disease.FIEVRE_DES_MARAIS) && Math.random() < 0.05) {
                data.addDisease(Disease.FIEVRE_DES_MARAIS);
                notifyNewDisease(player, Disease.FIEVRE_DES_MARAIS);
            }
        }

        // Scorbut — 5 jours sans fruits/légumes (géré via compteur dans FoodSystem)
        // → délégué à FoodSystem
    }

    public static void notifyNewDisease(ServerPlayerEntity player, Disease disease) {
        player.sendMessage(
            Text.literal("☠ Vous contractez : " + disease.displayName
                + " — " + disease.description)
                .formatted(Formatting.RED),
            false
        );
    }
}
