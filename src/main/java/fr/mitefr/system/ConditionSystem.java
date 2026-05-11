package fr.mitefr.system;

import fr.mitefr.MiteFRConstants;
import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.MiteFRGameRules;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Gère la jauge de Condition physique (0–100).
 * La condition diminue avec la fatigue, blessures, maladies, température extrême.
 * Elle remonte lentement quand le joueur mange, dort et reste au chaud.
 */
public final class ConditionSystem {

    private static final int TICK_INTERVAL = 20;

    private ConditionSystem() {}

    public static void tick(ServerPlayerEntity player) {
        if (!player.getWorld().getGameRules().getBoolean(MiteFRGameRules.ENABLE_CONDITION)) return;

        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.setConditionTimer(data.getConditionTimer() + 1);
        if (data.getConditionTimer() < TICK_INTERVAL) return;
        data.setConditionTimer(0);

        // Récupération passive si bien nourri et au chaud
        if (player.getHungerManager().getFoodLevel() >= 14
                && data.getTemperature() >= MiteFRConstants.TEMP_NORMAL_MIN
                && data.getTemperature() <= MiteFRConstants.TEMP_NORMAL_MAX) {
            data.modifyCondition(+1);
        }

        applyConditionEffects(player, data);
    }

    public static void applyConditionEffects(ServerPlayerEntity player, PlayerMiteData data) {
        int cond = data.getCondition();

        if (cond >= MiteFRConstants.CONDITION_TIER_FATIGUED) {
            // Pleine forme — retirer les debuffs
            removeDebuffs(player);
        } else if (cond >= MiteFRConstants.CONDITION_TIER_EXHAUSTED) {
            // Fatigué : vitesse -15%
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 40, 0, false, false, true));
        } else if (cond >= MiteFRConstants.CONDITION_TIER_CRITICAL) {
            // Épuisé : pas de sprint, minage -30%
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 40, 1, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE, 40, 1, false, false, true));
            player.setSprinting(false);
        } else {
            // Critique : déplacement minimal, incapable de miner
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 40, 3, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE, 40, 3, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS, 40, 2, false, false, true));
            player.setSprinting(false);
        }
    }

    private static void removeDebuffs(ServerPlayerEntity player) {
        // Retirer uniquement les effets appliqués par MITE-FR (non ambiants)
        // On ne retire pas les effets des maladies, seulement ceux de la condition
    }

    /**
     * Calcule la pénalité de condition causée par l'activité physique.
     * Appelé depuis les mixins lors du sprint ou combat.
     */
    public static void onSprint(ServerPlayerEntity player) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        if (player.getWorld().getTime() % 10 == 0) {
            data.modifyCondition(-1);
        }
    }

    public static void onTakeDamage(ServerPlayerEntity player, float amount) {
        PlayerMiteData data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.modifyCondition(-(int)(amount * 2));
    }
}
